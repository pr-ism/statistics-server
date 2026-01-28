# PR Label 이벤트 스펙

## 개요

PR에 라벨이 추가/삭제될 때 발생하는 이벤트입니다. `labeled`와 `unlabeled` 이벤트를 각각 다른 엔드포인트로 처리합니다.

## 수집 대상 Entity

| Entity | 설명 |
|--------|------|
| PrLabel | 현재 PR에 붙은 라벨 (현재 상태) |
| PrLabelHistory | 라벨 추가/삭제 이력 |

> **참고**: Label은 PR과 별도의 생명주기를 가짐

---

## GitHub Actions Workflow

> `pr-data-collector.yml`에 통합되어 있습니다.

```yaml
# pr-data-collector.yml 내 label 관련 부분

on:
  pull_request:
    types: [opened, labeled, unlabeled]

env:
  WEBHOOK_URL: ${{ secrets.STATISTICS_WEBHOOK_URL }}

# ... (opened 처리 생략)

      - name: Handle labeled event
        if: github.event.action == 'labeled'
        run: |
          PAYLOAD=$(jq -n \
            --arg repositoryFullName "${{ github.repository }}" \
            --argjson prNumber "${{ github.event.pull_request.number }}" \
            --arg labelName "${{ github.event.label.name }}" \
            --arg labeledAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
            '{
              repositoryFullName: $repositoryFullName,
              prNumber: $prNumber,
              label: {
                name: $labelName
              },
              labeledAt: $labeledAt
            }')

          if [ -n "$WEBHOOK_URL" ]; then
            curl -X POST \
              -H "Content-Type: application/json" \
              -H "X-API-Key: ${{ secrets.PRISM_API_KEY }}" \
              -d "$PAYLOAD" "$WEBHOOK_URL/label/added" --fail --silent --show-error
          fi

      - name: Handle unlabeled event
        if: github.event.action == 'unlabeled'
        run: |
          PAYLOAD=$(jq -n \
            --arg repositoryFullName "${{ github.repository }}" \
            --argjson prNumber "${{ github.event.pull_request.number }}" \
            --arg labelName "${{ github.event.label.name }}" \
            --arg unlabeledAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
            '{
              repositoryFullName: $repositoryFullName,
              prNumber: $prNumber,
              label: {
                name: $labelName
              },
              unlabeledAt: $unlabeledAt
            }')

          if [ -n "$WEBHOOK_URL" ]; then
            curl -X POST \
              -H "Content-Type: application/json" \
              -H "X-API-Key: ${{ secrets.PRISM_API_KEY }}" \
              -d "$PAYLOAD" "$WEBHOOK_URL/label/removed" --fail --silent --show-error
          fi
```

---

## Payload 구조

### Label Added

```json
{
  "repositoryFullName": "owner/repo",
  "prNumber": 1,
  "label": {
    "name": "bug"
  },
  "labeledAt": "2024-01-15T10:00:00Z"
}
```

### Label Removed

```json
{
  "repositoryFullName": "owner/repo",
  "prNumber": 1,
  "label": {
    "name": "bug"
  },
  "unlabeledAt": "2024-01-15T11:00:00Z"
}
```

---

## Server DTO

위치: `application/webhook/dto/request/`

### LabelAddedRequest.java

```java
public record LabelAddedRequest(
    String repositoryFullName,
    int prNumber,
    LabelData label,
    Instant labeledAt
) {
    public record LabelData(
        String name
    ) {}
}
```

### LabelRemovedRequest.java

```java
public record LabelRemovedRequest(
    String repositoryFullName,
    int prNumber,
    LabelData label,
    Instant unlabeledAt
) {
    public record LabelData(
        String name
    ) {}
}
```

---

## Entity 매핑

### PrLabel (라벨 추가 시)

| Payload 필드 | Entity 필드 | 변환 |
|-------------|-------------|------|
| label.name | labelName | 직접 매핑 |
| labeledAt | labeledAt | Instant → LocalDateTime |
| repositoryFullName + prNumber | pullRequestId | Project 조회 → PR 조회 후 연결 |

### PrLabelHistory

| Payload 필드 | Entity 필드 | 변환 |
|-------------|-------------|------|
| label.name | labelName | 직접 매핑 |
| - | action | ADDED 또는 REMOVED |
| labeledAt / unlabeledAt | changedAt | Instant → LocalDateTime |
| repositoryFullName + prNumber | pullRequestId | Project 조회 → PR 조회 후 연결 |

---

## 처리 흐름

### Label Added

```text
1. Webhook 수신 (Controller)
   ↓
2. LabelAddedRequest로 역직렬화
   ↓
3. X-API-Key 헤더로 Project 조회
   ↓
4. projectId + prNumber로 PullRequest 조회
   ↓
5. pullRequestId + labelName으로 PrLabel 존재 여부 확인
   ↓ (이미 존재하면 종료)
6. PrLabel Entity 생성 및 저장
   ↓
7. PrLabelHistory Entity 생성 및 저장 (action: ADDED)
```

### Label Removed

```text
1. Webhook 수신 (Controller)
   ↓
2. LabelRemovedRequest로 역직렬화
   ↓
3. X-API-Key 헤더로 Project 조회
   ↓
4. projectId + prNumber로 PullRequest 조회
   ↓
5. pullRequestId + labelName으로 PrLabel 존재 여부 확인
   ↓ (존재하지 않으면 종료)
6. PrLabel 삭제
   ↓
7. PrLabelHistory Entity 생성 및 저장 (action: REMOVED)
```

---

## 참고

- 동일한 라벨이 이미 존재하면 중복 저장 방지 (PrLabel, PrLabelHistory 모두 저장하지 않음)
- 삭제 시 해당 라벨이 없으면 아무 작업도 하지 않음 (PrLabelHistory도 저장하지 않음)

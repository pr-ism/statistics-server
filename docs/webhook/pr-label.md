# PullRequest Label 이벤트 스펙

## 개요

PullRequest에 라벨이 추가/삭제될 때 발생하는 이벤트입니다. `labeled`와 `unlabeled` 이벤트를 각각 다른 엔드포인트로 처리합니다.

## 수집 대상 Entity

| Entity | 설명 |
|--------|------|
| PullRequestLabel | 현재 PullRequest에 붙은 라벨 (현재 상태) |
| PullRequestLabelHistory | 라벨 추가/삭제 이력 |

> **참고**: Label은 PullRequest와 별도의 생명주기를 가짐

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
            --argjson pullRequestNumber "${{ github.event.pull_request.number }}" \
            --arg labelName "${{ github.event.label.name }}" \
            --arg labeledAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
            '{
              pullRequestNumber: $pullRequestNumber,
              label: {
                name: $labelName
              },
              labeledAt: $labeledAt
            }')

          if [ -n "$WEBHOOK_URL" ]; then
            curl -X POST \
              -H "Content-Type: application/json" \
              -H "X-API-Key: ${{ secrets.PRISM_API_KEY }}" \
              -d "$PAYLOAD" "$WEBHOOK_URL/pull-request/label/added" --fail --silent --show-error
          fi

      - name: Handle unlabeled event
        if: github.event.action == 'unlabeled'
        run: |
          PAYLOAD=$(jq -n \
            --argjson pullRequestNumber "${{ github.event.pull_request.number }}" \
            --arg labelName "${{ github.event.label.name }}" \
            --arg unlabeledAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
            '{
              pullRequestNumber: $pullRequestNumber,
              label: {
                name: $labelName
              },
              unlabeledAt: $unlabeledAt
            }')

          if [ -n "$WEBHOOK_URL" ]; then
            curl -X POST \
              -H "Content-Type: application/json" \
              -H "X-API-Key: ${{ secrets.PRISM_API_KEY }}" \
              -d "$PAYLOAD" "$WEBHOOK_URL/pull-request/label/removed" --fail --silent --show-error
          fi
```

---

## Payload 구조

### Label Added

```json
{
  "pullRequestNumber": 42,
  "label": {
    "name": "bug"
  },
  "labeledAt": "2024-01-15T10:00:00Z"
}
```

### Label Removed

```json
{
  "pullRequestNumber": 42,
  "label": {
    "name": "bug"
  },
  "unlabeledAt": "2024-01-15T11:00:00Z"
}
```

---

## Server DTO

위치: `application/webhook/dto/request/`

### PullRequestLabelAddedRequest.java

```java
public record PullRequestLabelAddedRequest(
    int pullRequestNumber,
    LabelData label,
    Instant labeledAt
) {
    public record LabelData(
        String name
    ) {}
}
```

### PullRequestLabelRemovedRequest.java

```java
public record PullRequestLabelRemovedRequest(
    int pullRequestNumber,
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

### PullRequestLabel (라벨 추가 시)

| Payload 필드 | Entity 필드 | 변환 |
|-------------|-------------|------|
| label.name | labelName | 직접 매핑 |
| labeledAt | labeledAt | Instant → LocalDateTime |
| pullRequestNumber | pullRequestId | X-API-Key로 Project 조회 → PullRequest 조회 후 연결 |

### PullRequestLabelHistory

| Payload 필드 | Entity 필드 | 변환 |
|-------------|-------------|------|
| label.name | labelName | 직접 매핑 |
| - | action | ADDED 또는 REMOVED |
| labeledAt / unlabeledAt | changedAt | Instant → LocalDateTime |
| pullRequestNumber | pullRequestId | X-API-Key로 Project 조회 → PullRequest 조회 후 연결 |

---

## 처리 흐름

### Label Added

```text
1. Webhook 수신 (Controller)
   ↓
2. PullRequestLabelAddedRequest로 역직렬화
   ↓
3. X-API-Key 헤더로 Project 조회
   ↓
4. projectId + pullRequestNumber로 PullRequest 조회
   ↓
5. pullRequestId + labelName으로 PullRequestLabel 존재 여부 확인
   ↓ (이미 존재하면 종료)
6. PullRequestLabel Entity 생성 및 저장
   ↓
7. PullRequestLabelHistory Entity 생성 및 저장 (action: ADDED)
```

### Label Removed

```text
1. Webhook 수신 (Controller)
   ↓
2. PullRequestLabelRemovedRequest로 역직렬화
   ↓
3. X-API-Key 헤더로 Project 조회
   ↓
4. projectId + pullRequestNumber로 PullRequest 조회
   ↓
5. pullRequestId + labelName으로 PullRequestLabel 존재 여부 확인
   ↓ (존재하지 않으면 종료)
6. PullRequestLabel 삭제
   ↓
7. PullRequestLabelHistory Entity 생성 및 저장 (action: REMOVED)
```

---

## 참고

- 동일한 라벨이 이미 존재하면 중복 저장 방지 (PullRequestLabel, PullRequestLabelHistory 모두 저장하지 않음)
- 삭제 시 해당 라벨이 없으면 아무 작업도 하지 않음 (PullRequestLabelHistory도 저장하지 않음)

# PR Opened 이벤트 스펙

## 개요

PR이 생성될 때 발생하는 이벤트입니다. Draft PR과 일반 PR 모두 동일한 엔드포인트에서 처리하며, `isDraft` 필드로 구분합니다.

## 수집 대상 Entity

| Entity | 설명 |
|--------|------|
| PullRequest | PR 기본 정보 |
| PrFile | PR 내 변경된 파일들 (현재 상태) |
| PrFileHistory | PR 내 변경된 파일 이력 |
| Commit | PR에 포함된 커밋들 |
| PrStateChangeHistory | 최초 상태 기록 (previous: null, new: DRAFT 또는 OPEN) |
| PrChangeHistory | 최초 변경 내역 기록 |

> **참고**: Label, ReviewRequest, Issue는 별도 이벤트에서 수집

---

## GitHub Actions Workflow

```yaml
name: PR Data Collector

on:
  pull_request:
    types: [opened]

env:
  WEBHOOK_URL: ${{ secrets.STATISTICS_WEBHOOK_URL }}

jobs:
  collect-pr-data:
    runs-on: ubuntu-latest
    steps:
      - name: Handle opened event
        if: github.event.action == 'opened'
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          PR_DATA=$(gh api graphql -f query='
            query($owner: String!, $repo: String!, $number: Int!) {
              repository(owner: $owner, name: $repo) {
                pullRequest(number: $number) {
                  number
                  title
                  url
                  additions
                  deletions
                  changedFiles
                  createdAt
                  author { login }
                  commits(first: 100) {
                    totalCount
                    nodes {
                      commit {
                        oid
                        committedDate
                      }
                    }
                  }
                }
              }
            }
          ' -f owner='${{ github.repository_owner }}' \
            -f repo='${{ github.event.repository.name }}' \
            -F number=${{ github.event.pull_request.number }})

          FILES_DATA=$(gh api \
            "repos/${{ github.repository }}/pulls/${{ github.event.pull_request.number }}/files" \
            --jq '[.[] | {filename, status, additions, deletions}]')

          PAYLOAD=$(jq -n \
            --argjson prData "$PR_DATA" \
            --argjson files "$FILES_DATA" \
            --arg repositoryFullName "${{ github.repository }}" \
            --argjson isDraft "${{ github.event.pull_request.draft }}" \
            '{
              eventType: "pull_request",
              action: "opened",
              repositoryFullName: $repositoryFullName,
              isDraft: $isDraft,
              pullRequest: $prData.data.repository.pullRequest,
              files: $files
            }')

          if [ -n "$WEBHOOK_URL" ]; then
            curl -X POST \
              -H "Content-Type: application/json" \
              -H "X-API-Key: ${{ secrets.PRISM_API_KEY }}" \
              -d "$PAYLOAD" "$WEBHOOK_URL" --fail --silent --show-error
          fi
```

---

## Payload 구조

```json
{
  "eventType": "pull_request",
  "action": "opened",
  "repositoryFullName": "owner/repo",
  "isDraft": false,
  "pullRequest": {
    "number": 1,
    "title": "PR 제목",
    "url": "https://github.com/owner/repo/pull/1",
    "additions": 100,
    "deletions": 50,
    "changedFiles": 10,
    "createdAt": "2024-01-15T10:00:00Z",
    "author": { "login": "username" },
    "commits": {
      "totalCount": 2,
      "nodes": [
        { "commit": { "oid": "abc123...", "committedDate": "2024-01-15T09:00:00Z" } },
        { "commit": { "oid": "def456...", "committedDate": "2024-01-15T09:30:00Z" } }
      ]
    }
  },
  "files": [
    {
      "filename": "src/main/java/Example.java",
      "status": "modified",
      "additions": 10,
      "deletions": 5
    }
  ]
}
```

---

## Server DTO

위치: `application/webhook/dto/request/PrOpenedRequest.java`

```java
public record PrOpenedRequest(
    String eventType,
    String action,
    String repositoryFullName,
    boolean isDraft,
    PullRequestData pullRequest,
    List<FileData> files
) {
    public record PullRequestData(
        int number,
        String title,
        String url,
        int additions,
        int deletions,
        int changedFiles,
        Instant createdAt,
        Author author,
        CommitsConnection commits
    ) {}

    public record Author(String login) {}

    public record CommitsConnection(int totalCount, List<CommitNode> nodes) {}

    public record CommitNode(CommitData commit) {}

    public record CommitData(String oid, Instant committedDate) {}

    public record FileData(
        String filename,
        String status,
        int additions,
        int deletions
    ) {}
}
```

---

## Entity 매핑

### PullRequest

| Payload 필드 | Entity 필드 | 변환 |
|-------------|-------------|------|
| pullRequest.number | prNumber | 그대로 |
| pullRequest.title | title | 그대로 |
| isDraft | state | `isDraft ? DRAFT : OPEN` |
| pullRequest.author.login | authorGithubId | 그대로 |
| pullRequest.url | link | 그대로 |
| pullRequest.additions | changeStats.additionCount | 그대로 |
| pullRequest.deletions | changeStats.deletionCount | 그대로 |
| pullRequest.changedFiles | changeStats.changedFileCount | 그대로 |
| pullRequest.commits.totalCount | commitCount | 그대로 |
| pullRequest.createdAt | timing.prCreatedAt | Instant → LocalDateTime |
| - | timing.mergedAt | null |
| - | timing.closedAt | null |
| repositoryFullName | projectId | Project 조회 후 연결 |

### PrFile

| Payload 필드 | Entity 필드 | 변환 |
|-------------|-------------|------|
| files[].filename | fileName | 그대로 |
| files[].status | changeType | FileChangeType.fromGitHubStatus() |
| files[].additions | fileChanges.additions | 그대로 |
| files[].deletions | fileChanges.deletions | 그대로 |
| - | pullRequestId | 저장된 PullRequest의 ID |

### PrFileHistory

| Payload 필드 | Entity 필드 | 변환 |
|-------------|-------------|------|
| files[].filename | fileName | 그대로 |
| files[].status | changeType | FileChangeType.fromGitHubStatus() |
| files[].additions | fileChanges.additions | 그대로 |
| files[].deletions | fileChanges.deletions | 그대로 |
| - | previousFileName | null (opened 시점에는 RENAMED 정보 없음) |
| pullRequest.createdAt | changedAt | Instant → LocalDateTime |
| - | pullRequestId | 저장된 PullRequest의 ID |

### Commit

| Payload 필드 | Entity 필드 | 변환 |
|-------------|-------------|------|
| commits.nodes[].commit.oid | commitSha | 그대로 |
| commits.nodes[].commit.committedDate | committedAt | Instant → LocalDateTime |
| - | pullRequestId | 저장된 PullRequest의 ID |

### PrStateChangeHistory (최초 상태)

| 필드 | 값 |
|------|-----|
| previousState | null |
| newState | `isDraft ? DRAFT : OPEN` |
| changedAt | pullRequest.createdAt |
| pullRequestId | 저장된 PullRequest의 ID |

### PrChangeHistory (최초 변경 내역)

| Payload 필드 | Entity 필드 |
|-------------|-------------|
| pullRequest.additions | changeStats.additions |
| pullRequest.deletions | changeStats.deletions |
| pullRequest.changedFiles | changeStats.changedFileCount |
| pullRequest.commits.totalCount | commitCount |
| pullRequest.createdAt | changedAt |
| - | pullRequestId |

---

## 처리 흐름

```text
1. Webhook 수신 (Controller)
   ↓
2. PrOpenedRequest로 역직렬화
   ↓
3. X-API-Key 헤더로 Project 조회
   ↓
4. isDraft 여부에 따라 분기
   - Draft PR → PrDraftCreatedEvent 발행 후 종료
   - 일반 PR → 계속 진행
   ↓
5. PullRequest Entity 생성 및 저장
   ↓
6. PrOpenCreatedEvent 발행
   ↓
7. EventListener들이 이벤트 수신 후 저장
   - PrFileEventListener → PrFile 목록 저장
   - PrFileHistoryEventListener → PrFileHistory 목록 저장
   - CommitEventListener → Commit 목록 저장
   - PrStateChangeHistoryEventListener → PrStateChangeHistory 저장
   - PrChangeHistoryEventListener → PrChangeHistory 저장
```

---

## 참고

- 동일한 PR이 이미 존재하면 업데이트 처리 필요 (재전송 대비)
- Label, ReviewRequest는 별도 이벤트에서 수집
- Issue는 별도 `issues` 이벤트에서 수집
- 커밋이 100개 초과 시 pagination 처리 필요

---

## 설계 검토 필요

### 1. Commit - 커밋별 변경량 추가 여부

**고민:** Commit에 additions/deletions를 추가하면 PrFile과 역할이 중복되는 것 아닌가?
**현재 선택:** Commit은 sha, committedAt만 저장 (파일 기준 추적은 PrFile 담당)
**검토 필요:** 둘 다 필요한지, 하나로 통합할 수 있는지?

### 2. PrFileHistory - opened 시점 RENAMED 처리

**고민:** opened 시점에 RENAMED 파일이 있을 수 있음 (브랜치에서 파일명 변경 후 PR 생성)
**현재 선택:** opened에서는 PrFileHistory.create() 사용 (previousFileName = null)
**이유:** PrOpenedRequest.FileData에 previousFilename 정보가 없음
**향후:** synchronize 이벤트에서 RENAMED 처리 시 createRenamed() 사용 예정

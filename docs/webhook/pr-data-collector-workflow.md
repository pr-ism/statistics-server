# PR Data Collector GitHub Actions Workflow

## 개요

PullRequest 관련 데이터를 수집하기 위한 GitHub Actions Workflow 설정입니다. 현재 서버에 구현된 모든 엔드포인트를 트리거할 수 있도록 구성되어 있습니다.

---

## GitHub Actions Workflow

```yaml
name: PR Data Collector

on:
  pull_request:
    types:
      # 현재 구현됨
      - opened
      - labeled
      - unlabeled
      - review_requested
      - review_request_removed
      # 추가 구현 필요 (PullRequestState 완전 지원을 위해)
      - closed              # PR 머지/닫기 → MERGED/CLOSED 상태
      - reopened            # PR 재오픈 → OPEN 상태
      - synchronize         # 새 커밋 푸시 → 파일/커밋 변경 추적
      - ready_for_review    # Draft → Open 전환
      - converted_to_draft  # Open → Draft 전환

  pull_request_review:
    types:
      - submitted           # 리뷰 제출 (APPROVED, CHANGES_REQUESTED, COMMENTED)

  pull_request_review_comment:
    types:
      - created             # 리뷰 코멘트 생성
      - edited              # 리뷰 코멘트 수정
      - deleted             # 리뷰 코멘트 삭제

env:
  WEBHOOK_URL: ${{ secrets.STATISTICS_WEBHOOK_URL }}

jobs:
  collect-pr-data:
    runs-on: ubuntu-latest
    steps:
      # ============================================
      # Pull Request Events
      # ============================================

      - name: Handle opened event
        if: github.event_name == 'pull_request' && github.event.action == 'opened'
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
            --argjson isDraft "${{ github.event.pull_request.draft }}" \
            '{
              isDraft: $isDraft,
              pullRequest: $prData.data.repository.pullRequest,
              files: $files
            }')

          if [ -n "$WEBHOOK_URL" ]; then
            curl -X POST \
              -H "Content-Type: application/json" \
              -H "X-API-Key: ${{ secrets.PRISM_API_KEY }}" \
              -d "$PAYLOAD" "$WEBHOOK_URL/pull-request/opened" --fail --silent --show-error
          fi

      - name: Handle labeled event
        if: github.event_name == 'pull_request' && github.event.action == 'labeled'
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
        if: github.event_name == 'pull_request' && github.event.action == 'unlabeled'
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

      - name: Handle review_requested event
        if: github.event_name == 'pull_request' && github.event.action == 'review_requested'
        run: |
          PAYLOAD=$(jq -n \
            --argjson pullRequestNumber "${{ github.event.pull_request.number }}" \
            --arg login "${{ github.event.requested_reviewer.login }}" \
            --argjson id "${{ github.event.requested_reviewer.id }}" \
            --arg requestedAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
            '{
              pullRequestNumber: $pullRequestNumber,
              reviewer: {
                login: $login,
                id: $id
              },
              requestedAt: $requestedAt
            }')

          if [ -n "$WEBHOOK_URL" ]; then
            curl -X POST \
              -H "Content-Type: application/json" \
              -H "X-API-Key: ${{ secrets.PRISM_API_KEY }}" \
              -d "$PAYLOAD" "$WEBHOOK_URL/review/reviewer/added" --fail --silent --show-error
          fi

      - name: Handle review_request_removed event
        if: github.event_name == 'pull_request' && github.event.action == 'review_request_removed'
        run: |
          PAYLOAD=$(jq -n \
            --argjson pullRequestNumber "${{ github.event.pull_request.number }}" \
            --arg login "${{ github.event.requested_reviewer.login }}" \
            --argjson id "${{ github.event.requested_reviewer.id }}" \
            --arg removedAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
            '{
              pullRequestNumber: $pullRequestNumber,
              reviewer: {
                login: $login,
                id: $id
              },
              removedAt: $removedAt
            }')

          if [ -n "$WEBHOOK_URL" ]; then
            curl -X POST \
              -H "Content-Type: application/json" \
              -H "X-API-Key: ${{ secrets.PRISM_API_KEY }}" \
              -d "$PAYLOAD" "$WEBHOOK_URL/review/reviewer/removed" --fail --silent --show-error
          fi

      # ============================================
      # Pull Request Review Events
      # ============================================

      - name: Handle review submitted event
        if: github.event_name == 'pull_request_review' && github.event.action == 'submitted'
        run: |
          PAYLOAD=$(jq -n \
            --argjson githubPullRequestId "${{ github.event.pull_request.id }}" \
            --argjson pullRequestNumber "${{ github.event.pull_request.number }}" \
            --argjson githubReviewId "${{ github.event.review.id }}" \
            --arg reviewerLogin "${{ github.event.review.user.login }}" \
            --argjson reviewerId "${{ github.event.review.user.id }}" \
            --arg state "${{ github.event.review.state }}" \
            --arg commitSha "${{ github.event.review.commit_id }}" \
            --arg body "${{ github.event.review.body }}" \
            --arg submittedAt "${{ github.event.review.submitted_at }}" \
            '{
              githubPullRequestId: $githubPullRequestId,
              pullRequestNumber: $pullRequestNumber,
              githubReviewId: $githubReviewId,
              reviewer: {
                login: $reviewerLogin,
                id: $reviewerId
              },
              state: $state,
              commitSha: $commitSha,
              body: $body,
              commentCount: 0,
              submittedAt: $submittedAt
            }')

          if [ -n "$WEBHOOK_URL" ]; then
            curl -X POST \
              -H "Content-Type: application/json" \
              -H "X-API-Key: ${{ secrets.PRISM_API_KEY }}" \
              -d "$PAYLOAD" "$WEBHOOK_URL/review/submitted" --fail --silent --show-error
          fi

      # ============================================
      # Pull Request Review Comment Events
      # ============================================

      - name: Handle review comment created event
        if: github.event_name == 'pull_request_review_comment' && github.event.action == 'created'
        run: |
          PAYLOAD=$(jq -n \
            --argjson githubCommentId "${{ github.event.comment.id }}" \
            --argjson githubReviewId "${{ github.event.comment.pull_request_review_id }}" \
            --arg body "${{ github.event.comment.body }}" \
            --arg path "${{ github.event.comment.path }}" \
            --argjson line "${{ github.event.comment.line }}" \
            --argjson startLine "${{ github.event.comment.start_line }}" \
            --arg side "${{ github.event.comment.side }}" \
            --arg commitSha "${{ github.event.comment.commit_id }}" \
            --argjson inReplyToId "${{ github.event.comment.in_reply_to_id }}" \
            --arg authorLogin "${{ github.event.comment.user.login }}" \
            --argjson authorId "${{ github.event.comment.user.id }}" \
            --arg createdAt "${{ github.event.comment.created_at }}" \
            --arg updatedAt "${{ github.event.comment.updated_at }}" \
            '{
              githubCommentId: $githubCommentId,
              githubReviewId: $githubReviewId,
              body: $body,
              path: $path,
              line: $line,
              startLine: (if $startLine == "null" then null else $startLine end),
              side: $side,
              commitSha: $commitSha,
              inReplyToId: (if $inReplyToId == "null" then null else $inReplyToId end),
              author: {
                login: $authorLogin,
                id: $authorId
              },
              createdAt: $createdAt,
              updatedAt: $updatedAt
            }')

          if [ -n "$WEBHOOK_URL" ]; then
            curl -X POST \
              -H "Content-Type: application/json" \
              -H "X-API-Key: ${{ secrets.PRISM_API_KEY }}" \
              -d "$PAYLOAD" "$WEBHOOK_URL/review/comment/created" --fail --silent --show-error
          fi

      - name: Handle review comment edited event
        if: github.event_name == 'pull_request_review_comment' && github.event.action == 'edited'
        run: |
          PAYLOAD=$(jq -n \
            --argjson githubCommentId "${{ github.event.comment.id }}" \
            --arg body "${{ github.event.comment.body }}" \
            --arg updatedAt "${{ github.event.comment.updated_at }}" \
            '{
              githubCommentId: $githubCommentId,
              body: $body,
              updatedAt: $updatedAt
            }')

          if [ -n "$WEBHOOK_URL" ]; then
            curl -X POST \
              -H "Content-Type: application/json" \
              -H "X-API-Key: ${{ secrets.PRISM_API_KEY }}" \
              -d "$PAYLOAD" "$WEBHOOK_URL/review/comment/edited" --fail --silent --show-error
          fi

      - name: Handle review comment deleted event
        if: github.event_name == 'pull_request_review_comment' && github.event.action == 'deleted'
        run: |
          PAYLOAD=$(jq -n \
            --argjson githubCommentId "${{ github.event.comment.id }}" \
            --arg updatedAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
            '{
              githubCommentId: $githubCommentId,
              updatedAt: $updatedAt
            }')

          if [ -n "$WEBHOOK_URL" ]; then
            curl -X POST \
              -H "Content-Type: application/json" \
              -H "X-API-Key: ${{ secrets.PRISM_API_KEY }}" \
              -d "$PAYLOAD" "$WEBHOOK_URL/review/comment/deleted" --fail --silent --show-error
          fi

      # ============================================
      # TODO: 추가 구현 필요한 이벤트
      # ============================================

      # - name: Handle closed event
      #   if: github.event_name == 'pull_request' && github.event.action == 'closed'
      #   # PR이 머지되었는지 여부: github.event.pull_request.merged == true
      #   # 엔드포인트: /pull-request/closed (구현 필요)

      # - name: Handle reopened event
      #   if: github.event_name == 'pull_request' && github.event.action == 'reopened'
      #   # 엔드포인트: /pull-request/reopened (구현 필요)

      # - name: Handle synchronize event
      #   if: github.event_name == 'pull_request' && github.event.action == 'synchronize'
      #   # 새 커밋 푸시 시 파일/커밋 변경 추적
      #   # 엔드포인트: /pull-request/synchronize (구현 필요)

      # - name: Handle ready_for_review event
      #   if: github.event_name == 'pull_request' && github.event.action == 'ready_for_review'
      #   # Draft → Open 전환
      #   # 엔드포인트: /pull-request/ready-for-review (구현 필요)

      # - name: Handle converted_to_draft event
      #   if: github.event_name == 'pull_request' && github.event.action == 'converted_to_draft'
      #   # Open → Draft 전환
      #   # 엔드포인트: /pull-request/converted-to-draft (구현 필요)
```

---

## 이벤트 타입별 엔드포인트 매핑

### 현재 구현됨

| GitHub Event | Action | 서버 엔드포인트 |
|---|---|---|
| pull_request | opened | `/collect/pull-request/opened` |
| pull_request | labeled | `/collect/pull-request/label/added` |
| pull_request | unlabeled | `/collect/pull-request/label/removed` |
| pull_request | review_requested | `/collect/review/reviewer/added` |
| pull_request | review_request_removed | `/collect/review/reviewer/removed` |
| pull_request_review | submitted | `/collect/review/submitted` |
| pull_request_review_comment | created | `/collect/review/comment/created` |
| pull_request_review_comment | edited | `/collect/review/comment/edited` |
| pull_request_review_comment | deleted | `/collect/review/comment/deleted` |

### 추가 구현 필요

| GitHub Event | Action | 용도 | 관련 PullRequestState |
|---|---|---|---|
| pull_request | closed | PR 머지/닫기 | MERGED, CLOSED |
| pull_request | reopened | PR 재오픈 | OPEN |
| pull_request | synchronize | 새 커밋 푸시 | - |
| pull_request | ready_for_review | Draft → Open | OPEN |
| pull_request | converted_to_draft | Open → Draft | DRAFT |

---

## 필요한 GitHub Secrets

| Secret Name | 설명 |
|---|---|
| `STATISTICS_WEBHOOK_URL` | 통계 서버 webhook 기본 URL (예: `https://api.example.com/collect`) |
| `PRISM_API_KEY` | 프로젝트별 API 키 |
| `GITHUB_TOKEN` | GitHub API 접근용 (자동 제공) |

---

## 참고

- `pull_request_review`와 `pull_request_review_comment`는 `pull_request`와 별개의 이벤트 타입입니다.
- 각 이벤트 타입은 별도로 `on:` 섹션에 정의해야 트리거됩니다.
- `github.event_name`으로 이벤트 타입을, `github.event.action`으로 액션 타입을 구분합니다.

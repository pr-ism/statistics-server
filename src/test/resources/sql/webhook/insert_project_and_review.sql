INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 1);

INSERT INTO reviews (id, created_at, github_pull_request_id, github_review_id, user_name, user_id, review_state, head_commit_sha, comment_count, submitted_at)
VALUES (1, CURRENT_TIMESTAMP, 1001, 200, 'reviewer1', 12345, 'APPROVED', 'abc123sha', 0, CURRENT_TIMESTAMP);

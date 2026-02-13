INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, '2024-01-15 10:00:00', '2024-01-15 10:00:00', '테스트 프로젝트', 'test-api-key', 1);

INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at)
VALUES (1, '2024-01-15 10:00:00', 999, 1, 'testuser', 1, 123, 'abc123def456', '테스트 PR', 'OPEN', 'https://github.com/test/repo/pull/123', 5, 100, 50, 3, '2024-01-15 10:00:00');

INSERT INTO requested_reviewers (id, created_at, github_pull_request_id, pull_request_number, head_commit_sha, user_name, user_id, github_requested_at)
VALUES (1, '2024-01-15 10:00:00', 999, 123, 'abc123def456', 'reviewer1', 12345, '2024-01-15 10:00:00');

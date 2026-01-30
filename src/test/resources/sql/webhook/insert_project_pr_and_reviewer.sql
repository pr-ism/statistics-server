INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, '2024-01-15 10:00:00', '2024-01-15 10:00:00', '테스트 프로젝트', 'test-api-key', 1);

INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pr_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pr_created_at)
VALUES (1, '2024-01-15 10:00:00', 1, 'testuser', 123, '테스트 PR', 'OPEN', 'https://github.com/test/repo/pull/123', 5, 100, 50, 3, '2024-01-15 10:00:00');

INSERT INTO requested_reviewers (id, created_at, pull_request_id, github_mention, github_uid, requested_at)
VALUES (1, '2024-01-15 10:00:00', 1, 'reviewer1', 12345, '2024-01-15 10:00:00');

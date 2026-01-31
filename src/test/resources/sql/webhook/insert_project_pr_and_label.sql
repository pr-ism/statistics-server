INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 1);

INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at)
VALUES (1, CURRENT_TIMESTAMP, 1, 'testuser', 123, '테스트 PR', 'OPEN', 'https://github.com/test/repo/pull/123', 5, 100, 50, 3, CURRENT_TIMESTAMP);

INSERT INTO pull_request_labels (id, created_at, pull_request_id, label_name, labeled_at)
VALUES (1, CURRENT_TIMESTAMP, 1, 'bug', CURRENT_TIMESTAMP);

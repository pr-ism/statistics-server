INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 7);

INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at)
VALUES (1, CURRENT_TIMESTAMP, 1, 'author1', 10, '타이밍 누락 PR', 'OPEN', 'https://github.com/test/repo/pull/10', 3, 50, 20, 2, NULL);

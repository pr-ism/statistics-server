INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 7);

INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at)
VALUES (1, CURRENT_TIMESTAMP, 1001, 1, 'author1', 1, 10, 'sha001', '타이밍 누락 PR', 'OPEN', 'https://github.com/test/repo/pull/10', 3, 50, 20, 2, NULL);

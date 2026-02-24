INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 1);

INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at)
VALUES (1, CURRENT_TIMESTAMP, 1001, 1, 'testuser', 1, 123, 'sha1', '테스트 PR', 'OPEN', 'https://github.com/test/repo/pull/123', 5, 100, 50, 2, CURRENT_TIMESTAMP);

INSERT INTO commits (pull_request_id, github_pull_request_id, commit_sha, committed_at, created_at)
VALUES (1, 1001, 'sha1', '2024-01-15 18:00:00', CURRENT_TIMESTAMP),
       (1, 1001, 'sha2', '2024-01-15 18:30:00', CURRENT_TIMESTAMP);

INSERT INTO pull_request_files (pull_request_id, github_pull_request_id, file_name, change_type, additions, deletions, created_at)
VALUES (1, 1001, 'src/main/java/OldFile.java', 'MODIFIED', 50, 30, CURRENT_TIMESTAMP),
       (1, 1001, 'src/main/java/OldFile2.java', 'ADDED', 50, 20, CURRENT_TIMESTAMP);

INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 7);

INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at)
VALUES (1, CURRENT_TIMESTAMP, 1001, 1, 'author1', 1, 10, 'sha001', '첫 번째 PR', 'OPEN', 'https://github.com/test/repo/pull/10', 3, 50, 20, 2, '2024-01-15 10:00:00');

INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at, github_merged_at, github_closed_at)
VALUES (2, CURRENT_TIMESTAMP, 1002, 1, 'author2', 2, 20, 'sha002', '두 번째 PR', 'MERGED', 'https://github.com/test/repo/pull/20', 5, 100, 30, 4, '2024-01-10 09:00:00', '2024-01-12 15:00:00', '2024-01-12 15:00:00');

INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at, github_closed_at)
VALUES (3, CURRENT_TIMESTAMP, 1003, 1, 'author1', 1, 30, 'sha003', '세 번째 PR', 'CLOSED', 'https://github.com/test/repo/pull/30', 1, 10, 5, 1, '2024-01-20 14:00:00', '2024-01-21 10:00:00');

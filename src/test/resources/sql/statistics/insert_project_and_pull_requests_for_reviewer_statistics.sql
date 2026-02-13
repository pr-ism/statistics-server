INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 7);

INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at)
VALUES (1, CURRENT_TIMESTAMP, 1001, 1, 'author1', 1, 10, 'sha001', '첫 번째 PR', 'OPEN', 'https://github.com/test/repo/pull/10', 3, 100, 40, 2, '2024-01-15 10:00:00');

INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at, github_merged_at, github_closed_at)
VALUES (2, CURRENT_TIMESTAMP, 1002, 1, 'author2', 2, 20, 'sha002', '두 번째 PR', 'MERGED', 'https://github.com/test/repo/pull/20', 5, 200, 60, 4, '2024-01-10 09:00:00', '2024-01-12 15:00:00', '2024-01-12 15:00:00');

INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at, github_merged_at, github_closed_at)
VALUES (3, CURRENT_TIMESTAMP, 1003, 1, 'author1', 1, 30, 'sha003', '세 번째 PR', 'MERGED', 'https://github.com/test/repo/pull/30', 2, 50, 10, 1, '2024-01-20 14:00:00', '2024-01-21 10:00:00', '2024-01-21 10:00:00');

INSERT INTO requested_reviewers (id, created_at, pull_request_id, github_pull_request_id, head_commit_sha, user_name, user_id, github_requested_at)
VALUES (1, CURRENT_TIMESTAMP, 1, 1001, 'sha001', 'reviewer1', 1001, '2024-01-15 11:00:00');

INSERT INTO requested_reviewers (id, created_at, pull_request_id, github_pull_request_id, head_commit_sha, user_name, user_id, github_requested_at)
VALUES (2, CURRENT_TIMESTAMP, 2, 1002, 'sha002', 'reviewer1', 1001, '2024-01-10 10:00:00');

INSERT INTO requested_reviewers (id, created_at, pull_request_id, github_pull_request_id, head_commit_sha, user_name, user_id, github_requested_at)
VALUES (3, CURRENT_TIMESTAMP, 2, 1002, 'sha002', 'reviewer2', 1002, '2024-01-10 10:00:00');

INSERT INTO requested_reviewers (id, created_at, pull_request_id, github_pull_request_id, head_commit_sha, user_name, user_id, github_requested_at)
VALUES (4, CURRENT_TIMESTAMP, 3, 1003, 'sha003', 'reviewer2', 1002, '2024-01-20 15:00:00');

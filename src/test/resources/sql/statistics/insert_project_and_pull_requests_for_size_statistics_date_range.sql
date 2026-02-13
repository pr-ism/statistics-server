INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 7);

-- PR1: 2024-01-10, SMALL (50 + 20 = 70)
INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at)
VALUES (1, CURRENT_TIMESTAMP, 1001, 1, 'author1', 1, 10, 'sha001', 'Small PR', 'OPEN', 'https://github.com/test/repo/pull/10', 2, 50, 20, 1, '2024-01-10 09:00:00');

-- PR2: 2024-01-20, MEDIUM (180 + 50 = 230)
INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at, github_merged_at, github_closed_at)
VALUES (2, CURRENT_TIMESTAMP, 1002, 1, 'author2', 2, 20, 'sha002', 'Medium PR', 'MERGED', 'https://github.com/test/repo/pull/20', 5, 180, 50, 3, '2024-01-20 10:00:00', '2024-01-22 15:00:00', '2024-01-22 15:00:00');

-- PR3: 2024-02-15, LARGE (400 + 150 = 550)
INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at, github_merged_at, github_closed_at)
VALUES (3, CURRENT_TIMESTAMP, 1003, 1, 'author1', 1, 30, 'sha003', 'Large PR', 'MERGED', 'https://github.com/test/repo/pull/30', 10, 400, 150, 7, '2024-02-15 14:00:00', '2024-02-17 10:00:00', '2024-02-17 10:00:00');

-- PR4: 2024-03-01, EXTRA_LARGE (700 + 300 = 1000)
INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at, github_merged_at, github_closed_at)
VALUES (4, CURRENT_TIMESTAMP, 1004, 1, 'author2', 2, 40, 'sha004', 'XL PR', 'MERGED', 'https://github.com/test/repo/pull/40', 22, 700, 300, 14, '2024-03-01 08:00:00', '2024-03-03 12:00:00', '2024-03-03 12:00:00');

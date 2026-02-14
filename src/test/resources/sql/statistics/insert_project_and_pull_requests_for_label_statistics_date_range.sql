INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 7);

-- PR1: 2024-01-10 (feature, enhancement)
INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at, github_merged_at, github_closed_at)
VALUES (1, CURRENT_TIMESTAMP, 1001, 1, 'author1', 1, 10, 'sha001', '기능 개발 PR', 'MERGED', 'https://github.com/test/repo/pull/10', 8, 400, 100, 5, '2024-01-10 09:00:00', '2024-01-12 15:00:00', '2024-01-12 15:00:00');

-- PR2: 2024-01-15 (bug)
INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at)
VALUES (2, CURRENT_TIMESTAMP, 1002, 1, 'author2', 2, 20, 'sha002', '버그 수정 PR', 'OPEN', 'https://github.com/test/repo/pull/20', 2, 50, 20, 1, '2024-01-15 10:00:00');

-- PR3: 2024-02-01 (bug)
INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at, github_merged_at, github_closed_at)
VALUES (3, CURRENT_TIMESTAMP, 1003, 1, 'author1', 1, 30, 'sha003', '두 번째 버그 수정', 'MERGED', 'https://github.com/test/repo/pull/30', 4, 150, 60, 3, '2024-02-01 14:00:00', '2024-02-02 10:00:00', '2024-02-02 10:00:00');

-- PR4: 2024-03-01 (refactor)
INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at, github_merged_at, github_closed_at)
VALUES (4, CURRENT_TIMESTAMP, 1004, 1, 'author2', 2, 40, 'sha004', '리팩토링 PR', 'MERGED', 'https://github.com/test/repo/pull/40', 6, 200, 180, 2, '2024-03-01 08:00:00', '2024-03-02 12:00:00', '2024-03-02 12:00:00');

INSERT INTO pull_request_labels (id, created_at, github_pull_request_id, pull_request_id, head_commit_sha, label_name, github_labeled_at)
VALUES (1, CURRENT_TIMESTAMP, 1001, 1, 'sha001', 'feature', '2024-01-10 09:00:00');

INSERT INTO pull_request_labels (id, created_at, github_pull_request_id, pull_request_id, head_commit_sha, label_name, github_labeled_at)
VALUES (2, CURRENT_TIMESTAMP, 1001, 1, 'sha001', 'enhancement', '2024-01-10 09:30:00');

INSERT INTO pull_request_labels (id, created_at, github_pull_request_id, pull_request_id, head_commit_sha, label_name, github_labeled_at)
VALUES (3, CURRENT_TIMESTAMP, 1002, 2, 'sha002', 'bug', '2024-01-15 10:00:00');

INSERT INTO pull_request_labels (id, created_at, github_pull_request_id, pull_request_id, head_commit_sha, label_name, github_labeled_at)
VALUES (4, CURRENT_TIMESTAMP, 1003, 3, 'sha003', 'bug', '2024-02-01 14:00:00');

INSERT INTO pull_request_labels (id, created_at, github_pull_request_id, pull_request_id, head_commit_sha, label_name, github_labeled_at)
VALUES (5, CURRENT_TIMESTAMP, 1004, 4, 'sha004', 'refactor', '2024-03-01 08:00:00');

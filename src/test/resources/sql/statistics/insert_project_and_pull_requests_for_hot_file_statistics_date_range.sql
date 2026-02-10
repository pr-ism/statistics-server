INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 7);

-- PR1: 2024-01-10
INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (1, CURRENT_TIMESTAMP, 1001, 1, 'author1', 1, 10, 'sha001', '1월 초 PR', 'MERGED', 'https://github.com/test/repo/pull/10', 2, 100, 40, 2, '2024-01-10 09:00:00', '2024-01-12 15:00:00', '2024-01-12 15:00:00');

-- PR2: 2024-01-20
INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at)
VALUES (2, CURRENT_TIMESTAMP, 1002, 1, 'author2', 2, 20, 'sha002', '1월 중순 PR', 'OPEN', 'https://github.com/test/repo/pull/20', 3, 150, 50, 3, '2024-01-20 10:00:00');

-- PR3: 2024-02-15
INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (3, CURRENT_TIMESTAMP, 1003, 1, 'author1', 1, 30, 'sha003', '2월 PR', 'MERGED', 'https://github.com/test/repo/pull/30', 2, 200, 80, 4, '2024-02-15 14:00:00', '2024-02-16 10:00:00', '2024-02-16 10:00:00');

-- PR4: 2024-03-01
INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (4, CURRENT_TIMESTAMP, 1004, 1, 'author2', 2, 40, 'sha004', '3월 PR', 'MERGED', 'https://github.com/test/repo/pull/40', 2, 120, 60, 2, '2024-03-01 08:00:00', '2024-03-02 12:00:00', '2024-03-02 12:00:00');

-- PR1 파일 (2024-01-10): Application.java(MODIFIED), README.md(ADDED)
INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (1, CURRENT_TIMESTAMP, 1, 'src/main/java/Application.java', 'MODIFIED', 50, 20);

INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (2, CURRENT_TIMESTAMP, 1, 'README.md', 'ADDED', 50, 0);

-- PR2 파일 (2024-01-20): Application.java(MODIFIED), Config.java(ADDED), README.md(MODIFIED)
INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (3, CURRENT_TIMESTAMP, 2, 'src/main/java/Application.java', 'MODIFIED', 60, 25);

INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (4, CURRENT_TIMESTAMP, 2, 'src/main/java/Config.java', 'ADDED', 70, 0);

INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (5, CURRENT_TIMESTAMP, 2, 'README.md', 'MODIFIED', 20, 5);

-- PR3 파일 (2024-02-15): Application.java(MODIFIED), Config.java(MODIFIED)
INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (6, CURRENT_TIMESTAMP, 3, 'src/main/java/Application.java', 'MODIFIED', 100, 40);

INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (7, CURRENT_TIMESTAMP, 3, 'src/main/java/Config.java', 'MODIFIED', 80, 30);

-- PR4 파일 (2024-03-01): Application.java(MODIFIED), Service.java(ADDED)
INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (8, CURRENT_TIMESTAMP, 4, 'src/main/java/Application.java', 'MODIFIED', 40, 20);

INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (9, CURRENT_TIMESTAMP, 4, 'src/main/java/Service.java', 'ADDED', 80, 0);

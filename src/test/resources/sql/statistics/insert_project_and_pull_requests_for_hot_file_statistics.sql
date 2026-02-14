INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 7);

INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at)
VALUES (1, CURRENT_TIMESTAMP, 1001, 1, 'author1', 1, 10, 'sha001', '버그 수정 PR', 'OPEN', 'https://github.com/test/repo/pull/10', 3, 100, 40, 2, '2024-01-15 10:00:00');

INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at, github_merged_at, github_closed_at)
VALUES (2, CURRENT_TIMESTAMP, 1002, 1, 'author2', 2, 20, 'sha002', '기능 개발 PR', 'MERGED', 'https://github.com/test/repo/pull/20', 4, 200, 60, 4, '2024-01-20 09:00:00', '2024-01-22 15:00:00', '2024-01-22 15:00:00');

INSERT INTO pull_requests (id, created_at, github_pull_request_id, project_id, user_name, user_id, pull_request_number, head_commit_sha, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, github_created_at, github_merged_at, github_closed_at)
VALUES (3, CURRENT_TIMESTAMP, 1003, 1, 'author1', 1, 30, 'sha003', '리팩토링 PR', 'MERGED', 'https://github.com/test/repo/pull/30', 2, 150, 80, 3, '2024-02-01 14:00:00', '2024-02-02 10:00:00', '2024-02-02 10:00:00');

-- PR1 파일: Application.java(MODIFIED), README.md(MODIFIED), Config.java(ADDED)
INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (1, CURRENT_TIMESTAMP, 1, 'src/main/java/Application.java', 'MODIFIED', 30, 10);

INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (2, CURRENT_TIMESTAMP, 1, 'README.md', 'MODIFIED', 20, 5);

INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (3, CURRENT_TIMESTAMP, 1, 'src/main/java/Config.java', 'ADDED', 50, 0);

-- PR2 파일: Application.java(MODIFIED), Config.java(MODIFIED), Service.java(ADDED), OldFile.java(REMOVED)
INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (4, CURRENT_TIMESTAMP, 2, 'src/main/java/Application.java', 'MODIFIED', 40, 15);

INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (5, CURRENT_TIMESTAMP, 2, 'src/main/java/Config.java', 'MODIFIED', 25, 10);

INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (6, CURRENT_TIMESTAMP, 2, 'src/main/java/Service.java', 'ADDED', 80, 0);

INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (7, CURRENT_TIMESTAMP, 2, 'src/main/java/OldFile.java', 'REMOVED', 0, 35);

-- PR3 파일: Application.java(MODIFIED), Service.java(RENAMED)
INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (8, CURRENT_TIMESTAMP, 3, 'src/main/java/Application.java', 'MODIFIED', 60, 20);

INSERT INTO pull_request_files (id, created_at, pull_request_id, file_name, change_type, additions, deletions)
VALUES (9, CURRENT_TIMESTAMP, 3, 'src/main/java/Service.java', 'RENAMED', 10, 5);

INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 7);

INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at)
VALUES (1, CURRENT_TIMESTAMP, 1, 'author1', 10, '버그 수정 PR', 'OPEN', 'https://github.com/test/repo/pull/10', 2, 50, 20, 1, '2024-01-15 10:00:00');

INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (2, CURRENT_TIMESTAMP, 1, 'author2', 20, '기능 개발 PR', 'MERGED', 'https://github.com/test/repo/pull/20', 8, 400, 100, 5, '2024-01-10 09:00:00', '2024-01-12 15:00:00', '2024-01-12 15:00:00');

INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (3, CURRENT_TIMESTAMP, 1, 'author1', 30, '두 번째 버그 수정', 'MERGED', 'https://github.com/test/repo/pull/30', 4, 150, 60, 3, '2024-01-20 14:00:00', '2024-01-21 10:00:00', '2024-01-21 10:00:00');

INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (4, CURRENT_TIMESTAMP, 1, 'author2', 40, '리팩토링 PR', 'MERGED', 'https://github.com/test/repo/pull/40', 6, 200, 180, 2, '2024-02-01 08:00:00', '2024-02-02 12:00:00', '2024-02-02 12:00:00');

INSERT INTO pull_request_labels (id, created_at, pull_request_id, label_name, labeled_at)
VALUES (1, CURRENT_TIMESTAMP, 1, 'bug', '2024-01-15 10:00:00');

INSERT INTO pull_request_labels (id, created_at, pull_request_id, label_name, labeled_at)
VALUES (2, CURRENT_TIMESTAMP, 2, 'feature', '2024-01-10 09:00:00');

INSERT INTO pull_request_labels (id, created_at, pull_request_id, label_name, labeled_at)
VALUES (3, CURRENT_TIMESTAMP, 3, 'bug', '2024-01-20 14:00:00');

INSERT INTO pull_request_labels (id, created_at, pull_request_id, label_name, labeled_at)
VALUES (4, CURRENT_TIMESTAMP, 4, 'refactor', '2024-02-01 08:00:00');

INSERT INTO pull_request_labels (id, created_at, pull_request_id, label_name, labeled_at)
VALUES (5, CURRENT_TIMESTAMP, 2, 'enhancement', '2024-01-10 09:30:00');

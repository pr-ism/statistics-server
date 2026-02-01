INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 7);

-- 2024-01-01 주 (월요일 2024-01-01): 2건
-- PR1: 2024-01-02 (화), additions=100, deletions=50 → 변경량 150
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at)
VALUES (1, CURRENT_TIMESTAMP, 1, 'author1', 10, 'Feature A', 'OPEN', 'https://github.com/test/repo/pull/10', 3, 100, 50, 2, '2024-01-02 10:00:00');

-- PR2: 2024-01-05 (금), additions=200, deletions=100 → 변경량 300
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at)
VALUES (2, CURRENT_TIMESTAMP, 1, 'author2', 20, 'Feature B', 'OPEN', 'https://github.com/test/repo/pull/20', 5, 200, 100, 3, '2024-01-05 14:00:00');

-- 2024-01-08 주 (월요일 2024-01-08): 0건 (빈 기간)

-- 2024-01-15 주 (월요일 2024-01-15): 3건
-- PR3: 2024-01-15 (월), additions=50, deletions=30 → 변경량 80
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (3, CURRENT_TIMESTAMP, 1, 'author1', 30, 'Bugfix C', 'MERGED', 'https://github.com/test/repo/pull/30', 2, 50, 30, 1, '2024-01-15 09:00:00', '2024-01-16 10:00:00', '2024-01-16 10:00:00');

-- PR4: 2024-01-17 (수), additions=400, deletions=200 → 변경량 600
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (4, CURRENT_TIMESTAMP, 1, 'author2', 40, 'Feature D', 'MERGED', 'https://github.com/test/repo/pull/40', 8, 400, 200, 5, '2024-01-17 11:00:00', '2024-01-18 15:00:00', '2024-01-18 15:00:00');

-- PR5: 2024-01-19 (금), additions=120, deletions=0 → 변경량 120
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (5, CURRENT_TIMESTAMP, 1, 'author1', 50, 'Feature E', 'MERGED', 'https://github.com/test/repo/pull/50', 4, 120, 0, 2, '2024-01-19 16:00:00', '2024-01-20 10:00:00', '2024-01-20 10:00:00');

-- 2024-01-22 주 (월요일 2024-01-22): 1건
-- PR6: 2024-01-24 (수), additions=500, deletions=500 → 변경량 1000
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (6, CURRENT_TIMESTAMP, 1, 'author2', 60, 'Refactor F', 'MERGED', 'https://github.com/test/repo/pull/60', 15, 500, 500, 8, '2024-01-24 08:00:00', '2024-01-26 12:00:00', '2024-01-26 12:00:00');

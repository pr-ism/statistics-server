INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 7);

-- SMALL: additions + deletions = 30 + 10 = 40
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at)
VALUES (1, CURRENT_TIMESTAMP, 1, 'author1', 10, 'Small PR 1', 'OPEN', 'https://github.com/test/repo/pull/10', 1, 30, 10, 1, '2024-01-15 10:00:00');

-- SMALL: additions + deletions = 60 + 40 = 100 (경계값)
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at)
VALUES (2, CURRENT_TIMESTAMP, 1, 'author2', 20, 'Small PR 2', 'OPEN', 'https://github.com/test/repo/pull/20', 2, 60, 40, 2, '2024-01-16 10:00:00');

-- MEDIUM: additions + deletions = 120 + 30 = 150
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (3, CURRENT_TIMESTAMP, 1, 'author1', 30, 'Medium PR 1', 'MERGED', 'https://github.com/test/repo/pull/30', 4, 120, 30, 3, '2024-01-20 14:00:00', '2024-01-21 10:00:00', '2024-01-21 10:00:00');

-- MEDIUM: additions + deletions = 200 + 100 = 300 (경계값)
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (4, CURRENT_TIMESTAMP, 1, 'author2', 40, 'Medium PR 2', 'MERGED', 'https://github.com/test/repo/pull/40', 6, 200, 100, 4, '2024-02-01 08:00:00', '2024-02-02 12:00:00', '2024-02-02 12:00:00');

-- MEDIUM: additions + deletions = 150 + 51 = 201
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (5, CURRENT_TIMESTAMP, 1, 'author1', 50, 'Medium PR 3', 'MERGED', 'https://github.com/test/repo/pull/50', 5, 150, 51, 5, '2024-02-10 09:00:00', '2024-02-11 12:00:00', '2024-02-11 12:00:00');

-- LARGE: additions + deletions = 400 + 200 = 600
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (6, CURRENT_TIMESTAMP, 1, 'author2', 60, 'Large PR 1', 'MERGED', 'https://github.com/test/repo/pull/60', 12, 400, 200, 8, '2024-02-15 08:00:00', '2024-02-17 12:00:00', '2024-02-17 12:00:00');

-- LARGE: additions + deletions = 500 + 200 = 700 (경계값)
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (7, CURRENT_TIMESTAMP, 1, 'author1', 70, 'Large PR 2', 'MERGED', 'https://github.com/test/repo/pull/70', 14, 500, 200, 10, '2024-03-01 08:00:00', '2024-03-03 12:00:00', '2024-03-03 12:00:00');

-- EXTRA_LARGE: additions + deletions = 600 + 200 = 800
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (8, CURRENT_TIMESTAMP, 1, 'author2', 80, 'XL PR 1', 'MERGED', 'https://github.com/test/repo/pull/80', 20, 600, 200, 12, '2024-03-10 08:00:00', '2024-03-12 12:00:00', '2024-03-12 12:00:00');

-- EXTRA_LARGE: additions + deletions = 1000 + 500 = 1500
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (9, CURRENT_TIMESTAMP, 1, 'author1', 90, 'XL PR 2', 'MERGED', 'https://github.com/test/repo/pull/90', 30, 1000, 500, 18, '2024-03-15 08:00:00', '2024-03-17 12:00:00', '2024-03-17 12:00:00');

-- EXTRA_LARGE: additions + deletions = 800 + 300 = 1100
INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (10, CURRENT_TIMESTAMP, 1, 'author2', 100, 'XL PR 3', 'MERGED', 'https://github.com/test/repo/pull/100', 25, 800, 300, 15, '2024-03-20 08:00:00', '2024-03-22 12:00:00', '2024-03-22 12:00:00');

INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 7);

INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at)
VALUES (1, CURRENT_TIMESTAMP, 1, 'author1', 10, '첫 번째 PR', 'OPEN', 'https://github.com/test/repo/pull/10', 3, 100, 40, 2, '2024-01-15 10:00:00');

INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (2, CURRENT_TIMESTAMP, 1, 'author2', 20, '두 번째 PR', 'MERGED', 'https://github.com/test/repo/pull/20', 5, 200, 60, 4, '2024-01-10 09:00:00', '2024-01-12 15:00:00', '2024-01-12 15:00:00');

INSERT INTO pull_requests (id, created_at, project_id, author_github_id, pull_request_number, title, state, link, changed_file_count, addition_count, deletion_count, commit_count, pull_request_created_at, merged_at, closed_at)
VALUES (3, CURRENT_TIMESTAMP, 1, 'author1', 30, '세 번째 PR', 'MERGED', 'https://github.com/test/repo/pull/30', 2, 50, 10, 1, '2024-01-20 14:00:00', '2024-01-21 10:00:00', '2024-01-21 10:00:00');

INSERT INTO requested_reviewers (id, created_at, pull_request_id, github_mention, github_uid, requested_at)
VALUES (1, CURRENT_TIMESTAMP, 1, 'reviewer1', 1001, '2024-01-15 11:00:00');

INSERT INTO requested_reviewers (id, created_at, pull_request_id, github_mention, github_uid, requested_at)
VALUES (2, CURRENT_TIMESTAMP, 2, 'reviewer1', 1001, '2024-01-10 10:00:00');

INSERT INTO requested_reviewers (id, created_at, pull_request_id, github_mention, github_uid, requested_at)
VALUES (3, CURRENT_TIMESTAMP, 2, 'reviewer2', 1002, '2024-01-10 10:00:00');

INSERT INTO requested_reviewers (id, created_at, pull_request_id, github_mention, github_uid, requested_at)
VALUES (4, CURRENT_TIMESTAMP, 3, 'reviewer2', 1002, '2024-01-20 15:00:00');

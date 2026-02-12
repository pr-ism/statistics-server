INSERT INTO projects (id, created_at, updated_at, name, api_key, user_id)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '테스트 프로젝트', 'test-api-key', 1);

INSERT INTO review_comments (id, created_at, review_id, github_comment_id, github_review_id, body, path, start_line, end_line, side, commit_sha, parent_comment_id, user_name, user_id, github_created_at, github_updated_at, deleted)
VALUES (1, CURRENT_TIMESTAMP, NULL, 100, 200, '원본 댓글 내용', 'src/main/java/Example.java', NULL, 10, 'RIGHT', 'abc123sha', NULL, 'reviewer1', 12345, '2024-01-15 10:00:00', '2024-01-15 10:00:00', false);

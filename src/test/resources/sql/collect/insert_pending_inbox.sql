INSERT INTO collect_inbox (id, created_at, updated_at, collect_type, project_id, run_id, payload_json, status, processing_attempt)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PULL_REQUEST_OPENED', 1, 10, '{}', 'PENDING', 0);

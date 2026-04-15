INSERT INTO collect_inbox (id, created_at, updated_at, collect_type, project_id, run_id, payload_json, status, processing_attempt, failed_at, failure_reason)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PULL_REQUEST_OPENED', 1, 20, '{}', 'FAILED', 1, '2026-03-16T00:02:00Z', '실패');

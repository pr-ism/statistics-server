INSERT INTO projects (id, name, api_key, user_id, created_at, updated_at)
VALUES (1, '설정 테스트 프로젝트', 'api-key-setting', 7, NOW(), NOW());

INSERT INTO project_core_time_settings (project_id, start_time, end_time, created_at, updated_at)
VALUES (1, '10:00:00', '18:00:00', NOW(), NOW());

INSERT INTO project_size_weight_settings (project_id, addition_weight, deletion_weight, file_weight, created_at, updated_at)
VALUES (1, 1.000000, 1.000000, 1.000000, NOW(), NOW());

INSERT INTO project_size_grade_threshold_settings (project_id, s_threshold, m_threshold, l_threshold, xl_threshold, created_at, updated_at)
VALUES (1, 10, 100, 300, 1000, NOW(), NOW());

-- =====================================================
-- MySQL Schema for statistics-server
-- Engine: InnoDB, Charset: utf8mb4
-- =====================================================

-- 1. users
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nickname_value VARCHAR(255),
    state VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. user_identities
CREATE TABLE user_identities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    registration_id VARCHAR(255),
    social_id VARCHAR(255),
    UNIQUE KEY uq_user_identities_social (registration_id, social_id),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. projects
CREATE TABLE projects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    repository_url VARCHAR(255),
    api_key VARCHAR(255),
    user_id BIGINT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. project_size_weight_settings
CREATE TABLE project_size_weight_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT,
    addition_weight DECIMAL(19,6),
    deletion_weight DECIMAL(19,6),
    file_weight DECIMAL(19,6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Pull Request Metadata
-- =====================================================

-- 5. pull_requests
CREATE TABLE pull_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    github_pull_request_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    user_name VARCHAR(255),
    user_id BIGINT,
    pull_request_number INT,
    head_commit_sha VARCHAR(255),
    title VARCHAR(255),
    state VARCHAR(50),
    link VARCHAR(500),
    changed_file_count INT,
    addition_count INT,
    deletion_count INT,
    commit_count INT,
    pull_request_created_at DATETIME(6),
    merged_at DATETIME(6),
    closed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    UNIQUE KEY uq_pull_requests_github_pull_request_id (github_pull_request_id),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. pull_request_files
CREATE TABLE pull_request_files (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT NOT NULL,
    file_name VARCHAR(500),
    change_type VARCHAR(50),
    additions INT,
    deletions INT,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. pull_request_labels
CREATE TABLE pull_request_labels (
    id BIGINT NOT NULL AUTO_INCREMENT,
    github_pull_request_id BIGINT NOT NULL,
    pull_request_id BIGINT,
    head_commit_sha VARCHAR(255),
    label_name VARCHAR(255),
    labeled_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    UNIQUE KEY uq_pull_request_labels (github_pull_request_id, label_name),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. commits
CREATE TABLE commits (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT NOT NULL,
    commit_sha VARCHAR(255),
    committed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Pull Request History
-- =====================================================

-- 9. pull_request_content_histories
CREATE TABLE pull_request_content_histories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT NOT NULL,
    head_commit_sha VARCHAR(255),
    changed_file_count INT,
    addition_count INT,
    deletion_count INT,
    commit_count INT,
    changed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. pull_request_file_histories
CREATE TABLE pull_request_file_histories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT NOT NULL,
    head_commit_sha VARCHAR(255),
    file_name VARCHAR(500),
    previous_file_name VARCHAR(500),
    change_type VARCHAR(50),
    additions INT,
    deletions INT,
    changed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. pull_request_state_histories
CREATE TABLE pull_request_state_histories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT NOT NULL,
    head_commit_sha VARCHAR(255),
    previous_state VARCHAR(50),
    new_state VARCHAR(50),
    changed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. pull_request_label_histories
CREATE TABLE pull_request_label_histories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    github_pull_request_id BIGINT NOT NULL,
    pull_request_id BIGINT,
    head_commit_sha VARCHAR(255),
    label_name VARCHAR(255),
    action VARCHAR(50),
    changed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Review Metadata
-- =====================================================

-- 13. reviews
CREATE TABLE reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT,
    github_pull_request_id BIGINT NOT NULL,
    github_review_id BIGINT NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    review_state VARCHAR(50) NOT NULL,
    head_commit_sha VARCHAR(255) NOT NULL,
    body TEXT,
    comment_count INT NOT NULL,
    submitted_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    UNIQUE KEY uq_reviews_github_review_id (github_review_id),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. review_comments
CREATE TABLE review_comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    review_id BIGINT,
    github_comment_id BIGINT NOT NULL,
    github_review_id BIGINT NOT NULL,
    body TEXT NOT NULL,
    path VARCHAR(500) NOT NULL,
    start_line INT,
    end_line INT NOT NULL,
    side VARCHAR(50) NOT NULL,
    commit_sha VARCHAR(255) NOT NULL,
    parent_comment_id BIGINT,
    user_name VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    github_created_at DATETIME(6) NOT NULL,
    github_updated_at DATETIME(6) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    UNIQUE KEY uq_review_comments_github_comment_id (github_comment_id),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. requested_reviewers
CREATE TABLE requested_reviewers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT,
    github_pull_request_id BIGINT NOT NULL,
    head_commit_sha VARCHAR(255),
    user_name VARCHAR(255),
    user_id BIGINT,
    requested_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    UNIQUE KEY uq_requested_reviewers (github_pull_request_id, user_id),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. requested_reviewer_histories
CREATE TABLE requested_reviewer_histories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT,
    github_pull_request_id BIGINT NOT NULL,
    head_commit_sha VARCHAR(255),
    user_name VARCHAR(255),
    user_id BIGINT,
    action VARCHAR(50),
    changed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Insight - Lifecycle
-- =====================================================

-- 17. pull_request_lifecycles
CREATE TABLE pull_request_lifecycles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT,
    review_ready_at DATETIME(6),
    time_to_merge_minutes BIGINT,
    total_lifespan_minutes BIGINT,
    active_work_minutes BIGINT,
    state_change_count INT,
    reopened BIT(1),
    closed_without_review BIT(1),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Insight - Size
-- =====================================================

-- 18. pull_request_sizes
CREATE TABLE pull_request_sizes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT,
    size_score DECIMAL(19,6),
    addition_weight DECIMAL(19,6),
    deletion_weight DECIMAL(19,6),
    file_weight DECIMAL(19,6),
    size_grade VARCHAR(50),
    file_change_diversity DECIMAL(19,6),
    addition_count INT,
    deletion_count INT,
    changed_file_count INT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Insight - Bottleneck
-- =====================================================

-- 19. pull_request_bottlenecks
CREATE TABLE pull_request_bottlenecks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT,
    review_wait_minutes BIGINT,
    review_progress_minutes BIGINT,
    merge_wait_minutes BIGINT,
    first_review_at DATETIME(6),
    last_review_at DATETIME(6),
    last_approve_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Insight - Activity
-- =====================================================

-- 20. review_activities
CREATE TABLE review_activities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT,
    review_round_trips INT,
    total_comment_count INT,
    comment_density DECIMAL(19,6),
    code_additions_after_review INT,
    code_deletions_after_review INT,
    has_additional_reviewers BIT(1),
    additional_reviewer_count INT,
    total_additions INT,
    total_deletions INT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Insight - Comment
-- =====================================================

-- 21. comment_analyses
CREATE TABLE comment_analyses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    review_comment_id BIGINT,
    pull_request_id BIGINT,
    comment_length INT,
    line_count INT,
    user_names VARCHAR(255),
    count INT,
    has_code BIT(1),
    has_url BIT(1),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Insight - Review Session & Response Time
-- =====================================================

-- 22. review_sessions
CREATE TABLE review_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT,
    reviewer_name VARCHAR(255),
    reviewer_github_id BIGINT,
    first_activity_at DATETIME(6),
    last_activity_at DATETIME(6),
    session_duration_minutes BIGINT,
    review_count INT,
    comment_count INT,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 23. review_response_times
CREATE TABLE review_response_times (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT,
    response_after_review_minutes BIGINT,
    changes_resolution_minutes BIGINT,
    last_changes_requested_at DATETIME(6),
    first_commit_after_changes_at DATETIME(6),
    first_approve_after_changes_at DATETIME(6),
    changes_requested_count INT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Insight - Opened PR Metrics
-- =====================================================

-- 24. pull_request_opened_change_summaries
CREATE TABLE pull_request_opened_change_summaries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT NOT NULL,
    total_changes INT NOT NULL,
    avg_changes_per_file DECIMAL(19,4) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 25. pull_request_opened_commit_densities
CREATE TABLE pull_request_opened_commit_densities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT NOT NULL,
    commit_density_per_file DECIMAL(19,4) NOT NULL,
    commit_density_per_change DECIMAL(19,6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 26. pull_request_opened_file_change_diversities
CREATE TABLE pull_request_opened_file_change_diversities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pull_request_id BIGINT NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    count INT NOT NULL,
    ratio DECIMAL(19,2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

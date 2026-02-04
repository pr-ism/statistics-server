SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE review_comments;
TRUNCATE TABLE reviews;
TRUNCATE TABLE requested_reviewer_histories;
TRUNCATE TABLE requested_reviewers;
TRUNCATE TABLE pull_request_label_histories;
TRUNCATE TABLE pull_request_labels;
TRUNCATE TABLE pull_request_file_histories;
TRUNCATE TABLE pull_request_content_histories;
TRUNCATE TABLE pull_request_state_histories;
TRUNCATE TABLE commits;
TRUNCATE TABLE pull_request_files;
TRUNCATE TABLE pull_requests;
TRUNCATE TABLE user_identities;
TRUNCATE TABLE users;
TRUNCATE TABLE projects;
TRUNCATE TABLE pull_request_opened_commit_densities;
TRUNCATE TABLE pull_request_opened_change_summaries;
TRUNCATE TABLE pull_request_opened_file_change_diversities;

SET REFERENTIAL_INTEGRITY TRUE;

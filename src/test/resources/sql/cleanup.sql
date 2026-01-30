SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE requested_reviewer_change_histories;
TRUNCATE TABLE requested_reviewers;
TRUNCATE TABLE pr_label_histories;
TRUNCATE TABLE pr_labels;
TRUNCATE TABLE pr_file_histories;
TRUNCATE TABLE pr_change_histories;
TRUNCATE TABLE pr_state_change_histories;
TRUNCATE TABLE commits;
TRUNCATE TABLE pr_files;
TRUNCATE TABLE pull_requests;
TRUNCATE TABLE user_identities;
TRUNCATE TABLE users;
TRUNCATE TABLE projects;

SET REFERENTIAL_INTEGRITY TRUE;

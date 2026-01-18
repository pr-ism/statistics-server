INSERT INTO users (id, created_at, updated_at, nickname_value, state)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '섬세한 보라', 'ACTIVE');

INSERT INTO user_identities (id, user_id, registration_id, social_id)
VALUES (1, 1, 'KAKAO', 'social-existing');

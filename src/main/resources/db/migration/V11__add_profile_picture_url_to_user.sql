ALTER TABLE app_users
    ADD COLUMN profile_picture_url VARCHAR(512);

ALTER TABLE app_users_aud
    ADD COLUMN profile_picture_url VARCHAR(512);
ALTER TABLE app_users
    ADD CONSTRAINT uc_app_users_phonenumber UNIQUE (phone_number);
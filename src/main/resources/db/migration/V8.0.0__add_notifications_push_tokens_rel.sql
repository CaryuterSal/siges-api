CREATE TABLE notifications_sent_to_tokens
(
    sent_to_push_tokens_id BIGINT       NOT NULL,
    sent_to_tokens_token   VARCHAR(255) NOT NULL
);

CREATE TABLE notifications_sent_to_tokens_aud
(
    rev                    INTEGER      NOT NULL,
    revtype                SMALLINT,
    sent_to_push_tokens_id BIGINT       NOT NULL,
    sent_to_tokens_token   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_notifications_sent_to_tokens_aud PRIMARY KEY (rev, sent_to_push_tokens_id, sent_to_tokens_token)
);

ALTER TABLE notifications
    ADD read_status VARCHAR(255);

ALTER TABLE notifications
    ADD type VARCHAR(255);

ALTER TABLE notifications
    ALTER COLUMN read_status SET NOT NULL;

ALTER TABLE notifications_aud
    ADD read_status VARCHAR(255);

ALTER TABLE notifications_aud
    ADD type VARCHAR(255);

ALTER TABLE notifications
    ALTER COLUMN type SET NOT NULL;

ALTER TABLE notifications_sent_to_tokens_aud
    ADD CONSTRAINT fk_notifications_sent_to_tokens_aud_on_rev FOREIGN KEY (rev) REFERENCES custom_revision_entity (id);

ALTER TABLE notifications_sent_to_tokens
    ADD CONSTRAINT fk_notsentotok_on_notification FOREIGN KEY (sent_to_push_tokens_id) REFERENCES notifications (id);

ALTER TABLE notifications_sent_to_tokens
    ADD CONSTRAINT fk_notsentotok_on_push_token FOREIGN KEY (sent_to_tokens_token) REFERENCES push_tokens (token);

ALTER TABLE push_tokens
    DROP COLUMN platform;

ALTER TABLE push_tokens
    ADD platform VARCHAR(255) NOT NULL;

ALTER TABLE push_tokens_aud
    DROP COLUMN platform;

ALTER TABLE push_tokens_aud
    ADD platform VARCHAR(255);
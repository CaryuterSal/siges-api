CREATE TABLE notifications_metadata
(
    notification_id BIGINT       NOT NULL,
    metadata        VARCHAR(255),
    metadata_key    VARCHAR(255) NOT NULL,
    CONSTRAINT pk_notifications_metadata PRIMARY KEY (notification_id, metadata_key)
);

CREATE TABLE notifications_metadata_aud
(
    rev             INTEGER      NOT NULL,
    notification_id BIGINT       NOT NULL,
    metadata        VARCHAR(255) NOT NULL,
    metadata_key    VARCHAR(255) NOT NULL,
    revtype         SMALLINT,
    CONSTRAINT pk_notifications_metadata_aud PRIMARY KEY (rev, notification_id, metadata)
);

ALTER TABLE notifications_metadata_aud
    ADD CONSTRAINT fk_notifications_metadata_aud_on_rev FOREIGN KEY (rev) REFERENCES custom_revision_entity (id);

ALTER TABLE notifications_metadata
    ADD CONSTRAINT fk_notifications_metadata_on_notification FOREIGN KEY (notification_id) REFERENCES notifications (id);
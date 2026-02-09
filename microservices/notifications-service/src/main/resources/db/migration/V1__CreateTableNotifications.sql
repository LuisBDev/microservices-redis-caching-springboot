CREATE TABLE notifications
(
    id      VARCHAR(255) NOT NULL,
    user_id BIGINT       NOT NULL,
    channel VARCHAR(255) NOT NULL,
    message VARCHAR(255) NOT NULL,
    sent_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id)
);
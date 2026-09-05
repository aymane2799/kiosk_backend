CREATE TABLE favorites (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    content_id VARCHAR(36) NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_favorites_content FOREIGN KEY (content_id) REFERENCES content (id),
    CONSTRAINT fk_favorites_user_content UNIQUE  (user_id, content_id)
);
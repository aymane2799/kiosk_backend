CREATE TABLE content (
    id VARCHAR(36) NOT NULL PRIMARY KEY ,
    title VARCHAR(255) NOT NULL,
    excerpt VARCHAR(1000),
    body TEXT,
    category VARCHAR(20),
    tier VARCHAR(20),

    published_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_content_tier ON content (tier);
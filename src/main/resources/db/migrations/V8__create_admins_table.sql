CREATE TABLE admins (
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_admin_email UNIQUE (email)
);

-- We removed ADMIN role from AppUser (users table)
UPDATE users SET role = 'USER' WHERE role = 'ADMIN';
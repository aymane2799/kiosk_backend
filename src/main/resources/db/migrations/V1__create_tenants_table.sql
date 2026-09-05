CREATE TABLE tenants (
    id VARCHAR(36) NOT NULL PRIMARY KEY ,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    logo_url VARCHAR(500) NOT NULL,
    primary_color VARCHAR(7),
    secondary_color VARCHAR(7),
    provider_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_tenants_name UNIQUE (name),
    CONSTRAINT uk_tenants_slug UNIQUE (slug)

)
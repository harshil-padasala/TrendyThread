-- Free-form post tags: new tag table + post_tag join table
-- (see com.trendythread.app.entities.Tag and Post.tags).
--
-- NOT auto-applied by Spring. Dev uses `ddl-auto: create-drop`, which creates these
-- tables automatically. Prod uses `ddl-auto: none` (see application-prod.yml), so
-- this must be run manually against the prod database once.

CREATE TABLE IF NOT EXISTS tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT uq_tag_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS post_tag (
    post_id INTEGER NOT NULL REFERENCES post (id),
    tag_id INTEGER NOT NULL REFERENCES tag (id),
    PRIMARY KEY (post_id, tag_id)
);

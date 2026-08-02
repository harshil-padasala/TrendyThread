-- Post likes feature: new post_like table (see com.trendythread.app.entities.PostLike).
--
-- NOT auto-applied by Spring. Dev uses `ddl-auto: create-drop`, which creates this
-- table automatically. Prod uses `ddl-auto: none` (see application-prod.yml), so
-- this must be run manually against the prod database once.

CREATE TABLE IF NOT EXISTS post_like (
    id BIGSERIAL PRIMARY KEY,
    post_id INTEGER NOT NULL REFERENCES post (id),
    blogger_id INTEGER NOT NULL REFERENCES blogger (id),
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT uq_post_like_post_blogger UNIQUE (post_id, blogger_id)
);

CREATE INDEX IF NOT EXISTS idx_post_like_post_id ON post_like (post_id);

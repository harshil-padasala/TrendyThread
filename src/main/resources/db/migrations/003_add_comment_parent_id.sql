-- Nested comment replies: adds a self-referencing parent_comment_id column
-- (see com.trendythread.app.entities.Comment). NULL means a top-level comment.
--
-- NOT auto-applied by Spring. Dev uses `ddl-auto: create-drop`, which creates this
-- column automatically. Prod uses `ddl-auto: none` (see application-prod.yml), so
-- this must be run manually against the prod database once.

ALTER TABLE comment
    ADD COLUMN IF NOT EXISTS parent_comment_id INTEGER REFERENCES comment (id);

CREATE INDEX IF NOT EXISTS idx_comment_parent_comment_id ON comment (parent_comment_id);

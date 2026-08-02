-- Featured-categories feature: adds the columns the Category entity expects
-- (see com.trendythread.app.entities.Category).
--
-- NOT auto-applied by Spring. Dev uses `spring.jpa.hibernate.ddl-auto: create-drop`,
-- which creates these columns automatically on every startup. Prod uses `ddl-auto: none`
-- (see application-prod.yml), so this script must be run manually against the prod
-- database once. It's committed here so the schema change has an in-repo record
-- instead of living only as tribal knowledge.
--
-- Safe to run once; re-running will fail on the duplicate-column check unless the
-- IF NOT EXISTS guards below are supported by your Postgres version (9.6+).

ALTER TABLE category
    ADD COLUMN IF NOT EXISTS featured       BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS display_order  INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS post_count     INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS auto_suggested BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_category_featured ON category (featured, display_order);
CREATE INDEX IF NOT EXISTS idx_category_post_count ON category (post_count DESC);

-- Optional one-time backfill: recompute post_count from existing posts instead of
-- trusting the default of 0. Safe to skip — the app's admin
-- "recalculate-post-counts" endpoint (AdminCategoryController) does the same thing
-- and can be run instead of this statement.
UPDATE category c
SET post_count = (SELECT COUNT(*) FROM post p WHERE p.category_id = c.id);

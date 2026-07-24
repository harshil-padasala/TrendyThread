package com.trendythread.app.constants;

import java.time.Duration;

public interface IRedisTtlConstant {

    /** Single-entity lookups by id/email — small, bounded key count, safe to cache longer. */
    Duration TTL_ENTITY = Duration.ofHours(6);

    /** Aggregate/collection caches (ALL bloggers, ALL categories, featured list) — bounded key count but must not go stale for long. */
    Duration TTL_COLLECTION = Duration.ofMinutes(15);

    /** Parameterized query results (pagination, keyword search, per-category/blogger/user post lists, comment lists) — unbounded key cardinality, keep short. */
    Duration TTL_QUERY = Duration.ofMinutes(5);
}

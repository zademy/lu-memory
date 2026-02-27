package com.zademy.lu_memory.services.search;

/**
 * Strategy contract for transforming a raw user query into an FTS-compatible
 * MATCH query.
 */
public interface MemorySearchQueryStrategy {

    /**
     * Builds the final query string used by SQLite FTS MATCH.
     *
     * @param rawQuery user-provided query text
     * @return transformed query ready for MATCH clause
     */
    String buildMatchQuery(String rawQuery);
}

package com.zademy.lu_memory.services.search;

import com.zademy.lu_memory.utils.SearchQueryUtils;
import com.zademy.lu_memory.utils.TextProcessingUtils;
import org.springframework.stereotype.Component;

/**
 * Advanced strategy: enriches the query to improve FTS recall.
 */
@Component
public class AdvancedMemorySearchQueryStrategy implements MemorySearchQueryStrategy {

    @Override
    public String buildMatchQuery(String rawQuery) {
        if (TextProcessingUtils.normalize(rawQuery) == null) {
            return "";
        }
        return SearchQueryUtils.enhanceFtsQuery(rawQuery);
    }
}

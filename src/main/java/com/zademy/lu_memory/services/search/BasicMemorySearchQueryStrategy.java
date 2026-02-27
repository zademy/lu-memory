package com.zademy.lu_memory.services.search;

import com.zademy.lu_memory.utils.TextProcessingUtils;
import org.springframework.stereotype.Component;

/**
 * Basic strategy: uses the query as-is after normalization.
 */
@Component
public class BasicMemorySearchQueryStrategy implements MemorySearchQueryStrategy {

    @Override
    public String buildMatchQuery(String rawQuery) {
        return TextProcessingUtils.normalize(rawQuery) == null ? "" : rawQuery.trim();
    }
}

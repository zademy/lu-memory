package com.zademy.lu_memory.utils;

import java.util.Locale;

/**
 * Utility class for search query processing and enhancement.
 * 
 * <p>This class provides utilities for optimizing and enhancing search queries,
 * particularly for SQLite FTS5 full-text search operations. It follows the
 * <b>Strategy Pattern</b> by providing different query enhancement strategies
 * based on query complexity.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>FTS5 query enhancement with boolean operators</li>
 *   <li>Query syntax optimization for better search performance</li>
   *   <li>Fallback search strategy implementation</li>
 * </ul>
 * 
 * @author lu-memory team
 * @since 1.0.0
 */
public final class SearchQueryUtils {

    private SearchQueryUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Enhances a plain query with FTS5 boolean operators for better search performance.
     * 
     * <p>This method applies query optimization strategies:</p>
     * <ul>
     *   <li>If no FTS operators are present, converts words to OR conditions</li>
     *   <li>Preserves existing boolean operators (AND, OR, NOT)</li>
     *   <li>Maintains quoted phrases for exact matching</li>
     * </ul>
     * 
     * @param query The raw query string to enhance
     * @return An optimized FTS5 query with boolean operators
     */
    public static String enhanceFtsQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        // Enhance FTS query with advanced operators
        String enhanced = query.trim();

        // If it does not contain FTS operators, add prefix search
        if (!containsFtsOperators(enhanced)) {
            String[] words = enhanced.split("\\s+");
            enhanced = String.join(" OR ", words);
        }

        return enhanced;
    }

    /**
     * Checks if a query contains FTS5 boolean operators.
     * 
     * @param query The query string to check
     * @return true if the query contains FTS operators, false otherwise
     */
    public static boolean containsFtsOperators(String query) {
        if (query == null) {
            return false;
        }
        
        String upperQuery = query.toUpperCase(Locale.ROOT);
        return upperQuery.contains("AND") || 
               upperQuery.contains("OR") || 
               upperQuery.contains("NOT") ||
               upperQuery.contains("\"");
    }

    /**
     * Validates if a search query is suitable for processing.
     * 
     * @param query The query to validate
     * @return true if the query is valid for search, false otherwise
     */
    public static boolean isValidSearchQuery(String query) {
        return query != null && !query.isBlank();
    }

    /**
     * Prepares a search term for SQL LIKE operations.
     * 
     * @param searchTerm The raw search term
     * @return The term wrapped with wildcards for LIKE operations
     */
    public static String prepareLikeSearchTerm(String searchTerm) {
        if (searchTerm == null) {
            return "%";
        }
        return "%" + searchTerm.trim() + "%";
    }
}

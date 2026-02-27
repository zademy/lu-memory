package com.zademy.lu_memory.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Utility class for text processing and string manipulation operations.
 * 
 * <p>This class provides common text processing utilities used across the memory system,
 * following the <b>Utility Pattern</b> with static methods for stateless operations.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>String normalization and sanitization</li>
 *   <li>URL-safe slug generation</li>
 *   <li>Cryptographic hash computation</li>
 *   <li>Content snippet generation</li>
 * </ul>
 * 
 * @author lu-memory team
 * @since 1.0.0
 */
public final class TextProcessingUtils {

    private TextProcessingUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Computes a SHA-256 hash of the given input string.
     * Used for content de-duplication and version tracking.
     *
     * @param input The raw string content to hash
     * @return A hexadecimal representation of the SHA-256 hash, or fallback hash if algorithm unavailable
     */
    public static String computeHash(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash if SHA-256 not available
            return String.valueOf(input.hashCode());
        }
    }

    /**
     * Transforms raw strings into URL-safe, lowercase slugs for consistent topic tracking.
     * 
     * <p>This method:
     * <ul>
     *   <li>Converts to lowercase</li>
     *   <li>Replaces non-alphanumeric characters with hyphens</li>
     *   <li>Removes leading/trailing hyphens</li>
     *   <li>Limits length to 120 characters</li>
     * </ul>
     * </p>
     *
     * @param raw The raw string to slugify
     * @return A URL-safe slug string, or empty string if input is null
     */
    public static String slugify(String raw) {
        if (raw == null) {
            return "";
        }

        String slug = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "")
                .trim();

        if (slug.length() > 120) {
            slug = slug.substring(0, 120).replaceAll("-$", "");
        }

        return slug;
    }

    /**
     * Trims strings and returns null if empty, ensuring clean data persistence.
     * 
     * <p>This utility method follows the <b>Null Object Pattern</b> principles
     * by returning null for empty/blank strings instead of empty strings.</p>
     *
     * @param value The string to normalize
     * @return The trimmed string, or null if the input is null or blank
     */
    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Creates a short preview of the content for list views and search results.
     * 
     * <p>Generates a snippet of up to 220 characters, appending "..." if the
     * content exceeds this limit. This is useful for displaying previews
     * in search results and list views without exposing full content.</p>
     *
     * @param content The full content to create a snippet from
     * @return A shortened preview string, or empty string if input is null
     */
    public static String toSnippet(String content) {
        if (content == null) {
            return "";
        }

        if (content.length() <= 220) {
            return content;
        }

        return content.substring(0, 220) + "...";
    }
}

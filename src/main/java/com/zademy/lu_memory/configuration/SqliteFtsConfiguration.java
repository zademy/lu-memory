package com.zademy.lu_memory.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SqliteFtsConfiguration implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqliteFtsConfiguration.class);

    private final JdbcTemplate jdbcTemplate;

    public SqliteFtsConfiguration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        LOGGER.info(">>> SqliteFtsConfiguration CONSTRUCTOR called <<<");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("=== AFTERPROPERTIESSET CALLED - Starting database initialization ===");
        try {
            // Verify database connection
            String dbUrl = jdbcTemplate.getDataSource().getConnection().getMetaData().getURL();
            LOGGER.info("Database URL: {}", dbUrl);

            // Check if tables exist BEFORE
            boolean observationsExists = checkTableExists("observations");
            boolean sessionsExists = checkTableExists("memory_sessions");
            boolean promptsExists = checkTableExists("saved_prompts");

            LOGGER.info("BEFORE CREATE - observations: {}, memory_sessions: {}, saved_prompts: {}",
                observationsExists ? "EXISTS" : "MISSING",
                sessionsExists ? "EXISTS" : "MISSING",
                promptsExists ? "EXISTS" : "MISSING");

            // 1. Create observations table
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS observations (
                        id TEXT PRIMARY KEY,
                        type TEXT NOT NULL,
                        topic_key TEXT NOT NULL,
                        title TEXT,
                        content TEXT NOT NULL,
                        tags_text TEXT,
                        source TEXT,
                        session_id TEXT,
                        scope TEXT NOT NULL DEFAULT 'project',
                        project_key TEXT NOT NULL DEFAULT 'default',
                        project_name TEXT,
                        content_hash TEXT,
                        duplicate_count INTEGER NOT NULL DEFAULT 0,
                        revision_count INTEGER NOT NULL DEFAULT 1,
                        last_seen_at TIMESTAMP,
                        deleted BOOLEAN NOT NULL DEFAULT FALSE,
                        deleted_at TIMESTAMP,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            LOGGER.info("Table 'observations' created/verified");

            // 2. Create memory_sessions table
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS memory_sessions (
                        id TEXT PRIMARY KEY,
                        agent_name TEXT,
                        branch_name TEXT,
                        summary TEXT,
                        status TEXT NOT NULL DEFAULT 'STARTED',
                        started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        ended_at TIMESTAMP
                    )
                    """);
            LOGGER.info("Table 'memory_sessions' created/verified");

            // 3. Create saved_prompts table
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS saved_prompts (
                        id TEXT PRIMARY KEY,
                        session_id TEXT,
                        topic_key TEXT,
                        intent TEXT,
                        source TEXT,
                        prompt TEXT NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            LOGGER.info("Table 'saved_prompts' created/verified");

            // 4. Create virtual table FTS5 for full-text search
            jdbcTemplate.execute("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS observations_fts USING fts5(
                        type,
                        topic_key,
                        title,
                        content,
                        tags_text,
                        source,
                        content='observations',
                        content_rowid='rowid'
                    )
                    """);
            LOGGER.info("Virtual table 'observations_fts' (FTS5) created/verified");

            // 5. Create triggers for FTS sync
            createTriggers();

            // Verify tables were created
            boolean obsAfter = checkTableExists("observations");
            boolean sessAfter = checkTableExists("memory_sessions");
            boolean promptsAfter = checkTableExists("saved_prompts");

            LOGGER.info("AFTER CREATE - observations: {}, memory_sessions: {}, saved_prompts: {}",
                obsAfter ? "EXISTS" : "MISSING",
                sessAfter ? "EXISTS" : "MISSING",
                promptsAfter ? "EXISTS" : "MISSING");

            LOGGER.info("=== DATABASE INITIALIZATION COMPLETED SUCCESSFULLY ===");
        } catch (Exception e) {
            LOGGER.error("CRITICAL: Error during database initialization: {}", e.getMessage(), e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private boolean checkTableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?",
                Integer.class,
                tableName
            );
            return count != null && count > 0;
        } catch (Exception e) {
            LOGGER.warn("Error checking if table {} exists: {}", tableName, e.getMessage());
            return false;
        }
    }

    private void createTriggers() {
        try {
            // Trigger para INSERT
            jdbcTemplate.execute("""
                    CREATE TRIGGER IF NOT EXISTS observations_ai AFTER INSERT ON observations
                    BEGIN
                        INSERT INTO observations_fts(rowid, type, topic_key, title, content, tags_text, source)
                        VALUES (NEW.rowid, NEW.type, NEW.topic_key, NEW.title, NEW.content, NEW.tags_text, NEW.source);
                    END
                    """);

            // Trigger para DELETE
            jdbcTemplate.execute("""
                    CREATE TRIGGER IF NOT EXISTS observations_ad AFTER DELETE ON observations
                    BEGIN
                        DELETE FROM observations_fts WHERE rowid = OLD.rowid;
                    END
                    """);

            // Trigger para UPDATE
            jdbcTemplate.execute("""
                    CREATE TRIGGER IF NOT EXISTS observations_au AFTER UPDATE ON observations
                    BEGIN
                        DELETE FROM observations_fts WHERE rowid = OLD.rowid;
                        INSERT INTO observations_fts(rowid, type, topic_key, title, content, tags_text, source)
                        VALUES (NEW.rowid, NEW.type, NEW.topic_key, NEW.title, NEW.content, NEW.tags_text, NEW.source);
                    END
                    """);

            LOGGER.info("SQLite FTS5 triggers created");
        } catch (Exception e) {
            LOGGER.error("Error creating FTS5 triggers: " + e.getMessage(), e);
        }
    }
}

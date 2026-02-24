package com.zademy.lu_memory.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SqliteFtsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqliteFtsConfiguration.class);

    @Bean
    ApplicationRunner ensureFts5Index(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                // Verificar si la tabla observations existe primero
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
                            deleted BOOLEAN NOT NULL DEFAULT FALSE,
                            deleted_at TIMESTAMP,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                        )
                        """);

                // Crear tabla virtual FTS5 para búsquedas de texto completo
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

                // Crear triggers para mantener FTS sincronizado
                createTriggers(jdbcTemplate);

                LOGGER.info("SQLite FTS5 virtual table and triggers are ready");
            } catch (Exception e) {
                LOGGER.error("Error configuring FTS5: " + e.getMessage(), e);
                // No fallar la aplicación si FTS5 falla
            }
        };
    }

    private void createTriggers(JdbcTemplate jdbcTemplate) {
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

package com.zademy.lu_memory.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PostgresFtsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresFtsConfiguration.class);

    @Bean
    ApplicationRunner ensureFtsIndex(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_observations_topic
                    ON observations(topic_key)
                    """);

            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_observations_created_at
                    ON observations(created_at)
                    """);

            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_observations_fts
                    ON observations
                    USING GIN (
                        to_tsvector('simple',
                            coalesce(topic_key,'') || ' ' ||
                            coalesce(title,'') || ' ' ||
                            coalesce(content,'') || ' ' ||
                            coalesce(tags_text,''))
                    )
                    """);

            LOGGER.info("PostgreSQL FTS indexes are ready");
        };
    }
}

-- Script to create lu-memory tables in SQLite

-- 1. Table observations
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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    importance_level TEXT NOT NULL DEFAULT 'MEDIUM'
);

-- 2. Table memory_sessions
CREATE TABLE IF NOT EXISTS memory_sessions (
    id TEXT PRIMARY KEY,
    agent_name TEXT,
    branch_name TEXT,
    summary TEXT,
    status TEXT NOT NULL DEFAULT 'STARTED',
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP
);

-- 3. Table saved_prompts
CREATE TABLE IF NOT EXISTS saved_prompts (
    id TEXT PRIMARY KEY,
    session_id TEXT,
    topic_key TEXT,
    intent TEXT,
    source TEXT,
    prompt TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Virtual table FTS5 for full-text search
CREATE VIRTUAL TABLE IF NOT EXISTS observations_fts USING fts5(
    type,
    topic_key,
    title,
    content,
    tags_text,
    source,
    importance_level,
    content='observations',
    content_rowid='rowid'
);

-- 5. Triggers to synchronize FTS

-- Trigger for INSERT
CREATE TRIGGER IF NOT EXISTS observations_ai AFTER INSERT ON observations
BEGIN
    INSERT INTO observations_fts(rowid, type, topic_key, title, content, tags_text, source, importance_level)
    VALUES (NEW.rowid, NEW.type, NEW.topic_key, NEW.title, NEW.content, NEW.tags_text, NEW.source, NEW.importance_level);
END;

-- Trigger for DELETE
CREATE TRIGGER IF NOT EXISTS observations_ad AFTER DELETE ON observations
BEGIN
    DELETE FROM observations_fts WHERE rowid = OLD.rowid;
END;

-- Trigger for UPDATE
CREATE TRIGGER IF NOT EXISTS observations_au AFTER UPDATE ON observations
BEGIN
    DELETE FROM observations_fts WHERE rowid = OLD.rowid;
    INSERT INTO observations_fts(rowid, type, topic_key, title, content, tags_text, source, importance_level)
    VALUES (NEW.rowid, NEW.type, NEW.topic_key, NEW.title, NEW.content, NEW.tags_text, NEW.source, NEW.importance_level);
END;

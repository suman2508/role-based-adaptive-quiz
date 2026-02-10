-- Flyway V1: Initial normalized schema for Adaptive Quiz Platform
-- Note: Using BIGSERIAL (long) IDs to align with existing JPA entities.
--       Options stored as JSONB. Embedding vector omitted initially (add via later migration when pgvector is enabled).

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    email           VARCHAR(320) NOT NULL UNIQUE,
    target_role     VARCHAR(200),
    readiness_score NUMERIC(5,2) DEFAULT 0
);

CREATE TABLE IF NOT EXISTS roles (
    id         BIGSERIAL PRIMARY KEY,
    role_name  VARCHAR(200) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS skills (
    id             BIGSERIAL PRIMARY KEY,
    skill_name     VARCHAR(200) NOT NULL,
    role_id        BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    priority_score INTEGER
);

CREATE INDEX IF NOT EXISTS idx_skills_role_id ON skills(role_id);

CREATE TABLE IF NOT EXISTS roadmaps (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    week_number INTEGER NOT NULL CHECK (week_number >= 1),
    skill_id    BIGINT REFERENCES skills(id) ON DELETE SET NULL,
    description TEXT
);

CREATE INDEX IF NOT EXISTS idx_roadmaps_user_week ON roadmaps(user_id, week_number);

CREATE TABLE IF NOT EXISTS quiz_questions (
    id              BIGSERIAL PRIMARY KEY,
    skill_id        BIGINT REFERENCES skills(id) ON DELETE CASCADE,
    difficulty      VARCHAR(16) NOT NULL,
    question_text   TEXT NOT NULL,
    options         JSONB NOT NULL,
    correct_answer  VARCHAR(512) NOT NULL,
    explanation     TEXT
    -- embedding_vector VECTOR(1536) -- add in a separate migration when pgvector extension is enabled
);

CREATE INDEX IF NOT EXISTS idx_quiz_questions_skill ON quiz_questions(skill_id);
CREATE INDEX IF NOT EXISTS idx_quiz_questions_skill_difficulty ON quiz_questions(skill_id, difficulty);

CREATE TABLE IF NOT EXISTS quiz_attempts (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    question_id       BIGINT NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    selected_answer   VARCHAR(512),
    score             NUMERIC(5,2) NOT NULL,
    attempt_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_quiz_attempts_user_time ON quiz_attempts(user_id, attempt_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_quiz_attempts_question ON quiz_attempts(question_id);

CREATE TABLE IF NOT EXISTS practice_schedules (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date                 DATE NOT NULL,
    skill_id             BIGINT REFERENCES skills(id) ON DELETE SET NULL,
    activity_description TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_practice_schedule_user_date ON practice_schedules(user_id, date);

-- Seed data placeholders (optional)
-- INSERT INTO roles (role_name) VALUES ('Backend Developer') ON CONFLICT DO NOTHING;

-- Constraints/Validation Notes:
-- - quiz_questions.difficulty can be validated at application layer or changed to an enum later.
-- - Consider additional covering indexes after observing query plans.

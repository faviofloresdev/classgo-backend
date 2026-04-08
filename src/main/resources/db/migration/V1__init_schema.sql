CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    role VARCHAR(20) NOT NULL,
    auth_provider VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE teachers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE parents (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE classes (
    id UUID PRIMARY KEY,
    teacher_id UUID NOT NULL REFERENCES teachers(id),
    name VARCHAR(255) NOT NULL,
    grade VARCHAR(20) NOT NULL,
    section VARCHAR(20),
    academic_year VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE students (
    id UUID PRIMARY KEY,
    class_id UUID NOT NULL REFERENCES classes(id),
    student_code VARCHAR(100) NOT NULL UNIQUE,
    internal_alias VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE parent_student_links (
    id UUID PRIMARY KEY,
    parent_id UUID NOT NULL REFERENCES parents(id),
    student_id UUID NOT NULL REFERENCES students(id),
    display_name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    nickname VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_parent_student UNIQUE (parent_id, student_id)
);

CREATE TABLE subjects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE topics (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES subjects(id),
    grade VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE questions (
    id UUID PRIMARY KEY,
    topic_id UUID NOT NULL REFERENCES topics(id),
    type VARCHAR(30) NOT NULL,
    prompt TEXT NOT NULL,
    explanation TEXT,
    difficulty_level INTEGER,
    sort_order INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE question_options (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES questions(id),
    option_text VARCHAR(500) NOT NULL,
    is_correct BOOLEAN NOT NULL,
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE weekly_challenges (
    id UUID PRIMARY KEY,
    class_id UUID NOT NULL REFERENCES classes(id),
    topic_id UUID NOT NULL REFERENCES topics(id),
    title VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_by_teacher_id UUID NOT NULL REFERENCES teachers(id),
    published_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE challenge_participants (
    id UUID PRIMARY KEY,
    challenge_id UUID NOT NULL REFERENCES weekly_challenges(id),
    student_id UUID NOT NULL REFERENCES students(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_challenge_student UNIQUE (challenge_id, student_id)
);

CREATE TABLE student_sessions (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id),
    challenge_id UUID NOT NULL REFERENCES weekly_challenges(id),
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE,
    last_activity_at TIMESTAMP WITH TIME ZONE,
    total_seconds BIGINT NOT NULL DEFAULT 0,
    active_seconds BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE student_activity_events (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES student_sessions(id),
    event_type VARCHAR(30) NOT NULL,
    event_payload TEXT,
    happened_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE student_answers (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id),
    challenge_id UUID NOT NULL REFERENCES weekly_challenges(id),
    question_id UUID NOT NULL REFERENCES questions(id),
    selected_option_id UUID REFERENCES question_options(id),
    answer_text TEXT,
    is_correct BOOLEAN NOT NULL,
    points_earned INTEGER NOT NULL,
    answered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_student_answer UNIQUE (student_id, challenge_id, question_id)
);

CREATE TABLE weekly_results (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id),
    challenge_id UUID NOT NULL REFERENCES weekly_challenges(id),
    total_points INTEGER NOT NULL,
    accuracy NUMERIC(5, 2) NOT NULL,
    completed_activities INTEGER NOT NULL,
    active_seconds BIGINT NOT NULL,
    rank_position INTEGER NOT NULL,
    strengths_summary VARCHAR(500),
    weaknesses_summary VARCHAR(500),
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_weekly_result UNIQUE (student_id, challenge_id)
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    recipient_email VARCHAR(255) NOT NULL,
    type VARCHAR(30) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payload TEXT NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE,
    error_message VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE media_assets (
    id UUID PRIMARY KEY,
    owner_user_id UUID REFERENCES users(id),
    category VARCHAR(30) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_key VARCHAR(500) NOT NULL UNIQUE,
    public_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token VARCHAR(200) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

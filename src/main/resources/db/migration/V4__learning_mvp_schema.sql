ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_id VARCHAR(100);

UPDATE users
SET name = COALESCE(name, email)
WHERE name IS NULL;

CREATE TABLE avatars_catalog (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE classrooms (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL UNIQUE,
    description VARCHAR(500),
    teacher_id UUID NOT NULL REFERENCES users(id),
    active_plan_id UUID,
    current_week INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE enrollments (
    id UUID PRIMARY KEY,
    classroom_id UUID NOT NULL REFERENCES classrooms(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_enrollment UNIQUE (classroom_id, student_id)
);

CREATE TABLE plans (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    teacher_id UUID NOT NULL REFERENCES users(id),
    activation_mode VARCHAR(20) NOT NULL,
    start_date DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE learning_topics (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    icon VARCHAR(100),
    color VARCHAR(20),
    difficulty VARCHAR(20) NOT NULL,
    questions_json TEXT NOT NULL,
    teacher_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE plan_topics (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    topic_id UUID NOT NULL REFERENCES learning_topics(id),
    week_number INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_plan_topic UNIQUE (plan_id, topic_id),
    CONSTRAINT uq_plan_week UNIQUE (plan_id, week_number)
);

ALTER TABLE classrooms
    ADD CONSTRAINT fk_classrooms_active_plan
    FOREIGN KEY (active_plan_id) REFERENCES plans(id);

CREATE TABLE student_attempts (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES users(id),
    classroom_id UUID NOT NULL REFERENCES classrooms(id) ON DELETE CASCADE,
    topic_id UUID NOT NULL REFERENCES learning_topics(id),
    week_number INTEGER NOT NULL,
    score INTEGER NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    time_spent INTEGER NOT NULL,
    correct_answers INTEGER NOT NULL,
    total_questions INTEGER NOT NULL,
    answers_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_classrooms_teacher_id ON classrooms(teacher_id);
CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_plans_teacher_id ON plans(teacher_id);
CREATE INDEX idx_learning_topics_teacher_id ON learning_topics(teacher_id);
CREATE INDEX idx_plan_topics_plan_id ON plan_topics(plan_id);
CREATE INDEX idx_student_attempts_lookup ON student_attempts(student_id, classroom_id, topic_id, week_number);

INSERT INTO avatars_catalog (id, name, category, url, sort_order)
SELECT 'animal-1', 'Leon', 'animals', 'https://cdn.classgo.app/avatars/animal-1.png', 1
WHERE NOT EXISTS (SELECT 1 FROM avatars_catalog WHERE id = 'animal-1');

INSERT INTO avatars_catalog (id, name, category, url, sort_order)
SELECT 'animal-2', 'Tigre', 'animals', 'https://cdn.classgo.app/avatars/animal-2.png', 2
WHERE NOT EXISTS (SELECT 1 FROM avatars_catalog WHERE id = 'animal-2');

INSERT INTO avatars_catalog (id, name, category, url, sort_order)
SELECT 'animal-3', 'Panda', 'animals', 'https://cdn.classgo.app/avatars/animal-3.png', 3
WHERE NOT EXISTS (SELECT 1 FROM avatars_catalog WHERE id = 'animal-3');

INSERT INTO avatars_catalog (id, name, category, url, sort_order)
SELECT 'robot-1', 'Robot Azul', 'robots', 'https://cdn.classgo.app/avatars/robot-1.png', 4
WHERE NOT EXISTS (SELECT 1 FROM avatars_catalog WHERE id = 'robot-1');

INSERT INTO avatars_catalog (id, name, category, url, sort_order)
SELECT 'robot-2', 'Robot Naranja', 'robots', 'https://cdn.classgo.app/avatars/robot-2.png', 5
WHERE NOT EXISTS (SELECT 1 FROM avatars_catalog WHERE id = 'robot-2');

INSERT INTO avatars_catalog (id, name, category, url, sort_order)
SELECT 'char-1', 'Explorador', 'characters', 'https://cdn.classgo.app/avatars/char-1.png', 6
WHERE NOT EXISTS (SELECT 1 FROM avatars_catalog WHERE id = 'char-1');

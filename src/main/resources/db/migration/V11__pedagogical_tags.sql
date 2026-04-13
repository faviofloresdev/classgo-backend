CREATE TABLE pedagogical_tags (
    id UUID PRIMARY KEY,
    teacher_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_pedagogical_tag_teacher_slug UNIQUE (teacher_id, slug)
);

CREATE INDEX idx_pedagogical_tags_teacher_name ON pedagogical_tags(teacher_id, name);

CREATE TABLE revoked_access_tokens (
    id UUID PRIMARY KEY,
    token_id VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO users (id, email, name, avatar_id, password_hash, role, auth_provider, is_active, created_at, updated_at)
SELECT '11111111-1111-1111-1111-111111111111', 'teacher@classgo.test', 'Prof. Garcia', 'char-1',
       '{noop}password',
       'TEACHER', 'LOCAL', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'teacher@classgo.test');

INSERT INTO users (id, email, name, avatar_id, password_hash, role, auth_provider, is_active, created_at, updated_at)
SELECT '22222222-2222-2222-2222-222222222222', 'student@classgo.test', 'Maria Lopez', 'animal-1',
       '{noop}password',
       'STUDENT', 'LOCAL', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'student@classgo.test');

INSERT INTO teachers (id, user_id, full_name, created_at, updated_at)
SELECT '33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'Prof. Garcia', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM teachers WHERE user_id = '11111111-1111-1111-1111-111111111111');

INSERT INTO learning_topics (id, name, description, icon, color, difficulty, questions_json, teacher_id, created_at, updated_at)
SELECT
    '44444444-4444-4444-4444-444444444444',
    'Sumas Basicas',
    'Aprende a sumar',
    'Zap',
    '#10B981',
    'EASY',
    '[{"id":"q1","type":"single_choice","prompt":"Cuanto es 3 + 4?","explanation":"Sumamos 3 y 4 para obtener 7.","options":[{"id":"a","text":"6","isCorrect":false},{"id":"b","text":"7","isCorrect":true},{"id":"c","text":"8","isCorrect":false}]},{"id":"q2","type":"fill_in_blank","prompt":"Completa la palabra escondiendo letras.","explanation":"","word":"CUATRO","hiddenIndexes":[1,3]}]',
    '11111111-1111-1111-1111-111111111111',
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM learning_topics WHERE id = '44444444-4444-4444-4444-444444444444');

INSERT INTO plans (id, name, description, teacher_id, activation_mode, start_date, created_at, updated_at)
SELECT '55555555-5555-5555-5555-555555555555', 'Matematicas Nivel 1', 'Plan demo', '11111111-1111-1111-1111-111111111111', 'MANUAL', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE id = '55555555-5555-5555-5555-555555555555');

INSERT INTO plan_topics (id, plan_id, topic_id, week_number, is_active, created_at, updated_at)
SELECT '66666666-6666-6666-6666-666666666666', '55555555-5555-5555-5555-555555555555', '44444444-4444-4444-4444-444444444444', 1, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM plan_topics WHERE id = '66666666-6666-6666-6666-666666666666');

INSERT INTO classrooms (id, name, code, description, teacher_id, active_plan_id, current_week, created_at, updated_at)
SELECT '77777777-7777-7777-7777-777777777777', 'Matematicas 3A', 'MAT3A', 'Aula demo', '11111111-1111-1111-1111-111111111111',
       '55555555-5555-5555-5555-555555555555', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM classrooms WHERE id = '77777777-7777-7777-7777-777777777777');

INSERT INTO enrollments (id, classroom_id, student_id, joined_at, created_at, updated_at)
SELECT '88888888-8888-8888-8888-888888888888', '77777777-7777-7777-7777-777777777777', '22222222-2222-2222-2222-222222222222', NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM enrollments WHERE id = '88888888-8888-8888-8888-888888888888');

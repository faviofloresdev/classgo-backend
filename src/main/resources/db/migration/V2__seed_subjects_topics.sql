INSERT INTO subjects (id, name, created_at, updated_at) VALUES
('00000000-0000-0000-0000-000000000101', 'Math', NOW(), NOW()),
('00000000-0000-0000-0000-000000000102', 'Language', NOW(), NOW());

INSERT INTO topics (id, subject_id, grade, title, description, created_at, updated_at) VALUES
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000101', '1', 'Counting to 10', 'Recognize and count numbers up to ten', NOW(), NOW()),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000101', '1', 'Shapes Around Us', 'Identify circles, squares and triangles', NOW(), NOW()),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000101', '2', 'Addition up to 20', 'Simple addition using objects and visuals', NOW(), NOW()),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000101', '2', 'Subtraction up to 20', 'Understand taking away with concrete examples', NOW(), NOW()),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000101', '2', 'Patterns', 'Recognize repeating visual and number patterns', NOW(), NOW()),
('00000000-0000-0000-0000-000000000206', '00000000-0000-0000-0000-000000000102', '1', 'Letter Sounds', 'Associate common sounds to letters', NOW(), NOW()),
('00000000-0000-0000-0000-000000000207', '00000000-0000-0000-0000-000000000102', '1', 'Vowels', 'Identify vowels in simple words', NOW(), NOW()),
('00000000-0000-0000-0000-000000000208', '00000000-0000-0000-0000-000000000102', '2', 'Syllables', 'Split words into syllables', NOW(), NOW()),
('00000000-0000-0000-0000-000000000209', '00000000-0000-0000-0000-000000000102', '2', 'Reading Short Sentences', 'Read and complete short guided sentences', NOW(), NOW()),
('00000000-0000-0000-0000-000000000210', '00000000-0000-0000-0000-000000000102', '2', 'Opposites', 'Understand opposite words in context', NOW(), NOW());

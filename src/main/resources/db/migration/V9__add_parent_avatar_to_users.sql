ALTER TABLE users
    ADD COLUMN IF NOT EXISTS parent_avatar_id VARCHAR(100);

UPDATE users
SET parent_avatar_id = 'parent-1'
WHERE role = 'STUDENT'
  AND parent_avatar_id IS NULL;

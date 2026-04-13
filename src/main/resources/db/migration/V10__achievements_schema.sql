CREATE TABLE achievements (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(30) NOT NULL,
    action VARCHAR(40) NOT NULL,
    actor VARCHAR(20) NOT NULL,
    threshold INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    is_manual BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE user_achievements (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
    progress_value BIGINT NOT NULL DEFAULT 0,
    unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    unlocked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_user_achievement UNIQUE (user_id, achievement_id)
);

CREATE TABLE user_achievement_metrics (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    completed_challenges_count BIGINT NOT NULL DEFAULT 0,
    current_weekly_streak BIGINT NOT NULL DEFAULT 0,
    high_score_challenges_count BIGINT NOT NULL DEFAULT 0,
    perfect_score_challenges_count BIGINT NOT NULL DEFAULT 0,
    distinct_sections_count BIGINT NOT NULL DEFAULT 0,
    distinct_features_count BIGINT NOT NULL DEFAULT 0,
    distinct_activity_types_count BIGINT NOT NULL DEFAULT 0,
    first_completion_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_user_achievement_metrics_user UNIQUE (user_id)
);

CREATE TABLE user_achievement_facts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fact_type VARCHAR(50) NOT NULL,
    fact_key VARCHAR(200) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_user_achievement_fact UNIQUE (user_id, fact_type, fact_key)
);

CREATE TABLE achievement_global_facts (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    fact_type VARCHAR(50) NOT NULL,
    fact_key VARCHAR(200) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_achievement_global_fact UNIQUE (fact_type, fact_key)
);

CREATE INDEX idx_user_achievement_facts_lookup ON user_achievement_facts(user_id, fact_type);

INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000001', 'PROGRESS_CHALLENGE_1', 'First Challenge', 'PROGRESS', 'COMPLETE_CHALLENGE', 'STUDENT', 1, TRUE, FALSE, FALSE, 10, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PROGRESS_CHALLENGE_1');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000002', 'PROGRESS_CHALLENGE_3', 'Getting Started', 'PROGRESS', 'COMPLETE_CHALLENGE', 'STUDENT', 3, TRUE, FALSE, FALSE, 20, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PROGRESS_CHALLENGE_3');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000003', 'PROGRESS_CHALLENGE_5', 'Momentum', 'PROGRESS', 'COMPLETE_CHALLENGE', 'STUDENT', 5, TRUE, FALSE, FALSE, 30, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PROGRESS_CHALLENGE_5');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000004', 'PROGRESS_CHALLENGE_10', 'Challenge Runner', 'PROGRESS', 'COMPLETE_CHALLENGE', 'STUDENT', 10, TRUE, FALSE, FALSE, 40, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PROGRESS_CHALLENGE_10');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000005', 'PROGRESS_CHALLENGE_20', 'Challenge Builder', 'PROGRESS', 'COMPLETE_CHALLENGE', 'STUDENT', 20, TRUE, FALSE, FALSE, 50, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PROGRESS_CHALLENGE_20');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000006', 'PROGRESS_CHALLENGE_35', 'Challenge Explorer', 'PROGRESS', 'COMPLETE_CHALLENGE', 'STUDENT', 35, TRUE, FALSE, FALSE, 60, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PROGRESS_CHALLENGE_35');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000007', 'PROGRESS_CHALLENGE_50', 'Challenge Master', 'PROGRESS', 'COMPLETE_CHALLENGE', 'STUDENT', 50, TRUE, FALSE, FALSE, 70, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PROGRESS_CHALLENGE_50');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000008', 'PROGRESS_CHALLENGE_75', 'Challenge Champion', 'PROGRESS', 'COMPLETE_CHALLENGE', 'STUDENT', 75, TRUE, FALSE, FALSE, 80, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PROGRESS_CHALLENGE_75');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000009', 'PROGRESS_CHALLENGE_100', 'Challenge Legend', 'PROGRESS', 'COMPLETE_CHALLENGE', 'STUDENT', 100, TRUE, FALSE, FALSE, 90, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PROGRESS_CHALLENGE_100');

INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000010', 'CONSISTENCY_WEEK_2', 'Weekly Rhythm', 'CONSISTENCY', 'COMPLETE_CHALLENGE', 'STUDENT', 2, TRUE, FALSE, FALSE, 110, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'CONSISTENCY_WEEK_2');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000011', 'CONSISTENCY_WEEK_3', 'Steady Learner', 'CONSISTENCY', 'COMPLETE_CHALLENGE', 'STUDENT', 3, TRUE, FALSE, FALSE, 120, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'CONSISTENCY_WEEK_3');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000012', 'CONSISTENCY_WEEK_5', 'Habit Builder', 'CONSISTENCY', 'COMPLETE_CHALLENGE', 'STUDENT', 5, TRUE, FALSE, FALSE, 130, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'CONSISTENCY_WEEK_5');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000013', 'CONSISTENCY_WEEK_8', 'Weekly Warrior', 'CONSISTENCY', 'COMPLETE_CHALLENGE', 'STUDENT', 8, TRUE, FALSE, FALSE, 140, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'CONSISTENCY_WEEK_8');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000014', 'CONSISTENCY_WEEK_12', 'Consistency Hero', 'CONSISTENCY', 'COMPLETE_CHALLENGE', 'STUDENT', 12, TRUE, FALSE, FALSE, 150, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'CONSISTENCY_WEEK_12');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000015', 'CONSISTENCY_WEEK_20', 'Long Run', 'CONSISTENCY', 'COMPLETE_CHALLENGE', 'STUDENT', 20, TRUE, FALSE, FALSE, 160, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'CONSISTENCY_WEEK_20');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000016', 'CONSISTENCY_WEEK_30', 'Unstoppable', 'CONSISTENCY', 'COMPLETE_CHALLENGE', 'STUDENT', 30, TRUE, FALSE, FALSE, 170, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'CONSISTENCY_WEEK_30');

INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000017', 'PERFORMANCE_HIGH_1', 'High Score', 'PERFORMANCE', 'RECORD_CHALLENGE_SCORE', 'STUDENT', 1, TRUE, FALSE, FALSE, 210, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PERFORMANCE_HIGH_1');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000018', 'PERFORMANCE_HIGH_3', 'Sharp Performer', 'PERFORMANCE', 'RECORD_CHALLENGE_SCORE', 'STUDENT', 3, TRUE, FALSE, FALSE, 220, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PERFORMANCE_HIGH_3');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000019', 'PERFORMANCE_HIGH_10', 'High Score Specialist', 'PERFORMANCE', 'RECORD_CHALLENGE_SCORE', 'STUDENT', 10, TRUE, FALSE, FALSE, 230, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PERFORMANCE_HIGH_10');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000020', 'PERFORMANCE_PERFECT_1', 'Perfect Start', 'PERFORMANCE', 'RECORD_CHALLENGE_SCORE', 'STUDENT', 1, TRUE, FALSE, FALSE, 240, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PERFORMANCE_PERFECT_1');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000021', 'PERFORMANCE_PERFECT_5', 'Perfect Focus', 'PERFORMANCE', 'RECORD_CHALLENGE_SCORE', 'STUDENT', 5, TRUE, FALSE, FALSE, 250, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PERFORMANCE_PERFECT_5');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000022', 'PERFORMANCE_PERFECT_10', 'Perfect Mastery', 'PERFORMANCE', 'RECORD_CHALLENGE_SCORE', 'STUDENT', 10, TRUE, FALSE, FALSE, 260, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'PERFORMANCE_PERFECT_10');

INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000023', 'EXPLORATION_AVATAR_STUDENT', 'Student Avatar Updated', 'EXPLORATION', 'UPDATE_AVATAR', 'STUDENT', 1, TRUE, FALSE, FALSE, 310, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'EXPLORATION_AVATAR_STUDENT');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000024', 'EXPLORATION_AVATAR_PARENT', 'Parent Avatar Updated', 'EXPLORATION', 'UPDATE_AVATAR', 'PARENT', 1, TRUE, FALSE, FALSE, 320, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'EXPLORATION_AVATAR_PARENT');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000025', 'EXPLORATION_PROFILE_COMPLETE', 'Profile Complete', 'EXPLORATION', 'COMPLETE_PROFILE', 'STUDENT', 1, TRUE, FALSE, FALSE, 330, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'EXPLORATION_PROFILE_COMPLETE');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000026', 'EXPLORATION_SECTIONS_3', 'Explorer', 'EXPLORATION', 'VISIT_SECTION', 'STUDENT', 3, TRUE, FALSE, FALSE, 340, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'EXPLORATION_SECTIONS_3');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000027', 'EXPLORATION_FEATURE_1', 'Curious', 'EXPLORATION', 'USE_FEATURE', 'STUDENT', 1, TRUE, FALSE, FALSE, 350, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'EXPLORATION_FEATURE_1');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000028', 'EXPLORATION_FEATURE_3', 'Discoverer', 'EXPLORATION', 'USE_FEATURE', 'STUDENT', 3, TRUE, FALSE, FALSE, 360, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'EXPLORATION_FEATURE_3');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000029', 'EXPLORATION_ACTIVITY_TYPE_1', 'Adventurer', 'EXPLORATION', 'COMPLETE_ACTIVITY_TYPE', 'STUDENT', 1, TRUE, FALSE, FALSE, 370, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'EXPLORATION_ACTIVITY_TYPE_1');
INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000030', 'EXPLORATION_ACTIVITY_TYPE_3', 'Multitalent', 'EXPLORATION', 'COMPLETE_ACTIVITY_TYPE', 'STUDENT', 3, TRUE, FALSE, FALSE, 380, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'EXPLORATION_ACTIVITY_TYPE_3');

INSERT INTO achievements (id, code, name, category, action, actor, threshold, is_active, is_hidden, is_manual, sort_order, created_at, updated_at)
SELECT 'a1000000-0000-0000-0000-000000000031', 'SPECIAL_FIRST_COMPLETION', 'Pioneer', 'SPECIAL', 'FIRST_TO_COMPLETE_CHALLENGE', 'STUDENT', 1, TRUE, FALSE, FALSE, 410, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE code = 'SPECIAL_FIRST_COMPLETION');

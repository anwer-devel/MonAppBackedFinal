-- V41: Create user_scores table
CREATE TABLE user_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    partner_id UUID REFERENCES partners(id) ON DELETE CASCADE,
    total_score BIGINT NOT NULL DEFAULT 0,
    total_events INTEGER NOT NULL DEFAULT 0,
    total_correct_answers INTEGER NOT NULL DEFAULT 0,
    total_wrong_answers INTEGER NOT NULL DEFAULT 0,
    rank INTEGER,
    last_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_user_partner UNIQUE (user_id, partner_id)
);

-- Indexes
CREATE INDEX idx_score_user ON user_scores(user_id);
CREATE INDEX idx_score_global ON user_scores(total_score DESC);
CREATE INDEX idx_score_partner_rank ON user_scores(partner_id, total_score DESC);

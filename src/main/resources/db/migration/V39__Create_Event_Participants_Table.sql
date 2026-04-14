-- V39: Create event_participants table
CREATE TABLE event_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING' CHECK (status IN ('WAITING', 'ACTIVE', 'COMPLETED', 'ABANDONED')),
    joined_at TIMESTAMP NOT NULL DEFAULT now(),
    completed_at TIMESTAMP,
    current_question_index INTEGER NOT NULL DEFAULT 0,
    score INTEGER NOT NULL DEFAULT 0,
    correct_answers INTEGER NOT NULL DEFAULT 0,
    wrong_answers INTEGER NOT NULL DEFAULT 0,
    rank INTEGER,
    is_online BOOLEAN NOT NULL DEFAULT true,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_event_user UNIQUE (event_id, user_id)
);

-- Indexes
CREATE INDEX idx_participant_event ON event_participants(event_id);
CREATE INDEX idx_participant_user ON event_participants(user_id);
CREATE INDEX idx_participant_status ON event_participants(status);
CREATE INDEX idx_participant_score ON event_participants(event_id, score DESC);

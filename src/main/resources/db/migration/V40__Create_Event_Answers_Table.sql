-- V40: Create event_answers table
CREATE TABLE event_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    participant_id UUID NOT NULL REFERENCES event_participants(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_id UUID NOT NULL REFERENCES category_contents(id),
    question_index INTEGER NOT NULL,
    selected_answer TEXT,
    is_correct BOOLEAN NOT NULL DEFAULT false,
    points_earned INTEGER NOT NULL DEFAULT 0,
    response_time_ms BIGINT,
    speed_bonus INTEGER NOT NULL DEFAULT 0,
    answered_at TIMESTAMP NOT NULL DEFAULT now(),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_participant_content UNIQUE (participant_id, content_id)
);

-- Indexes
CREATE INDEX idx_answer_event ON event_answers(event_id);
CREATE INDEX idx_answer_participant ON event_answers(participant_id);
CREATE INDEX idx_answer_content ON event_answers(content_id);
CREATE INDEX idx_answer_event_user ON event_answers(event_id, user_id);
CREATE INDEX idx_answer_question ON event_answers(participant_id, question_index);

-- V38: Create events table
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cover_image VARCHAR(500),
    event_type VARCHAR(20) NOT NULL CHECK (event_type IN ('SIMPLE', 'LIVE')),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SCHEDULED', 'WAITING_ROOM', 'LIVE', 'FINISHED', 'CANCELLED')),
    category_id UUID NOT NULL REFERENCES categories(id),
    partner_id UUID REFERENCES partners(id),
    created_by VARCHAR(20) NOT NULL CHECK (created_by IN ('ADMIN', 'PARTNER')),
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    scheduled_at TIMESTAMP,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    max_participants INTEGER,
    current_participants INTEGER NOT NULL DEFAULT 0,
    current_question_index INTEGER NOT NULL DEFAULT 0,
    question_time_limit INTEGER NOT NULL DEFAULT 30,
    total_questions INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Indexes
CREATE INDEX idx_event_partner ON events(partner_id);
CREATE INDEX idx_event_status ON events(status);
CREATE INDEX idx_event_type ON events(event_type);
CREATE INDEX idx_event_category ON events(category_id);
CREATE INDEX idx_event_scheduled ON events(scheduled_at);
CREATE INDEX idx_event_active ON events(is_active);
CREATE INDEX idx_event_partner_status ON events(partner_id, status, scheduled_at);

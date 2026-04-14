-- V43: Create notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL CHECK (type IN ('EVENT_STARTED', 'EVENT_REMINDER', 'EVENT_CANCELLED', 'FRIEND_REQUEST', 'FRIEND_ACCEPTED', 'SCORE_UPDATED', 'ACHIEVEMENT_UNLOCKED', 'SYSTEM')),
    title VARCHAR(255) NOT NULL,
    body TEXT,
    data JSONB,
    is_read BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Indexes
CREATE INDEX idx_notif_user ON notifications(user_id);
CREATE INDEX idx_notif_type ON notifications(type);
CREATE INDEX idx_notif_read ON notifications(is_read);
CREATE INDEX idx_notif_user_read ON notifications(user_id, is_read);

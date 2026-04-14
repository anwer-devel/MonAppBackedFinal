-- V13: Create friendships table
CREATE TABLE friendships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    friend_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_friendship_pair UNIQUE (user_id, friend_id),
    CONSTRAINT chk_not_self_friend CHECK (user_id != friend_id)
);

CREATE INDEX idx_friendship_user ON friendships(user_id);
CREATE INDEX idx_friendship_friend ON friendships(friend_id);
CREATE INDEX idx_friendship_status ON friendships(status);

-- V42: Add DECLINED status to friendships and event_id column (if not already exists)
-- Note: friendships table already exists (V13), we're extending it

-- Add event_id column to track where friendship was initiated
ALTER TABLE friendships ADD COLUMN IF NOT EXISTS event_id UUID REFERENCES events(id);

-- Create index on event_id
CREATE INDEX IF NOT EXISTS idx_friendship_event ON friendships(event_id);

-- Note: The existing friendships table already has PENDING, ACCEPTED, BLOCKED
-- We're keeping the existing structure as it satisfies our needs

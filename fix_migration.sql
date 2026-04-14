-- Fix Migration Script for V20 Events Status Constraint
-- This script will:
-- 1. Remove the failed V20 migration from Flyway history
-- 2. Ensure events table has the correct constraint

-- Connect to the database and run these commands:

-- Step 1: Remove the failed migration from Flyway history
DELETE FROM flyway_schema_history
WHERE version = 20 AND description = 'Fix Events Status Constraint';

-- Step 2: If needed, manually fix the events table constraint
-- (only if the constraint is still in bad state)
-- First check if constraint exists and is causing issues
DO $$
BEGIN
  -- Check if the old constraint exists
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_name = 'events' AND constraint_name = 'events_status_check'
  ) THEN
    -- Drop the constraint
    ALTER TABLE events DROP CONSTRAINT events_status_check;
  END IF;

  -- Add the correct constraint that allows both old and new values
  ALTER TABLE events ADD CONSTRAINT events_status_check
    CHECK (status IN ('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'UPCOMING', 'LIVE', 'FINISHED'));

END $$;

-- Step 3: Migrate the data
UPDATE events SET status = 'PENDING' WHERE status = 'UPCOMING';
UPDATE events SET status = 'ACTIVE' WHERE status = 'LIVE';
UPDATE events SET status = 'COMPLETED' WHERE status = 'FINISHED';

-- Step 4: Tighten the constraint to only allow new values
ALTER TABLE events DROP CONSTRAINT events_status_check;
ALTER TABLE events ADD CONSTRAINT events_status_check
    CHECK (status IN ('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED'));


-- Reset Flyway history for V25 migration to allow re-execution
-- This will allow the corrected V25__Link_Events_To_Themes.sql to run properly

-- Delete the failed V25 migration from the schema_version table
DELETE FROM flyway_schema_history
WHERE version = '25'
AND description = 'Link Events To Themes';

-- Verify the deletion
SELECT * FROM flyway_schema_history
WHERE version >= '24'
ORDER BY version DESC;


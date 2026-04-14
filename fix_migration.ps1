#!/usr/bin/env pwsh

# Fix Migration Script for V20 Events Status Constraint
# This script resets the failed migration and applies the fix

param(
    [string]$DbHost = "localhost",
    [string]$DbPort = "5432",
    [string]$DbName = "app_db",
    [string]$DbUser = "postgres",
    [string]$DbPassword = "postgres_password_123"
)

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   Fixing Flyway Migration V20 - Events Status Constraint   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Connection string
$env:PGPASSWORD = $DbPassword

# Step 1: Remove failed migration from Flyway history
Write-Host "[1/4] Removing failed migration from Flyway history..." -ForegroundColor Yellow
$removeQuery = @"
DELETE FROM flyway_schema_history
WHERE version = 20 AND description = 'Fix Events Status Constraint';
"@

try {
    psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -c $removeQuery
    Write-Host "✓ Failed migration removed from Flyway history" -ForegroundColor Green
} catch {
    Write-Host "✗ Error removing migration: $_" -ForegroundColor Red
    exit 1
}

# Step 2: Fix the constraint and migrate data
Write-Host "[2/4] Fixing events table constraint..." -ForegroundColor Yellow
$fixConstraintQuery = @"
DO `$`$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_name = 'events' AND constraint_name = 'events_status_check'
  ) THEN
    ALTER TABLE events DROP CONSTRAINT events_status_check;
  END IF;

  ALTER TABLE events ADD CONSTRAINT events_status_check
    CHECK (status IN ('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'UPCOMING', 'LIVE', 'FINISHED'));
END `$`$;
"@

try {
    psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -c $fixConstraintQuery
    Write-Host "✓ Events table constraint fixed" -ForegroundColor Green
} catch {
    Write-Host "✗ Error fixing constraint: $_" -ForegroundColor Red
    exit 1
}

# Step 3: Migrate the data
Write-Host "[3/4] Migrating event status values..." -ForegroundColor Yellow
$migrateQuery = @"
UPDATE events SET status = 'PENDING' WHERE status = 'UPCOMING';
UPDATE events SET status = 'ACTIVE' WHERE status = 'LIVE';
UPDATE events SET status = 'COMPLETED' WHERE status = 'FINISHED';
"@

try {
    psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -c $migrateQuery
    Write-Host "✓ Event status values migrated" -ForegroundColor Green
} catch {
    Write-Host "✗ Error migrating data: $_" -ForegroundColor Red
    exit 1
}

# Step 4: Tighten the constraint
Write-Host "[4/4] Tightening events table constraint..." -ForegroundColor Yellow
$tightenQuery = @"
ALTER TABLE events DROP CONSTRAINT events_status_check;
ALTER TABLE events ADD CONSTRAINT events_status_check
    CHECK (status IN ('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED'));
"@

try {
    psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -c $tightenQuery
    Write-Host "✓ Events table constraint tightened" -ForegroundColor Green
} catch {
    Write-Host "✗ Error tightening constraint: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║              Migration Fix Completed Successfully!         ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Run: mvn clean" -ForegroundColor White
Write-Host "2. Run: mvn test" -ForegroundColor White
Write-Host ""


#!/usr/bin/env pwsh
#Requires -Version 7.0

# =============================================================================
# FULL SCENARIO TEST - Quiz/Event Application (PowerShell Version)
# =============================================================================
# Usage: .\full_scenario_test.ps1
# Or:    powershell -ExecutionPolicy Bypass -File .\full_scenario_test.ps1
# =============================================================================

$ErrorActionPreference = "Stop"

$BASE = "http://localhost:8080/api"
$Global:AUTH_TOKENS = @{}

# Colors
$Colors = @{
    Red = "`e[31m"
    Green = "`e[32m"  
    Yellow = "`e[33m"
    Blue = "`e[34m"
    Reset = "`e[0m"
}

function Write-Color($Color, $Message) {
    Write-Host "$($Colors[$Color])$Message$($Colors.Reset)"
}

function Invoke-ApiRequest {
    param(
        [string]$Method = "GET",
        [string]$Uri,
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [int]$ExpectedStatus = 200
    )

    try {
        $params = @{
            Method = $Method
            Uri = $Uri
            Headers = $Headers
            ContentType = "application/json"
        }
        if ($Body) { $params.Body = $Body }

        $response = Invoke-RestMethod @params -SkipHttpErrorCheck
        return @{ Success = $true; Data = $response }
    }
    catch {
        return @{ Success = $false; Error = $_.Exception.Message }
    }
}

function Extract-JsonValue($Json, $Key) {
    if ($Json -match "`"$Key`"\s*:\s*`"([^`"]*)`"") { return $Matches[1] }
    if ($Json -match "`"$Key`"\s*:\s*([0-9a-f-]{36})") { return $Matches[1] }
    if ($Json -match "`"$Key`"\s*:\s*(true|false|null|\d+)") { return $Matches[1] }
    return $null
}

# =============================================================================
# STEP 1 : LOGIN
# =============================================================================
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Color "Blue" "  STEP 1 : LOGIN"
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Host ""

$LoginData = @{
    admin = @{ email = "admin@quizapp.com"; password = "Admin123!" }
    partner = @{ email = "partner@cafe-lune.com"; password = "Partner123!" }
    alice = @{ email = "alice@example.com"; password = "Alice123!" }
    bob = @{ email = "bob@example.com"; password = "Bob123!" }
}

foreach ($user in $LoginData.Keys) {
    Write-Host "🔐 Login $user..."
    $body = $LoginData[$user] | ConvertTo-Json -Compress
    $result = Invoke-ApiRequest -Method POST -Uri "$BASE/auth/login" -Body $body

    if ($result.Success -and $result.Data.accessToken) {
        $Global:AUTH_TOKENS[$user] = $result.Data.accessToken
        Write-Color "Green" "✅ $user Token: $($result.Data.accessToken.Substring(0,30))..."
    }
    else {
        Write-Color "Red" "❌ $user login failed"
        if ($result.Error) { Write-Host "Error: $($result.Error)" }
    }
    Write-Host ""
}

if (-not $Global:AUTH_TOKENS['partner']) {
    Write-Color "Red" "❌ Partner login required for subsequent steps"
    exit 1
}

# =============================================================================
# STEP 2 : PARTNER crée une catégorie
# =============================================================================
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Color "Blue" "  STEP 2 : PARTNER crée une catégorie QUIZ"
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Host ""

$categoryBody = @{
    name = "Culture Générale - Café Lune"
    description = "Quiz culture générale pour les clients du café"
    type = "QUIZ"
    visibility = "PUBLIC"
    tags = @("culture", "quiz", "cafe")
    metadata = @{ difficulty = "mixed"; estimatedTime = 10 }
} | ConvertTo-Json -Compress -Depth 3

$categoryResult = Invoke-ApiRequest `
    -Method POST `
    -Uri "$BASE/categories/partner" `
    -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['partner'])" } `
    -Body $categoryBody

if ($categoryResult.Success) {
    $Global:CATEGORY_ID = $categoryResult.Data.data.id
    Write-Color "Green" "✅ Category ID: $($Global:CATEGORY_ID)"
}
else {
    Write-Color "Red" "❌ Category creation failed"
    exit 1
}
Write-Host ""

# =============================================================================
# STEP 3 : PARTNER ajoute des questions
# =============================================================================
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Color "Blue" "  STEP 3 : PARTNER ajoute 5 questions"
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Host ""

$Global:CONTENT_IDS = @()

for ($i = 1; $i -le 5; $i++) {
    Write-Host "❓ Adding Question $i..."

    $options = @(
        @{ text = "Capitale$i"; isCorrect = $true },
        @{ text = "FauxA$i"; isCorrect = $false },
        @{ text = "FauxB$i"; isCorrect = $false },
        @{ text = "FauxC$i"; isCorrect = $false }
    )

    $contentBody = @{
        contentType = "QUESTION"
        title = "Question $i : Quelle est la capitale du pays n°$i ?"
        correctAnswer = "Capitale$i"
        options = $options
        points = $i * 10
        timeLimit = 30
        difficulty = "EASY"
        order = $i - 1
    } | ConvertTo-Json -Compress -Depth 5

    $contentResult = Invoke-ApiRequest `
        -Method POST `
        -Uri "$BASE/categories/$($Global:CATEGORY_ID)/content" `
        -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['partner'])" } `
        -Body $contentBody

    if ($contentResult.Success -and $contentResult.Data.data.id) {
        $Global:CONTENT_IDS += $contentResult.Data.data.id
        Write-Color "Green" "  ✅ Q$i ID: $($contentResult.Data.data.id)"
    }
}
Write-Host ""

# =============================================================================
# STEP 4 : ADMIN approuve la catégorie
# =============================================================================
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Color "Blue" "  STEP 4 : ADMIN approuve la catégorie"
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Host ""

$approveBody = @{ status = "APPROVED" } | ConvertTo-Json -Compress
$approveResult = Invoke-ApiRequest `
    -Method PATCH `
    -Uri "$BASE/categories/admin/$($Global:CATEGORY_ID)/approve" `
    -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['admin'])" } `
    -Body $approveBody

if ($approveResult.Success) {
    Write-Color "Green" "✅ Category approved successfully"
}
else {
    Write-Color "Red" "❌ Category approval failed"
}
Write-Host ""

# =============================================================================
# STEP 5 : PARTNER crée EVENT SIMPLE
# =============================================================================
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Color "Blue" "  STEP 5 : PARTNER crée un EVENT SIMPLE"
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Host ""

$simpleEventBody = @{
    title = "Quiz du Soir - Simple"
    description = "Testez vos connaissances!"
    eventType = "SIMPLE"
    categoryId = $Global:CATEGORY_ID
    visibility = "PUBLIC"
    questionTimeLimit = 30
    maxParticipants = 50
} | ConvertTo-Json -Compress

$simpleEventResult = Invoke-ApiRequest `
    -Method POST `
    -Uri "$BASE/events/partner" `
    -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['partner'])" } `
    -Body $simpleEventBody

if ($simpleEventResult.Success) {
    $Global:SIMPLE_EVENT_ID = $simpleEventResult.Data.data.id
    Write-Color "Green" "✅ Simple Event ID: $($Global:SIMPLE_EVENT_ID)"
}
else {
    Write-Color "Red" "❌ Simple Event creation failed"
    exit 1
}
Write-Host ""

# =============================================================================
# STEP 6 : PARTNER crée EVENT LIVE
# =============================================================================
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Color "Blue" "  STEP 6 : PARTNER crée un EVENT LIVE"
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Host ""

$scheduledAt = (Get-Date).AddMinutes(2).ToString("yyyy-MM-ddTHH:mm:ss")
Write-Host "Scheduled at: $scheduledAt"

$liveEventBody = @{
    title = "Grand Quiz Live - Café de la Lune"
    description = "Event live en temps réel, soyez prêts!"
    eventType = "LIVE"
    categoryId = $Global:CATEGORY_ID
    visibility = "PUBLIC"
    scheduledAt = $scheduledAt
    questionTimeLimit = 20
    maxParticipants = 100
} | ConvertTo-Json -Compress

$liveEventResult = Invoke-ApiRequest `
    -Method POST `
    -Uri "$BASE/events/partner" `
    -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['partner'])" } `
    -Body $liveEventBody

if ($liveEventResult.Success -and $liveEventResult.Data.data.id) {
    $Global:LIVE_EVENT_ID = $liveEventResult.Data.data.id
    Write-Color "Green" "✅ Live Event ID: $($Global:LIVE_EVENT_ID)"
}
else {
    Write-Color "Yellow" "⚠️ Live Event creation returned unexpected response, continuing..."
    $Global:LIVE_EVENT_ID = "live-event-placeholder"
}
Write-Host ""

# =============================================================================
# STEP 7 : ALICE joue l'EVENT SIMPLE
# =============================================================================
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Color "Blue" "  STEP 7 : ALICE joue l'event simple"
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Host ""

Write-Host "🎮 Alice joining event..."
$joinResult = Invoke-ApiRequest `
    -Method POST `
    -Uri "$BASE/events/$($Global:SIMPLE_EVENT_ID)/join" `
    -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['alice'])" }

Write-Host "Join Response: $($joinResult.Data | ConvertTo-Json -Compress)"
Write-Host ""

# Loop questions
for ($i = 1; $i -le 5; $i++) {
    Write-Host "  → Question $i..."

    $questionResult = Invoke-ApiRequest `
        -Method GET `
        -Uri "$BASE/events/$($Global:SIMPLE_EVENT_ID)/question" `
        -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['alice'])" }

    $questionJson = $questionResult.Data | ConvertTo-Json -Compress
    Write-Host "  Question Response: $questionJson"

    # Check if completed
    if (-not $questionResult.Success -or -not $questionResult.Data -or $questionJson -eq "null") {
        Write-Color "Green" "  ✅ Toutes les questions terminées"
        break
    }

    $contentId = $questionResult.Data.id
    $qIndex = $questionResult.Data.questionIndex

    if (-not $contentId) {
        Write-Color "Green" "  ✅ Fin du quiz"
        break
    }

    Write-Host "    ContentID: $contentId, Index: $qIndex"

    $answerBody = @{
        contentId = $contentId
        questionIndex = $qIndex
        selectedAnswer = "Capitale$i"
    } | ConvertTo-Json -Compress

    $answerResult = Invoke-ApiRequest `
        -Method POST `
        -Uri "$BASE/events/$($Global:SIMPLE_EVENT_ID)/answer" `
        -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['alice'])" } `
        -Body $answerBody

    $answerJson = $answerResult.Data | ConvertTo-Json -Compress
    Write-Host "  Answer Response: $answerJson"

    if ($answerResult.Success) {
        $isCorrect = $answerResult.Data.correct
        $points = $answerResult.Data.pointsEarned
        $score = $answerResult.Data.currentScore
        Write-Color "Yellow" "    Correcte: $isCorrect | Points: $points | Score total: $score"
    }

    Start-Sleep -Milliseconds 500
}
Write-Host ""

# =============================================================================
# STEP 8 : BOB joue aussi
# =============================================================================
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Color "Blue" "  STEP 8 : BOB joue l'event simple"
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Host ""

Write-Host "🎮 Bob joining event..."
Invoke-ApiRequest `
    -Method POST `
    -Uri "$BASE/events/$($Global:SIMPLE_EVENT_ID)/join" `
    -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['bob'])" } | Out-Null

for ($i = 1; $i -le 5; $i++) {
    $questionResult = Invoke-ApiRequest `
        -Method GET `
        -Uri "$BASE/events/$($Global:SIMPLE_EVENT_ID)/question" `
        -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['bob'])" }

    $questionJson = $questionResult.Data | ConvertTo-Json -Compress
    if (-not $questionResult.Success -or $questionJson -eq "null") { break }

    $contentId = $questionResult.Data.id
    $qIndex = $questionResult.Data.questionIndex
    if (-not $contentId) { break }

    $answerBody = @{
        contentId = $contentId
        questionIndex = $qIndex
        selectedAnswer = "MauvaiseReponse"
    } | ConvertTo-Json -Compress

    Invoke-ApiRequest `
        -Method POST `
        -Uri "$BASE/events/$($Global:SIMPLE_EVENT_ID)/answer" `
        -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['bob'])" } `
        -Body $answerBody | Out-Null

    Write-Color "Yellow" "  Bob Q$i : réponse soumise (mauvaise réponse)"
    Start-Sleep -Milliseconds 300
}
Write-Host ""

# =============================================================================
# STEP 9 : LEADERBOARD
# =============================================================================
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Color "Blue" "  STEP 9 : Leaderboard event simple"
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Host ""

$leaderboardResult = Invoke-ApiRequest `
    -Method GET `
    -Uri "$BASE/events/$($Global:SIMPLE_EVENT_ID)/leaderboard" `
    -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['alice'])" }

Write-Host "Leaderboard: $($leaderboardResult.Data | ConvertTo-Json -Compress)"
Write-Host ""

# =============================================================================
# STEP 10 : SCORES
# =============================================================================
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Color "Blue" "  STEP 10 : Scores utilisateurs"
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Host ""

Write-Host "⭐ Alice score:"
$aliceScore = Invoke-ApiRequest `
    -Method GET `
    -Uri "$BASE/scores/me" `
    -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['alice'])" }
Write-Host ($aliceScore.Data | ConvertTo-Json)
Write-Host ""

Write-Host "⭐ Bob score:"
$bobScore = Invoke-ApiRequest `
    -Method GET `
    -Uri "$BASE/scores/me" `
    -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['bob'])" }
Write-Host ($bobScore.Data | ConvertTo-Json)
Write-Host ""

Write-Host "⭐ Leaderboard global:"
$globalLeaderboard = Invoke-ApiRequest `
    -Method GET `
    -Uri "$BASE/scores/global?page=0&size=5" `
    -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['alice'])" }
Write-Host ($globalLeaderboard.Data | ConvertTo-Json)
Write-Host ""

# =============================================================================
# STEP 11 : LIVE EVENT
# =============================================================================
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Color "Blue" "  STEP 11 : PARTNER lance le LIVE event"
Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
Write-Host ""

if ($Global:LIVE_EVENT_ID -and $Global:LIVE_EVENT_ID -ne "live-event-placeholder") {
    $launchResult = Invoke-ApiRequest `
        -Method POST `
        -Uri "$BASE/events/partner/$($Global:LIVE_EVENT_ID)/launch" `
        -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['partner'])" }

    Write-Host "Launch Response: $($launchResult.Data | ConvertTo-Json -Compress)"

    if ($launchResult.Success) {
        Write-Color "Green" "✅ Live Event launched successfully"

        # Step 12: Alice joins live
        Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
        Write-Color "Blue" "  STEP 12 : ALICE rejoint le LIVE event"
        Write-Color "Blue" "═══════════════════════════════════════════════════════════════"
        Write-Host ""

        $joinLiveResult = Invoke-ApiRequest `
            -Method POST `
            -Uri "$BASE/events/$($Global:LIVE_EVENT_ID)/join" `
            -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['alice'])" }

        Write-Host "Join Live Response: $($joinLiveResult.Data | ConvertTo-Json -Compress)"

        $liveStateResult = Invoke-ApiRequest `
            -Method GET `
            -Uri "$BASE/events/$($Global:LIVE_EVENT_ID)/state" `
            -Headers @{ Authorization = "Bearer $($Global:AUTH_TOKENS['alice'])" }

        Write-Host "Live State: $($liveStateResult.Data | ConvertTo-Json -Compress)"
    }
}
else {
    Write-Color "Yellow" "⚠️ Skipping LIVE event steps (creation failed or not available)"
}

# =============================================================================
# RÉSULTAT FINAL
# =============================================================================
Write-Host ""
Write-Color "Green" "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Color "Green" "  ✅ SCÉNARIO COMPLET TERMINÉ"
Write-Color "Green" "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host ""
Write-Color "Yellow" "Résumé:"
Write-Host "  - Catégorie créée: $($Global:CATEGORY_ID)"
Write-Host "  - Event Simple créé: $($Global:SIMPLE_EVENT_ID)"
Write-Host "  - Event Live créé: $($Global:LIVE_EVENT_ID)"
Write-Host ""
Write-Color "Yellow" "Tokens pour tests manuels:"
Write-Host "  Admin:  $($Global:AUTH_TOKENS['admin'].Substring(0,50))..."
Write-Host "  Partner: $($Global:AUTH_TOKENS['partner'].Substring(0,50))..."
Write-Host "  Alice:  $($Global:AUTH_TOKENS['alice'].Substring(0,50))..."
Write-Host "  Bob:    $($Global:AUTH_TOKENS['bob'].Substring(0,50))..."
Write-Host ""

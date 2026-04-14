# =============================================================================
# TEST COMPLET - Events Simple & Live avec détection d'erreurs
# =============================================================================
# Usage: powershell -ExecutionPolicy Bypass -File .\test_events_complete.ps1
# =============================================================================

$BASE = "http://localhost:8080/api"
$TOKENS = @{}
$IDS = @{}

function Write-Step($Step) {
    Write-Host "" 
    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "  $Step" -ForegroundColor Cyan
    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
}

function Test-ApiCall($Method, $Uri, $Headers, $Body, $Description) {
    Write-Host "  → $Description ..." -NoNewline
    try {
        $params = @{ Method = $Method; Uri = $Uri; Headers = $Headers; ContentType = "application/json" }
        if ($Body) { $params.Body = $Body }
        $response = Invoke-RestMethod @params
        Write-Host " ✅ SUCCESS" -ForegroundColor Green
        return @{ Success = $true; Data = $response }
    }
    catch {
        $status = $_.Exception.Response.StatusCode.value__
        $errorMsg = $_.Exception.Message
        Write-Host " ❌ FAILED ($status)" -ForegroundColor Red
        Write-Host "     Error: $errorMsg" -ForegroundColor Red
        return @{ Success = $false; Status = $status; Error = $errorMsg }
    }
}

# =============================================================================
# STEP 1 : LOGIN
# =============================================================================
Write-Step "STEP 1 : LOGIN"

$logins = @(
    @{ Name = "Admin"; Email = "hazem@admin.com"; Password = "12345678" },
    @{ Name = "Partner"; Email = "client@gmail.com"; Password = "12345678" },
    @{ Name = "User"; Email = "anwer@gmail.com"; Password = "12345678" }
)

foreach ($login in $logins) {
    $body = ($login | ConvertTo-Json -Compress).Replace('"Name"', '"name"').Replace('"Email"', '"email"').Replace('"Password"', '"password"')
    $body = "{`"email`":`"$($login.Email)`",`"password`":`"$($login.Password)`"}"
    
    $result = Test-ApiCall -Method POST -Uri "$BASE/auth/login" -Body $body -Description "Login $($login.Name)"
    
    if ($result.Success -and $result.Data.data.accessToken) {
        $TOKENS[$login.Name] = $result.Data.data.accessToken
        Write-Host "     Token: $($result.Data.data.accessToken.Substring(0,40))..." -ForegroundColor Gray
    } else {
        Write-Host "     CRITICAL: Cannot proceed without $($login.Name) token" -ForegroundColor Red
        exit 1
    }
}

# =============================================================================
# STEP 2 : PARTNER crée une catégorie
# =============================================================================
Write-Step "STEP 2 : PARTNER crée une catégorie QUIZ"

$catBody = @{
    name = "Test Quiz Event"
    description = "Catégorie pour tester les events"
    type = "QUIZ"
    visibility = "PUBLIC"
    tags = @("test", "quiz")
} | ConvertTo-Json -Compress -Depth 3

$catResult = Test-ApiCall -Method POST -Uri "$BASE/categories/partner" `
    -Headers @{ Authorization = "Bearer $($TOKENS['Partner'])" } `
    -Body $catBody `
    -Description "Create Category"

if ($catResult.Success -and $catResult.Data.data.id) {
    $IDS['Category'] = $catResult.Data.data.id
    Write-Host "     Category ID: $($IDS['Category'])" -ForegroundColor Yellow
} else {
    Write-Host "     ERROR: Failed to create category" -ForegroundColor Red
    exit 1
}

# =============================================================================
# STEP 3 : PARTNER ajoute des questions
# =============================================================================
Write-Step "STEP 3 : PARTNER ajoute 3 questions"

for ($i = 1; $i -le 3; $i++) {
    $qBody = @{
        contentType = "QUESTION"
        title = "Question $i : Quelle est la réponse ?"
        correctAnswer = "Reponse$i"
        options = @(
            @{ text = "Reponse$i"; isCorrect = $true },
            @{ text = "FauxA"; isCorrect = $false },
            @{ text = "FauxB"; isCorrect = $false }
        )
        points = $i * 10
        timeLimit = 30
        difficulty = "EASY"
        order = $i - 1
    } | ConvertTo-Json -Compress -Depth 5

    $qResult = Test-ApiCall -Method POST -Uri "$BASE/categories/$($IDS['Category'])/content" `
        -Headers @{ Authorization = "Bearer $($TOKENS['Partner'])" } `
        -Body $qBody `
        -Description "Add Question $i"

    if ($qResult.Success -and $qResult.Data.data.id) {
        if (-not $IDS['Questions']) { $IDS['Questions'] = @() }
        $IDS['Questions'] += $qResult.Data.data.id
        Write-Host "     Content ID: $($qResult.Data.data.id)" -ForegroundColor Gray
    }
}

# =============================================================================
# STEP 4 : ADMIN approuve la catégorie
# =============================================================================
Write-Step "STEP 4 : ADMIN approuve la catégorie"

$approveBody = @{ status = "APPROVED" } | ConvertTo-Json -Compress
$approveResult = Test-ApiCall -Method PATCH -Uri "$BASE/categories/admin/$($IDS['Category'])/approve" `
    -Headers @{ Authorization = "Bearer $($TOKENS['Admin'])" } `
    -Body $approveBody `
    -Description "Approve Category"

if (-not $approveResult.Success) {
    Write-Host "     WARNING: Approval failed, continuing..." -ForegroundColor Yellow
}

# =============================================================================
# STEP 5 : PARTNER crée EVENT SIMPLE
# =============================================================================
Write-Step "STEP 5 : PARTNER crée un EVENT SIMPLE"

$simpleBody = @{
    title = "Quiz Simple Test"
    description = "Event simple pour test"
    eventType = "SIMPLE"
    categoryId = $IDS['Category']
    visibility = "PUBLIC"
    questionTimeLimit = 30
    maxParticipants = 50
} | ConvertTo-Json -Compress

$simpleResult = Test-ApiCall -Method POST -Uri "$BASE/events/partner" `
    -Headers @{ Authorization = "Bearer $($TOKENS['Partner'])" } `
    -Body $simpleBody `
    -Description "Create SIMPLE Event"

if ($simpleResult.Success -and $simpleResult.Data.data.id) {
    $IDS['SimpleEvent'] = $simpleResult.Data.data.id
    Write-Host "     Simple Event ID: $($IDS['SimpleEvent'])" -ForegroundColor Yellow
} else {
    Write-Host "     ERROR: Failed to create simple event" -ForegroundColor Red
}

# =============================================================================
# STEP 6 : PARTNER crée EVENT LIVE
# =============================================================================
Write-Step "STEP 6 : PARTNER crée un EVENT LIVE"

$scheduledAt = (Get-Date).AddMinutes(2).ToString("yyyy-MM-ddTHH:mm:ss")
$liveBody = @{
    title = "Quiz Live Test"
    description = "Event live pour test"
    eventType = "LIVE"
    categoryId = $IDS['Category']
    visibility = "PUBLIC"
    scheduledAt = $scheduledAt
    questionTimeLimit = 20
    maxParticipants = 100
} | ConvertTo-Json -Compress

$liveResult = Test-ApiCall -Method POST -Uri "$BASE/events/partner" `
    -Headers @{ Authorization = "Bearer $($TOKENS['Partner'])" } `
    -Body $liveBody `
    -Description "Create LIVE Event"

if ($liveResult.Success -and $liveResult.Data.data.id) {
    $IDS['LiveEvent'] = $liveResult.Data.data.id
    Write-Host "     Live Event ID: $($IDS['LiveEvent'])" -ForegroundColor Yellow
    Write-Host "     Scheduled at: $scheduledAt" -ForegroundColor Gray
} else {
    Write-Host "     WARNING: Live event creation failed" -ForegroundColor Yellow
}

# =============================================================================
# STEP 7 : USER rejoint et joue l'EVENT SIMPLE
# =============================================================================
Write-Step "STEP 7 : USER rejoint et joue l'EVENT SIMPLE"

# Join event
$joinResult = Test-ApiCall -Method POST -Uri "$BASE/events/$($IDS['SimpleEvent'])/join" `
    -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
    -Description "User joins Simple Event"

if (-not $joinResult.Success) {
    Write-Host "     ERROR: Cannot join event" -ForegroundColor Red
}

# Play questions
for ($i = 1; $i -le 3; $i++) {
    # Get question
    $qResult = Test-ApiCall -Method GET -Uri "$BASE/events/$($IDS['SimpleEvent'])/question" `
        -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
        -Description "Get Question $i"

    if (-not $qResult.Success -or -not $qResult.Data) {
        Write-Host "     No more questions or error" -ForegroundColor Yellow
        break
    }

    $contentId = $qResult.Data.id
    $qIndex = $qResult.Data.questionIndex
    Write-Host "     Question $i - ContentID: $contentId, Index: $qIndex" -ForegroundColor Gray

    # Submit answer
    $ansBody = @{
        contentId = $contentId
        questionIndex = $qIndex
        selectedAnswer = "Reponse$i"
    } | ConvertTo-Json -Compress

    $ansResult = Test-ApiCall -Method POST -Uri "$BASE/events/$($IDS['SimpleEvent'])/answer" `
        -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
        -Body $ansBody `
        -Description "Submit Answer $i"

    if ($ansResult.Success) {
        $isCorrect = $ansResult.Data.correct
        $points = $ansResult.Data.pointsEarned
        $score = $ansResult.Data.currentScore
        Write-Host "     Result: Correct=$isCorrect, Points=$points, TotalScore=$score" -ForegroundColor $(if ($isCorrect) { "Green" } else { "Red" })
    }
}

# =============================================================================
# STEP 8 : LEADERBOARD
# =============================================================================
Write-Step "STEP 8 : LEADERBOARD"

$lbResult = Test-ApiCall -Method GET -Uri "$BASE/events/$($IDS['SimpleEvent'])/leaderboard" `
    -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
    -Description "Get Leaderboard"

if ($lbResult.Success) {
    $entries = $lbResult.Data.entries
    Write-Host "     Leaderboard entries: $($entries.Count)" -ForegroundColor Yellow
    foreach ($entry in $entries | Select-Object -First 3) {
        Write-Host "     #$($entry.rank) - $($entry.username): $($entry.score) points" -ForegroundColor Gray
    }
}

# =============================================================================
# STEP 9 : SCORES USER
# =============================================================================
Write-Step "STEP 9 : SCORES USER"

$scoreResult = Test-ApiCall -Method GET -Uri "$BASE/scores/me" `
    -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
    -Description "Get User Score"

if ($scoreResult.Success) {
    Write-Host "     Total Score: $($scoreResult.Data.totalScore)" -ForegroundColor Yellow
    Write-Host "     Total Events: $($scoreResult.Data.totalEvents)" -ForegroundColor Gray
}

# =============================================================================
# STEP 10 : TEST LIVE EVENT (si créé)
# =============================================================================
if ($IDS['LiveEvent']) {
    Write-Step "STEP 10 : TEST LIVE EVENT"
    
    # Launch live event
    $launchResult = Test-ApiCall -Method POST -Uri "$BASE/events/partner/$($IDS['LiveEvent'])/launch" `
        -Headers @{ Authorization = "Bearer $($TOKENS['Partner'])" } `
        -Description "Launch Live Event"

    # Join live event
    $joinLiveResult = Test-ApiCall -Method POST -Uri "$BASE/events/$($IDS['LiveEvent'])/join" `
        -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
        -Description "User joins Live Event"

    # Get state
    $stateResult = Test-ApiCall -Method GET -Uri "$BASE/events/$($IDS['LiveEvent'])/state" `
        -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
        -Description "Get Live Event State"
    
    if ($stateResult.Success) {
        Write-Host "     Status: $($stateResult.Data.status)" -ForegroundColor Yellow
        Write-Host "     Current Question: $($stateResult.Data.currentQuestionIndex)" -ForegroundColor Gray
    }
}

# =============================================================================
# RÉSUMÉ FINAL
# =============================================================================
Write-Step "RÉSUMÉ FINAL - TEST COMPLET"

Write-Host "  IDs créés:" -ForegroundColor Yellow
foreach ($key in $IDS.Keys) {
    $value = $IDS[$key]
    if ($value -is [array]) {
        Write-Host "    $key : $($value.Count) items" -ForegroundColor Gray
    } else {
        Write-Host "    $key : $value" -ForegroundColor Gray
    }
}

Write-Host "" 
Write-Host "  ✅ TESTS TERMINÉS" -ForegroundColor Green
Write-Host "" 

# Check for any critical failures
$hasErrors = $false
if (-not $IDS['SimpleEvent']) { 
    Write-Host "  ❌ CRITICAL: Simple Event creation failed" -ForegroundColor Red
    $hasErrors = $true
}
if (-not $IDS['LiveEvent']) { 
    Write-Host "  ⚠️  WARNING: Live Event creation failed (optional)" -ForegroundColor Yellow
}

if ($hasErrors) {
    Write-Host "" 
    Write-Host "  Des erreurs critiques ont été détectées." -ForegroundColor Red
    Write-Host "  Vérifiez les logs au-dessus pour les détails." -ForegroundColor Red
} else {
    Write-Host "" 
    Write-Host "  Tous les tests critiques ont réussi !" -ForegroundColor Green
}

# =============================================================================
# TEST COMPLET - Scénario Event Simple + Live + Gameplay + Scores
# =============================================================================
# Usage: powershell -ExecutionPolicy Bypass -File .\full_scenario_debug.ps1
# =============================================================================

$BASE = "http://localhost:8080/api"
$TOKENS = @{}
$IDS = @{}
$SCORES = @{}

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
        return @{ Success = $true; Data = $response; Raw = $response | ConvertTo-Json -Depth 5 }
    }
    catch {
        $status = $_.Exception.Response.StatusCode.value__
        $errorMsg = $_.Exception.Message
        Write-Host " ❌ FAILED (HTTP $status)" -ForegroundColor Red
        
        # Try to read error response
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $reader.BaseStream.Position = 0
            $reader.DiscardBufferedData()
            $errorBody = $reader.ReadToEnd()
            Write-Host "     Response: $errorBody" -ForegroundColor Red
        } catch { }
        
        return @{ Success = $false; Status = $status; Error = $errorMsg }
    }
}

# =============================================================================
# STEP 1 : LOGIN ALL USERS
# =============================================================================
Write-Step "STEP 1 : LOGIN (Admin, Partner, User)"

$users = @(
    @{ Name = "Admin"; Email = "hazem@admin.com"; Password = "12345678" },
    @{ Name = "Partner"; Email = "client@gmail.com"; Password = "12345678" },
    @{ Name = "User"; Email = "anwer@gmail.com"; Password = "12345678" }
)

foreach ($user in $users) {
    $body = "{`"email`":`"$($user.Email)`",`"password`":`"$($user.Password)`"}"
    $result = Test-ApiCall -Method POST -Uri "$BASE/auth/login" -Body $body -Description "Login $($user.Name)"
    
    if ($result.Success) {
        $TOKENS[$user.Name] = $result.Data.data.accessToken
        # Decode JWT to show role
        $tokenParts = $TOKENS[$user.Name].Split('.')
        $payload = $tokenParts[1]
        while ($payload.Length % 4) { $payload += '=' }
        $payloadJson = [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($payload)) | ConvertFrom-Json
        Write-Host "     Token Role: $($payloadJson.role)" -ForegroundColor Gray
    }
}

if (-not $TOKENS['Partner'] -or -not $TOKENS['User']) {
    Write-Host "CRITICAL: Login failed" -ForegroundColor Red
    exit 1
}

# =============================================================================
# STEP 2 : PARTNER crée une catégorie
# =============================================================================
Write-Step "STEP 2 : PARTNER crée une catégorie QUIZ"

$catBody = @{
    name = "Test Quiz Complet"
    description = "Catégorie pour test complet"
    type = "QUIZ"
    visibility = "PUBLIC"
    tags = @("test", "complet", "demo")
} | ConvertTo-Json -Compress

$catResult = Test-ApiCall -Method POST -Uri "$BASE/categories/partner" `
    -Headers @{ Authorization = "Bearer $($TOKENS['Partner'])" } `
    -Body $catBody `
    -Description "Create Category"

# FIX: Vérifier la structure de réponse de Category
Write-Host "DEBUG Cat Response: $($catResult.Raw)" -ForegroundColor Magenta
if ($catResult.Success -and $catResult.Data.data) {
    $IDS['Category'] = $catResult.Data.data.id
    Write-Host "     Category ID: $($IDS['Category'])" -ForegroundColor Yellow
} elseif ($catResult.Success -and $catResult.Data.id) {
    $IDS['Category'] = $catResult.Data.id
    Write-Host "     Category ID: $($IDS['Category'])" -ForegroundColor Yellow
}

# =============================================================================
# STEP 3 : PARTNER ajoute 3 questions
# =============================================================================
Write-Step "STEP 3 : PARTNER ajoute 3 questions"

for ($i = 1; $i -le 3; $i++) {
    $qBody = @{
        contentType = "QUESTION"
        title = "Question $i : Quelle est la réponse $i ?"
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
}

# =============================================================================
# STEP 4 : ADMIN approuve la catégorie
# =============================================================================
Write-Step "STEP 4 : ADMIN approuve la catégorie"

$approveBody = @{ status = "APPROVED" } | ConvertTo-Json -Compress
Test-ApiCall -Method PATCH -Uri "$BASE/categories/admin/$($IDS['Category'])/approve" `
    -Headers @{ Authorization = "Bearer $($TOKENS['Admin'])" } `
    -Body $approveBody `
    -Description "Approve Category"

# =============================================================================
# STEP 5 : PARTNER crée EVENT SIMPLE
# =============================================================================
Write-Step "STEP 5 : PARTNER crée un EVENT SIMPLE"

$simpleBody = @{
    title = "Quiz Simple Demo"
    description = "Event simple pour démo"
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

Write-Host "DEBUG Response: $($simpleResult.Raw)" -ForegroundColor Magenta

# FIX: La réponse est directement EventDto, pas wrappée dans { data: {...} }
if ($simpleResult.Success -and $simpleResult.Data.id) {
    $IDS['SimpleEvent'] = $simpleResult.Data.id
    Write-Host "     Simple Event ID: $($IDS['SimpleEvent'])" -ForegroundColor Yellow
    Write-Host "     Event Status: $($simpleResult.Data.status)" -ForegroundColor Gray
} else {
    Write-Host "     ERROR: Data structure mismatch or creation failed" -ForegroundColor Red
}

# =============================================================================
# STEP 6 : PARTNER crée EVENT LIVE
# =============================================================================
Write-Step "STEP 6 : PARTNER crée un EVENT LIVE"

$scheduledAt = (Get-Date).AddMinutes(5).ToString("yyyy-MM-ddTHH:mm:ss")
$liveBody = @{
    title = "Quiz Live Demo"
    description = "Event live pour démo"
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

# FIX: Réponse directe, pas wrappée dans data
if ($liveResult.Success -and $liveResult.Data.id) {
    $IDS['LiveEvent'] = $liveResult.Data.id
    Write-Host "     Live Event ID: $($IDS['LiveEvent'])" -ForegroundColor Yellow
    Write-Host "     Scheduled At: $scheduledAt" -ForegroundColor Gray
}

# =============================================================================
# STEP 7 : USER rejoint et joue l'EVENT SIMPLE
# =============================================================================
Write-Step "STEP 7 : USER rejoint et joue l'EVENT SIMPLE"

if (-not $IDS['SimpleEvent']) {
    Write-Host "  ❌ SKIPPED: No Simple Event ID" -ForegroundColor Yellow
} else {
    # Join event
    $joinResult = Test-ApiCall -Method POST -Uri "$BASE/events/$($IDS['SimpleEvent'])/join" `
        -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
        -Description "7.1 User joins Simple Event"
    
    if ($joinResult.Success) {
        Write-Host "     Participant ID: $($joinResult.Data.participantId)" -ForegroundColor Gray
        $IDS['Participant'] = $joinResult.Data.participantId
    }
    
    # Play questions
    $totalScore = 0
    for ($i = 1; $i -le 3; $i++) {
        # Get question
        $qResult = Test-ApiCall -Method GET -Uri "$BASE/events/$($IDS['SimpleEvent'])/question" `
            -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
            -Description "7.$i Get Question $i"
        
        if (-not $qResult.Success -or -not $qResult.Data) {
            Write-Host "     No more questions" -ForegroundColor Yellow
            break
        }
        
        $contentId = $qResult.Data.id
        $qIndex = $qResult.Data.questionIndex
        Write-Host "       ContentID: $contentId | Index: $qIndex" -ForegroundColor Gray
        
        # Submit answer
        $ansBody = @{
            contentId = $contentId
            questionIndex = $qIndex
            selectedAnswer = "Reponse$i"
        } | ConvertTo-Json -Compress
        
        $ansResult = Test-ApiCall -Method POST -Uri "$BASE/events/$($IDS['SimpleEvent'])/answer" `
            -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
            -Body $ansBody `
            -Description "     Submit Answer $i"
        
        if ($ansResult.Success) {
            $isCorrect = $ansResult.Data.correct
            $points = $ansResult.Data.pointsEarned
            $totalScore = $ansResult.Data.currentScore
            Write-Host "         Result: $(if($isCorrect){'✅ CORRECT'}else{'❌ WRONG'}) | +$points pts | Total: $totalScore" -ForegroundColor $(if($isCorrect){"Green"}else{"Red"})
        }
    }
    
    $SCORES['UserSimple'] = $totalScore
    Write-Host "  Final Simple Score: $totalScore" -ForegroundColor Cyan
}

# =============================================================================
# STEP 8 : TEST LIVE EVENT
# =============================================================================
if ($IDS['LiveEvent']) {
    Write-Step "STEP 8 : TEST EVENT LIVE"
    
    # 8.1 Launch live event
    $launchResult = Test-ApiCall -Method POST -Uri "$BASE/events/partner/$($IDS['LiveEvent'])/launch" `
        -Headers @{ Authorization = "Bearer $($TOKENS['Partner'])" } `
        -Description "8.1 Launch Live Event"
    
    if ($launchResult.Success) {
        Write-Host "     Live Event Status: $($launchResult.Data.status)" -ForegroundColor Yellow
    }
    
    # 8.2 User join live event
    $joinLiveResult = Test-ApiCall -Method POST -Uri "$BASE/events/$($IDS['LiveEvent'])/join" `
        -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
        -Description "8.2 User joins Live Event"
    
    # 8.3 Get live event state
    $stateResult = Test-ApiCall -Method GET -Uri "$BASE/events/$($IDS['LiveEvent'])/state" `
        -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
        -Description "8.3 Get Live Event State"
    
    if ($stateResult.Success) {
        Write-Host "     Status: $($stateResult.Data.status)" -ForegroundColor Yellow
        Write-Host "     Current Question: $($stateResult.Data.currentQuestionIndex)" -ForegroundColor Gray
    }
}

# =============================================================================
# STEP 9 : LEADERBOARD EVENT SIMPLE
# =============================================================================
Write-Step "STEP 9 : LEADERBOARD"

if ($IDS['SimpleEvent']) {
    $lbResult = Test-ApiCall -Method GET -Uri "$BASE/events/$($IDS['SimpleEvent'])/leaderboard" `
        -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
        -Description "Get Simple Event Leaderboard"
    
    if ($lbResult.Success -and $lbResult.Data.entries) {
        Write-Host "  Leaderboard ($($lbResult.Data.entries.Count) entries):" -ForegroundColor Yellow
        foreach ($entry in $lbResult.Data.entries | Select-Object -First 5) {
            Write-Host "    #$($entry.rank) - $($entry.username): $($entry.score) pts" -ForegroundColor Gray
        }
    }
}

# =============================================================================
# STEP 10 : SCORES
# =============================================================================
Write-Step "STEP 10 : SCORES UTILISATEUR"

$scoreResult = Test-ApiCall -Method GET -Uri "$BASE/scores/me" `
    -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
    -Description "Get User Score"

if ($scoreResult.Success) {
    Write-Host "  Total Score: $($scoreResult.Data.totalScore)" -ForegroundColor Green
    Write-Host "  Total Events: $($scoreResult.Data.totalEvents)" -ForegroundColor Gray
    Write-Host "  Rank: $($scoreResult.Data.globalRank)" -ForegroundColor Gray
}

# Global leaderboard
$globalResult = Test-ApiCall -Method GET -Uri "$BASE/scores/global?page=0&size=5" `
    -Headers @{ Authorization = "Bearer $($TOKENS['User'])" } `
    -Description "Get Global Leaderboard"

if ($globalResult.Success -and $globalResult.Data.content) {
    Write-Host "  Global Top 5:" -ForegroundColor Yellow
    foreach ($entry in $globalResult.Data.content | Select-Object -First 5) {
        Write-Host "    #$($entry.rank) - $($entry.username): $($entry.totalScore) pts (Events: $($entry.totalEvents))" -ForegroundColor Gray
    }
}

# =============================================================================
# RÉSUMÉ FINAL
# =============================================================================
Write-Step "RÉSUMÉ FINAL"

Write-Host "  Objets créés:" -ForegroundColor Yellow
Write-Host "    Category ID: $($IDS['Category'])" -ForegroundColor Gray
Write-Host "    Simple Event ID: $($IDS['SimpleEvent'])" -ForegroundColor Gray
Write-Host "    Live Event ID: $($IDS['LiveEvent'])" -ForegroundColor Gray
Write-Host "    Participant ID: $($IDS['Participant'])" -ForegroundColor Gray

Write-Host "" 
Write-Host "  Scores:" -ForegroundColor Yellow
Write-Host "    Simple Event Score: $($SCORES['UserSimple'])" -ForegroundColor Gray

Write-Host "" 

# Check success
$success = $true
if (-not $IDS['SimpleEvent']) { 
    Write-Host "  ❌ Simple Event: FAILED" -ForegroundColor Red
    $success = $false
} else {
    Write-Host "  ✅ Simple Event: CREATED" -ForegroundColor Green
}

if (-not $IDS['LiveEvent']) { 
    Write-Host "  ❌ Live Event: FAILED" -ForegroundColor Red
    $success = $false
} else {
    Write-Host "  ✅ Live Event: CREATED" -ForegroundColor Green
}

if ($SCORES['UserSimple'] -eq 0) {
    Write-Host "  ❌ Gameplay: NO POINTS SCORED" -ForegroundColor Red
} else {
    Write-Host "  ✅ Gameplay: $($SCORES['UserSimple']) POINTS" -ForegroundColor Green
}

Write-Host "" 
if ($success) {
    Write-Host "  🎉 SCÉNARIO COMPLET RÉUSSI !" -ForegroundColor Green
} else {
    Write-Host "  ⚠️ CERTAINES ÉTAPES ONT ÉCHOUÉ" -ForegroundColor Yellow
}

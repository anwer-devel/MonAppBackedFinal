# Debug test - étape par étape
$BASE = "http://localhost:8080/api"

Write-Host "=== DEBUG TEST ===" -ForegroundColor Cyan

# 1. Login Partner
Write-Host "`n1. LOGIN PARTNER (client@gmail.com)" -ForegroundColor Yellow
$loginBody = '{"email":"client@gmail.com","password":"12345678"}'
Write-Host "Request: POST $BASE/auth/login"
Write-Host "Body: $loginBody"

try {
    $response = Invoke-RestMethod -Uri "$BASE/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    Write-Host "✅ Login SUCCESS" -ForegroundColor Green
    $partnerToken = $response.data.accessToken
    Write-Host "Token: $($partnerToken.Substring(0,50))..."
    
    # Vérifier le rôle dans le token (décoder le JWT payload)
    $tokenParts = $partnerToken.Split('.')
    $payload = $tokenParts[1]
    # Ajouter le padding nécessaire pour Base64
    while ($payload.Length % 4) { $payload += '=' }
    $payloadJson = [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($payload))
    Write-Host "Token payload: $payloadJson" -ForegroundColor Gray
} catch {
    Write-Host "❌ Login FAILED" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    exit
}

# 2. Get Partner Info (vérifie que le partner existe)
Write-Host "`n2. CHECK PARTNER EXISTS" -ForegroundColor Yellow
try {
    # Essayons d'abord de récupérer les events du partner pour voir si le partner est reconnu
    $response = Invoke-RestMethod -Uri "$BASE/events/partner/mine" -Method GET -Headers @{ Authorization = "Bearer $partnerToken" }
    Write-Host "✅ Partner recognized - Events count: $($response.content.Count)" -ForegroundColor Green
} catch {
    $status = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ Partner check FAILED (HTTP $status)" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
}

# 3. Create Category
Write-Host "`n3. CREATE CATEGORY" -ForegroundColor Yellow
$catBody = '{
    "name": "Debug Test Category",
    "description": "Test",
    "type": "QUIZ",
    "visibility": "PUBLIC"
}'
Write-Host "Request: POST $BASE/categories/partner"

try {
    $response = Invoke-RestMethod -Uri "$BASE/categories/partner" -Method POST -Headers @{ Authorization = "Bearer $partnerToken" } -Body $catBody -ContentType "application/json"
    Write-Host "✅ Category Created: $($response.data.id)" -ForegroundColor Green
    $categoryId = $response.data.id
} catch {
    $status = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ Category Creation FAILED (HTTP $status)" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    exit
}

# 4. Add Question
Write-Host "`n4. ADD QUESTION" -ForegroundColor Yellow
$qBody = '{
    "contentType": "QUESTION",
    "title": "Q1",
    "correctAnswer": "A",
    "options": [{"text":"A","isCorrect":true},{"text":"B","isCorrect":false}],
    "points": 10,
    "timeLimit": 30
}'

try {
    $response = Invoke-RestMethod -Uri "$BASE/categories/$categoryId/content" -Method POST -Headers @{ Authorization = "Bearer $partnerToken" } -Body $qBody -ContentType "application/json"
    Write-Host "✅ Question Added: $($response.data.id)" -ForegroundColor Green
} catch {
    $status = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ Question Add FAILED (HTTP $status)" -ForegroundColor Red
}

# 5. Approve Category (besoin du token admin)
Write-Host "`n5. APPROVE CATEGORY (besoin login admin)" -ForegroundColor Yellow
$adminBody = '{"email":"hazem@admin.com","password":"12345678"}'
try {
    $response = Invoke-RestMethod -Uri "$BASE/auth/login" -Method POST -Body $adminBody -ContentType "application/json"
    $adminToken = $response.data.accessToken
    
    $approveBody = '{"status":"APPROVED"}'
    $response = Invoke-RestMethod -Uri "$BASE/categories/admin/$categoryId/approve" -Method PATCH -Headers @{ Authorization = "Bearer $adminToken" } -Body $approveBody -ContentType "application/json"
    Write-Host "✅ Category Approved" -ForegroundColor Green
} catch {
    $status = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ Approve FAILED (HTTP $status)" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
}

# 6. Create Event - THIS IS THE CRITICAL TEST
Write-Host "`n6. CREATE EVENT SIMPLE (TEST CRITIQUE)" -ForegroundColor Yellow
$eventBody = "{`"title`":`"Test Event`",`"description`":`"Test`",`"eventType`":`"SIMPLE`",`"categoryId`":`"$categoryId`",`"visibility`":`"PUBLIC`",`"maxParticipants`":50}"
Write-Host "Request: POST $BASE/events/partner"
Write-Host "Body: $eventBody"

try {
    $response = Invoke-RestMethod -Uri "$BASE/events/partner" -Method POST -Headers @{ Authorization = "Bearer $partnerToken" } -Body $eventBody -ContentType "application/json"
    Write-Host "✅ EVENT CREATED SUCCESS!" -ForegroundColor Green
    Write-Host "Event ID: $($response.data.id)" -ForegroundColor Yellow
} catch {
    $status = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ EVENT CREATION FAILED (HTTP $status)" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    
    # Essayer de lire le corps de la réponse d'erreur
    try {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $reader.DiscardBufferedData()
        $errorBody = $reader.ReadToEnd()
        Write-Host "Response body: $errorBody" -ForegroundColor Red
    } catch {
        Write-Host "Could not read error response body" -ForegroundColor Gray
    }
}

Write-Host "`n=== END DEBUG ===" -ForegroundColor Cyan

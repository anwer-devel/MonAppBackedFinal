# Simple test script - PowerShell 5.1 compatible
$BASE = "http://localhost:8080/api"

Write-Host "=== TEST LOGIN ===" -ForegroundColor Cyan

try {
    $body = '{"email":"hazem@admin.com","password":"12345678"}'
    $response = Invoke-RestMethod -Uri "$BASE/auth/login" -Method POST -Body $body -ContentType "application/json"
    Write-Host "Admin Login SUCCESS" -ForegroundColor Green
    Write-Host "Token: $($response.data.accessToken.Substring(0,40))..."
} catch {
    Write-Host "Admin Login FAILED: $_" -ForegroundColor Red
}

Write-Host ""

try {
    $body = '{"email":"client@gmail.com","password":"12345678"}'
    $response = Invoke-RestMethod -Uri "$BASE/auth/login" -Method POST -Body $body -ContentType "application/json"
    Write-Host "Partner Login SUCCESS" -ForegroundColor Green
    $partnerToken = $response.data.accessToken
} catch {
    Write-Host "Partner Login FAILED: $_" -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "=== TEST CREATE CATEGORY ===" -ForegroundColor Cyan

try {
    $headers = @{ Authorization = "Bearer $partnerToken" }
    $body = '{
        "name": "Test Category",
        "description": "Test description",
        "type": "QUIZ",
        "visibility": "PUBLIC"
    }'
    $response = Invoke-RestMethod -Uri "$BASE/categories/partner" -Method POST -Headers $headers -Body $body -ContentType "application/json"
    Write-Host "Category Created: $($response.data.id)" -ForegroundColor Green
} catch {
    Write-Host "Category Creation FAILED: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== DONE ===" -ForegroundColor Cyan

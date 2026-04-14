@echo off
setlocal EnableDelayedExpansion

:: =============================================================================
:: FULL SCENARIO TEST - Quiz/Event Application (Windows Batch Version)
:: =============================================================================
:: Usage: full_scenario_test.bat
:: Requires: curl (included in Windows 10/11)
:: =============================================================================

set BASE=http://localhost:8080/api
set TEMP_FILE=%TEMP%\quiz_test_response.json

echo.
echo ===============================================================================
echo   STEP 1 : LOGIN
echo ===============================================================================
echo.

:: Login Admin
echo [LOGIN] Admin...
curl -s -X POST "%BASE%/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"hazem@admin.com\",\"password\":\"12345678\"}" > %TEMP_FILE%
for /f "tokens=*" %%a in ('type %TEMP_FILE% ^| findstr "accessToken"') do (
    set ADMIN_TOKEN=%%a
    set ADMIN_TOKEN=!ADMIN_TOKEN:~15,-2!
)
echo   Admin Token: !ADMIN_TOKEN:~0,30!...

:: Login Partner
echo [LOGIN] Partner...
curl -s -X POST "%BASE%/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"partner@cafe-lune.com\",\"password\":\"Partner123!\"}" > %TEMP_FILE%
for /f "tokens=*" %%a in ('type %TEMP_FILE% ^| findstr "accessToken"') do (
    set PARTNER_TOKEN=%%a
    set PARTNER_TOKEN=!PARTNER_TOKEN:~15,-2!
)
echo   Partner Token: !PARTNER_TOKEN:~0,30!...

:: Login User (alice)
echo [LOGIN] User (alice)...
curl -s -X POST "%BASE%/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"alice@example.com\",\"password\":\"Alice123!\"}" > %TEMP_FILE%
for /f "tokens=*" %%a in ('type %TEMP_FILE% ^| findstr "accessToken"') do (
    set ALICE_TOKEN=%%a
    set ALICE_TOKEN=!ALICE_TOKEN:~15,-2!
)
echo   Alice Token: !ALICE_TOKEN:~0,30!...

:: Login Bob
echo [LOGIN] Bob...
curl -s -X POST "%BASE%/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"bob@example.com\",\"password\":\"Bob123!\"}" > %TEMP_FILE%
for /f "tokens=*" %%a in ('type %TEMP_FILE% ^| findstr "accessToken"') do (
    set BOB_TOKEN=%%a
    set BOB_TOKEN=!BOB_TOKEN:~15,-2!
)
echo   Bob Token: !BOB_TOKEN:~0,30!...

if "!PARTNER_TOKEN!"=="" (
    echo [ERROR] Partner login failed
    exit /b 1
)

echo.
echo ===============================================================================
echo   STEP 2 : PARTNER cree une categorie QUIZ
echo ===============================================================================
echo.

curl -s -X POST "%BASE%/categories/partner" -H "Authorization: Bearer !PARTNER_TOKEN!" -H "Content-Type: application/json" -d "{\"name\":\"Culture Generale - Cafe Lune\",\"description\":\"Quiz culture generale\",\"type\":\"QUIZ\",\"visibility\":\"PUBLIC\",\"tags\":[\"culture\",\"quiz\",\"cafe\"]}" > %TEMP_FILE%
type %TEMP_FILE%
echo.

:: Extract category ID (simplified parsing)
for /f "tokens=*" %%a in ('type %TEMP_FILE% ^| findstr "\"id\""') do (
    set CATEGORY_ID=%%a
    set CATEGORY_ID=!CATEGORY_ID:~6,36!
)
echo [INFO] Category ID: !CATEGORY_ID!

echo.
echo ===============================================================================
echo   STEP 3 : PARTNER ajoute 3 questions  
echo ===============================================================================
echo.

for /L %%i in (1,1,3) do (
    echo [ADD] Question %%i...
    curl -s -X POST "%BASE%/categories/!CATEGORY_ID!/content" -H "Authorization: Bearer !PARTNER_TOKEN!" -H "Content-Type: application/json" -d "{\"contentType\":\"QUESTION\",\"title\":\"Question %%i\",\"correctAnswer\":\"Reponse%%i\",\"options\":[{\"text\":\"Reponse%%i\",\"isCorrect\":true},{\"text\":\"FauxA\",\"isCorrect\":false}],\"points\":%%i0,\"timeLimit\":30,\"difficulty\":\"EASY\",\"order\":%%i}" > %TEMP_FILE%
    type %TEMP_FILE%
    echo.
)

echo.
echo ===============================================================================
echo   STEP 4 : ADMIN approuve la categorie
echo ===============================================================================
echo.

curl -s -X PATCH "%BASE%/categories/admin/!CATEGORY_ID!/approve" -H "Authorization: Bearer !ADMIN_TOKEN!" -H "Content-Type: application/json" -d "{\"status\":\"APPROVED\"}" > %TEMP_FILE%
type %TEMP_FILE%
echo.
echo [OK] Category approved

echo.
echo ===============================================================================
echo   STEP 5 : PARTNER cree un EVENT SIMPLE
echo ===============================================================================
echo.

curl -s -X POST "%BASE%/events/partner" -H "Authorization: Bearer !PARTNER_TOKEN!" -H "Content-Type: application/json" -d "{\"title\":\"Quiz du Soir\",\"description\":\"Testez vos connaissances!\",\"eventType\":\"SIMPLE\",\"categoryId\":\"!CATEGORY_ID!\",\"visibility\":\"PUBLIC\",\"questionTimeLimit\":30,\"maxParticipants\":50}" > %TEMP_FILE%
type %TEMP_FILE%
echo.

for /f "tokens=*" %%a in ('type %TEMP_FILE% ^| findstr "\"id\""') do (
    set EVENT_ID=%%a
    set EVENT_ID=!EVENT_ID:~6,36!
)
echo [INFO] Event ID: !EVENT_ID!

echo.
echo ===============================================================================
echo   STEP 6 : ALICE joue l'event
echo ===============================================================================
echo.

echo [JOIN] Alice joining event...
curl -s -X POST "%BASE%/events/!EVENT_ID!/join" -H "Authorization: Bearer !ALICE_TOKEN!"
echo.

for /L %%i in (1,1,3) do (
    echo [QUESTION %%i] Getting question...
    curl -s -X GET "%BASE%/events/!EVENT_ID!/question" -H "Authorization: Bearer !ALICE_TOKEN!" > %TEMP_FILE%
    type %TEMP_FILE%
    echo.
    
    :: Try to extract content ID and answer (simplified)
    timeout /t 1 /nobreak >nul 2>&1
    echo [ANSWER %%i] Submitting answer...
    curl -s -X POST "%BASE%/events/!EVENT_ID!/answer" -H "Authorization: Bearer !ALICE_TOKEN!" -H "Content-Type: application/json" -d "{\"contentId\":\"00000000-0000-0000-0000-000000000001\",\"questionIndex\":%%i,\"selectedAnswer\":\"Reponse%%i\"}" > %TEMP_FILE%
    type %TEMP_FILE%
    echo.
)

echo.
echo ===============================================================================
echo   STEP 7 : LEADERBOARD
echo ===============================================================================
echo.

curl -s -X GET "%BASE%/events/!EVENT_ID!/leaderboard" -H "Authorization: Bearer !ALICE_TOKEN!" > %TEMP_FILE%
type %TEMP_FILE%
echo.

echo.
echo ===============================================================================
echo   STEP 8 : SCORES
echo ===============================================================================
echo.

echo [SCORE] Alice:
curl -s -X GET "%BASE%/scores/me" -H "Authorization: Bearer !ALICE_TOKEN!"
echo.
echo.

echo.
echo [SCORE] Global Leaderboard:
curl -s -X GET "%BASE%/scores/global?page=0&size=5" -H "Authorization: Bearer !ALICE_TOKEN!"
echo.

echo.
echo ===============================================================================
echo   ✅ SCENARIO COMPLETE
echo ===============================================================================
echo.
echo Category ID: !CATEGORY_ID!
echo Event ID: !EVENT_ID!
echo.
echo Tokens:
echo   Admin: !ADMIN_TOKEN:~0,40!...
echo   Partner: !PARTNER_TOKEN:~0,40!...
echo   Alice: !ALICE_TOKEN:~0,40!...

if exist %TEMP_FILE% del %TEMP_FILE%
endlocal
pause

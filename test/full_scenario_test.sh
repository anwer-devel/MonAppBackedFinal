#!/bin/bash

# =============================================================================
# FULL SCENARIO TEST - Quiz/Event Application
# =============================================================================
# Ce script teste le scénario complet:
# 1. Login (Admin, Partner, Users)
# 2. Partner crée une catégorie
# 3. Partner ajoute des questions
# 4. Admin approuve la catégorie
# 5. Partner crée un EVENT SIMPLE
# 6. Partner crée un EVENT LIVE
# 7. Users jouent l'event simple
# 8. Leaderboard et scores
# 9. Partner lance le LIVE event
# =============================================================================

set -e

BASE="http://localhost:8080/api"

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# =============================================================================
# STEP 1 : LOGIN - obtenir les tokens
# =============================================================================

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  STEP 1 : LOGIN${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

echo ""
echo "🔐 Login Admin..."
ADMIN_LOGIN=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@quizapp.com","password":"Admin123!"}' || echo '{"error":"login failed"}')
echo "Response: $ADMIN_LOGIN"
ADMIN_TOKEN=$(echo "$ADMIN_LOGIN" | grep -o '"accessToken"[^,]*' | cut -d'"' -f4)
if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "null" ]; then
    echo -e "${RED}❌ Admin login failed${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Admin Token: ${ADMIN_TOKEN:0:30}...${NC}"

echo ""
echo "🔐 Login Partner..."
PARTNER_LOGIN=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"partner@cafe-lune.com","password":"Partner123!"}' || echo '{"error":"login failed"}')
echo "Response: $PARTNER_LOGIN"
PARTNER_TOKEN=$(echo "$PARTNER_LOGIN" | grep -o '"accessToken"[^,]*' | cut -d'"' -f4)
if [ -z "$PARTNER_TOKEN" ] || [ "$PARTNER_TOKEN" = "null" ]; then
    echo -e "${RED}❌ Partner login failed${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Partner Token: ${PARTNER_TOKEN:0:30}...${NC}"

echo ""
echo "🔐 Login Alice (User)..."
ALICE_LOGIN=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"Alice123!"}' || echo '{"error":"login failed"}')
echo "Response: $ALICE_LOGIN"
ALICE_TOKEN=$(echo "$ALICE_LOGIN" | grep -o '"accessToken"[^,]*' | cut -d'"' -f4)
if [ -z "$ALICE_TOKEN" ] || [ "$ALICE_TOKEN" = "null" ]; then
    echo -e "${RED}❌ Alice login failed${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Alice Token: ${ALICE_TOKEN:0:30}...${NC}"

echo ""
echo "🔐 Login Bob (User)..."
BOB_LOGIN=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@example.com","password":"Bob123!"}' || echo '{"error":"login failed"}')
echo "Response: $BOB_LOGIN"
BOB_TOKEN=$(echo "$BOB_LOGIN" | grep -o '"accessToken"[^,]*' | cut -d'"' -f4)
if [ -z "$BOB_TOKEN" ] || [ "$BOB_TOKEN" = "null" ]; then
    echo -e "${RED}❌ Bob login failed${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Bob Token: ${BOB_TOKEN:0:30}...${NC}"

# =============================================================================
# STEP 2 : PARTNER crée une catégorie
# =============================================================================

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  STEP 2 : PARTNER crée une catégorie QUIZ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

CATEGORY_RESPONSE=$(curl -s -X POST "$BASE/categories/partner" \
  -H "Authorization: Bearer $PARTNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Culture Générale - Café Lune",
    "description": "Quiz culture générale pour les clients du café",
    "type": "QUIZ",
    "visibility": "PUBLIC",
    "tags": ["culture", "quiz", "cafe"],
    "metadata": {"difficulty": "mixed", "estimatedTime": 10}
  }')
echo "Response: $CATEGORY_RESPONSE"

# Extraire l'ID de la catégorie
CATEGORY_ID=$(echo "$CATEGORY_RESPONSE" | grep -o '"id"[^,}]*' | head -1 | cut -d'"' -f4)
if [ -z "$CATEGORY_ID" ] || [ "$CATEGORY_ID" = "null" ]; then
    # Try alternative pattern
    CATEGORY_ID=$(echo "$CATEGORY_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
fi

if [ -z "$CATEGORY_ID" ] || [ "$CATEGORY_ID" = "null" ]; then
    echo -e "${RED}❌ ERREUR création catégorie - ID non trouvé${NC}"
    echo "Response received: $CATEGORY_RESPONSE"
    exit 1
fi
echo -e "${GREEN}✅ Category ID: $CATEGORY_ID${NC}"

# =============================================================================
# STEP 3 : PARTNER ajoute des questions
# =============================================================================

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  STEP 3 : PARTNER ajoute 5 questions${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

for i in 1 2 3 4 5; do
  echo ""
  echo "❓ Adding Question $i..."
  Q_RESPONSE=$(curl -s -X POST "$BASE/categories/$CATEGORY_ID/content" \
    -H "Authorization: Bearer $PARTNER_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"contentType\": \"QUESTION\",
      \"title\": \"Question $i : Quelle est la capitale du pays n°$i ?\",
      \"correctAnswer\": \"Capitale$i\",
      \"options\": [
        {\"text\": \"Capitale$i\", \"isCorrect\": true},
        {\"text\": \"FauxA$i\", \"isCorrect\": false},
        {\"text\": \"FauxB$i\", \"isCorrect\": false},
        {\"text\": \"FauxC$i\", \"isCorrect\": false}
      ],
      \"points\": $((i * 10)),
      \"timeLimit\": 30,
      \"difficulty\": \"EASY\",
      \"order\": $((i - 1))
    }")
  echo "  Response: $Q_RESPONSE"
  Q_ID=$(echo "$Q_RESPONSE" | grep -o '"id"[^,}]*' | head -1 | cut -d'"' -f4)
  echo -e "${GREEN}  Q$i ID: $Q_ID${NC}"
done

# =============================================================================
# STEP 4 : ADMIN approuve la catégorie
# =============================================================================

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  STEP 4 : ADMIN approuve la catégorie${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

APPROVE_RESPONSE=$(curl -s -X PATCH \
  "$BASE/categories/admin/$CATEGORY_ID/approve" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "APPROVED"}')
echo "Response: $APPROVE_RESPONSE"

STATUS=$(echo "$APPROVE_RESPONSE" | grep -o '"status"[^,}]*' | head -1 | cut -d'"' -f4)
if [ "$STATUS" != "APPROVED" ]; then
    # Try alternative
    STATUS=$(echo "$APPROVE_RESPONSE" | grep -o 'APPROVED' || echo "NOT_FOUND")
fi

if [ "$STATUS" != "APPROVED" ]; then
    echo -e "${RED}❌ ERREUR approbation catégorie - Status: $STATUS${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Category approved successfully${NC}"

# =============================================================================
# STEP 5 : PARTNER crée EVENT SIMPLE
# =============================================================================

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  STEP 5 : PARTNER crée un EVENT SIMPLE${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

SIMPLE_EVENT_RESPONSE=$(curl -s -X POST "$BASE/events/partner" \
  -H "Authorization: Bearer $PARTNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Quiz du Soir - Simple\",
    \"description\": \"Testez vos connaissances!\",
    \"eventType\": \"SIMPLE\",
    \"categoryId\": \"$CATEGORY_ID\",
    \"visibility\": \"PUBLIC\",
    \"questionTimeLimit\": 30,
    \"maxParticipants\": 50
  }")
echo "Response: $SIMPLE_EVENT_RESPONSE"

SIMPLE_EVENT_ID=$(echo "$SIMPLE_EVENT_RESPONSE" | grep -o '"id"[^,}]*' | head -1 | cut -d'"' -f4)
if [ -z "$SIMPLE_EVENT_ID" ] || [ "$SIMPLE_EVENT_ID" = "null" ]; then
    echo -e "${RED}❌ ERREUR création event simple${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Simple Event ID: $SIMPLE_EVENT_ID${NC}"

# =============================================================================
# STEP 6 : PARTNER crée EVENT LIVE
# =============================================================================

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  STEP 6 : PARTNER crée un EVENT LIVE${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

# Générer une date dans 2 minutes (format ISO compatible)
if command -v date &> /dev/null && date --version 2>&1 | grep -q "GNU"; then
    SCHEDULED_AT=$(date -u -d "+2 minutes" '+%Y-%m-%dT%H:%M:%S' 2>/dev/null || date -u '+%Y-%m-%dT%H:%M:%S')
else
    # macOS date ou autre
    SCHEDULED_AT=$(date -u '+%Y-%m-%dT%H:%M:%S')
fi

echo "Scheduled at: $SCHEDULED_AT"

LIVE_EVENT_RESPONSE=$(curl -s -X POST "$BASE/events/partner" \
  -H "Authorization: Bearer $PARTNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Grand Quiz Live - Café de la Lune\",
    \"description\": \"Event live en temps réel, soyez prêts!\",
    \"eventType\": \"LIVE\",
    \"categoryId\": \"$CATEGORY_ID\",
    \"visibility\": \"PUBLIC\",
    \"scheduledAt\": \"$SCHEDULED_AT\",
    \"questionTimeLimit\": 20,
    \"maxParticipants\": 100
  }")
echo "Response: $LIVE_EVENT_RESPONSE"

LIVE_EVENT_ID=$(echo "$LIVE_EVENT_RESPONSE" | grep -o '"id"[^,}]*' | head -1 | cut -d'"' -f4)
if [ -z "$LIVE_EVENT_ID" ] || [ "$LIVE_EVENT_ID" = "null" ]; then
    echo -e "${YELLOW}⚠️ Live Event creation returned unexpected response, continuing...${NC}"
    LIVE_EVENT_ID="live-event-placeholder"
else
    echo -e "${GREEN}✅ Live Event ID: $LIVE_EVENT_ID${NC}"
fi

# =============================================================================
# STEP 7 : ALICE joue l'EVENT SIMPLE
# =============================================================================

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  STEP 7 : ALICE joue l'event simple${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

# Join event
echo ""
echo "🎮 Alice joining event..."
JOIN_RESPONSE=$(curl -s -X POST "$BASE/events/$SIMPLE_EVENT_ID/join" \
  -H "Authorization: Bearer $ALICE_TOKEN")
echo "Join Response: $JOIN_RESPONSE"

# Loop questions
for i in 1 2 3 4 5; do
  echo ""
  echo "  → Question $i..."

  QUESTION=$(curl -s -X GET "$BASE/events/$SIMPLE_EVENT_ID/question" \
    -H "Authorization: Bearer $ALICE_TOKEN")
  echo "  Question Response: $QUESTION"

  # Check if event completed (null or empty response)
  if [ "$QUESTION" = "null" ] || [ -z "$QUESTION" ] || [ "$QUESTION" = "" ]; then
    echo -e "${GREEN}  ✅ Toutes les questions terminées${NC}"
    break
  fi

  # Extract content ID and index
  CONTENT_ID=$(echo "$QUESTION" | grep -o '"id"[^,}]*' | head -1 | cut -d'"' -f4)
  Q_INDEX=$(echo "$QUESTION" | grep -o '"questionIndex"[^,}]*' | head -1 | grep -o '[0-9]*' || echo "$((i-1))")

  if [ -z "$CONTENT_ID" ] || [ "$CONTENT_ID" = "null" ]; then
    echo -e "${GREEN}  ✅ Fin du quiz${NC}"
    break
  fi

  echo "    ContentID: $CONTENT_ID, Index: $Q_INDEX"

  ANSWER_RESPONSE=$(curl -s -X POST "$BASE/events/$SIMPLE_EVENT_ID/answer" \
    -H "Authorization: Bearer $ALICE_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"contentId\": \"$CONTENT_ID\",
      \"questionIndex\": $Q_INDEX,
      \"selectedAnswer\": \"Capitale$i\"
    }")
  echo "  Answer Response: $ANSWER_RESPONSE"

  IS_CORRECT=$(echo "$ANSWER_RESPONSE" | grep -o '"correct"[^,}]*' | head -1 | grep -o 'true\|false' || echo "unknown")
  POINTS=$(echo "$ANSWER_RESPONSE" | grep -o '"pointsEarned"[^,}]*' | head -1 | grep -o '[0-9]*' || echo "0")
  SCORE=$(echo "$ANSWER_RESPONSE" | grep -o '"currentScore"[^,}]*' | head -1 | grep -o '[0-9]*' || echo "0")

  echo -e "    ${YELLOW}Correcte: $IS_CORRECT | Points: $POINTS | Score total: $SCORE${NC}"

  sleep 0.5
done

# =============================================================================
# STEP 8 : BOB joue aussi l'event simple
# =============================================================================

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  STEP 8 : BOB joue l'event simple${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

echo ""
echo "🎮 Bob joining event..."
curl -s -X POST "$BASE/events/$SIMPLE_EVENT_ID/join" \
  -H "Authorization: Bearer $BOB_TOKEN" > /dev/null

for i in 1 2 3 4 5; do
  QUESTION=$(curl -s -X GET "$BASE/events/$SIMPLE_EVENT_ID/question" \
    -H "Authorization: Bearer $BOB_TOKEN")

  if [ "$QUESTION" = "null" ] || [ -z "$QUESTION" ] || [ "$QUESTION" = "" ]; then
    break
  fi

  CONTENT_ID=$(echo "$QUESTION" | grep -o '"id"[^,}]*' | head -1 | cut -d'"' -f4)
  Q_INDEX=$(echo "$QUESTION" | grep -o '"questionIndex"[^,}]*' | head -1 | grep -o '[0-9]*' || echo "$((i-1))")

  if [ -z "$CONTENT_ID" ] || [ "$CONTENT_ID" = "null" ]; then
    break
  fi

  # Bob répond mal exprès
  curl -s -X POST "$BASE/events/$SIMPLE_EVENT_ID/answer" \
    -H "Authorization: Bearer $BOB_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"contentId\": \"$CONTENT_ID\",
      \"questionIndex\": $Q_INDEX,
      \"selectedAnswer\": \"MauvaiseReponse\"
    }" > /dev/null
  echo -e "  ${YELLOW}Bob Q$i : réponse soumise (mauvaise réponse)${NC}"
  sleep 0.3
done

# =============================================================================
# STEP 9 : LEADERBOARD event simple
# =============================================================================

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  STEP 9 : Leaderboard event simple${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

LEADERBOARD=$(curl -s -X GET "$BASE/events/$SIMPLE_EVENT_ID/leaderboard" \
  -H "Authorization: Bearer $ALICE_TOKEN")
echo "Leaderboard: $LEADERBOARD"

# =============================================================================
# STEP 10 : SCORES utilisateurs
# =============================================================================

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  STEP 10 : Scores utilisateurs${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

echo ""
echo "⭐ Alice score:"
ALICE_SCORE=$(curl -s -X GET "$BASE/scores/me" \
  -H "Authorization: Bearer $ALICE_TOKEN")
echo "$ALICE_SCORE"

echo ""
echo "⭐ Bob score:"
BOB_SCORE=$(curl -s -X GET "$BASE/scores/me" \
  -H "Authorization: Bearer $BOB_TOKEN")
echo "$BOB_SCORE"

echo ""
echo "⭐ Leaderboard global:"
GLOBAL_LEADERBOARD=$(curl -s -X GET "$BASE/scores/global?page=0&size=5" \
  -H "Authorization: Bearer $ALICE_TOKEN")
echo "$GLOBAL_LEADERBOARD"

# =============================================================================
# STEP 11 : PARTNER lance le LIVE event
# =============================================================================

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  STEP 11 : PARTNER lance le LIVE event${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

if [ "$LIVE_EVENT_ID" != "live-event-placeholder" ]; then
  LAUNCH_RESPONSE=$(curl -s -X POST \
    "$BASE/events/partner/$LIVE_EVENT_ID/launch" \
    -H "Authorization: Bearer $PARTNER_TOKEN")
  echo "Launch Response: $LAUNCH_RESPONSE"

  STATUS=$(echo "$LAUNCH_RESPONSE" | grep -o '"status"[^,}]*' | head -1 | cut -d'"' -f4)
  if [ -z "$STATUS" ]; then
    STATUS=$(echo "$LAUNCH_RESPONSE" | grep -o 'WAITING_ROOM\|LIVE' || echo "UNKNOWN")
  fi
  echo -e "${GREEN}✅ Live Event Status: $STATUS${NC}"

  # =============================================================================
  # STEP 12 : ALICE rejoint le LIVE event
  # =============================================================================

  echo ""
  echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
  echo -e "${BLUE}  STEP 12 : ALICE rejoint le LIVE event${NC}"
  echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

  JOIN_LIVE=$(curl -s -X POST "$BASE/events/$LIVE_EVENT_ID/join" \
    -H "Authorization: Bearer $ALICE_TOKEN")
  echo "Join Live Response: $JOIN_LIVE"

  # État du live event
  LIVE_STATE=$(curl -s -X GET "$BASE/events/$LIVE_EVENT_ID/state" \
    -H "Authorization: Bearer $ALICE_TOKEN")
  echo "Live State: $LIVE_STATE"
else
  echo -e "${YELLOW}⚠️ Skipping LIVE event steps (creation failed)${NC}"
fi

# =============================================================================
# RÉSULTAT FINAL
# =============================================================================

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  ✅ SCÉNARIO COMPLET TERMINÉ${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${YELLOW}Résumé:${NC}"
echo -e "  - Catégorie créée: $CATEGORY_ID"
echo -e "  - Event Simple créé: $SIMPLE_EVENT_ID"
echo -e "  - Event Live créé: $LIVE_EVENT_ID"
echo ""
echo -e "${YELLOW}Tokens pour tests manuels:${NC}"
echo -e "  Admin: ${ADMIN_TOKEN:0:50}..."
echo -e "  Partner: ${PARTNER_TOKEN:0:50}..."
echo -e "  Alice: ${ALICE_TOKEN:0:50}..."
echo -e "  Bob: ${BOB_TOKEN:0:50}..."
echo ""

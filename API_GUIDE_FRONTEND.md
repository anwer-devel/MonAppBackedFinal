# API Guide - Event & Friendship Management (Frontend)

## Base URL
```
http://localhost:8080/api
```

## Authentication
All endpoints require Bearer token in header:
```
Authorization: Bearer {jwt_token}
```

---

## EVENT MANAGEMENT

### 1. Create Event (Admin Only)
```
POST /events/admin
```

**Payload:**
```json
{
  "title": "Quiz Géographie",
  "description": "Testez vos connaissances géographiques",
  "coverImage": "https://example.com/cover.jpg",
  "eventType": "SIMPLE",
  "categoryId": "uuid-de-la-categorie",
  "visibility": "PUBLIC",
  "scheduledAt": "2026-04-15T18:00:00",
  "maxParticipants": 100,
  "questionTimeLimit": 30
}
```

**Response 200:**
```json
{
  "id": "uuid-event",
  "title": "Quiz Géographie",
  "description": "Testez vos connaissances géographiques",
  "coverImage": "https://example.com/cover.jpg",
  "eventType": "SIMPLE",
  "status": "DRAFT",
  "categoryId": "uuid-categorie",
  "partnerId": null,
  "createdBy": "ADMIN",
  "visibility": "PUBLIC",
  "scheduledAt": "2026-04-15T18:00:00",
  "maxParticipants": 100,
  "currentParticipants": 0,
  "totalQuestions": 10,
  "isActive": true
}
```

---

### 2. Get All Events (Admin Only)
```
GET /events/admin/all?page=0&size=10&status=SCHEDULED
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)
- `status`: Filter by status (optional) - DRAFT, SCHEDULED, LIVE, FINISHED, CANCELLED

**Response 200:**
```json
{
  "content": [
    {
      "id": "uuid-event",
      "title": "Quiz Géographie",
      "eventType": "SIMPLE",
      "status": "SCHEDULED",
      "categoryId": "uuid-categorie",
      "scheduledAt": "2026-04-15T18:00:00",
      "currentParticipants": 45,
      "maxParticipants": 100
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

### 3. Create Partner Event (Partner Only)
```
POST /events/partner
```

**Payload:**
```json
{
  "title": "Quiz Partenaire",
  "description": "Event sponsorisé",
  "coverImage": "https://example.com/cover.jpg",
  "eventType": "LIVE",
  "categoryId": "uuid-categorie",
  "visibility": "PUBLIC",
  "scheduledAt": "2026-04-20T20:00:00",
  "maxParticipants": 500,
  "questionTimeLimit": 20
}
```

**Response 200:** Same as Create Event

---

### 4. Get My Partner Events
```
GET /events/partner/mine?page=0&size=10&status=LIVE
```

**Response 200:** Page of EventDto

---

### 5. Launch Live Event (Partner Only)
```
POST /events/partner/{eventId}/launch
```

**Response 200:** EventDto with status=LIVE

---

### 6. Update My Event (Partner Only)
```
PATCH /events/partner/{eventId}
```

**Payload:**
```json
{
  "title": "Nouveau titre",
  "description": "Nouvelle description",
  "scheduledAt": "2026-04-25T19:00:00"
}
```

**Response 200:** Updated EventDto

---

### 7. Cancel Event (Admin/Partner)
```
DELETE /events/admin/{eventId}     (Admin)
DELETE /events/partner/{eventId}     (Partner - own events only)
```

**Response 204:** No Content

---

### 8. Get Events by Partner (Public)
```
GET /events/partner/{partnerId}?page=0&size=10
```

**Response 200:** Page of EventDto

---

### 9. Join Event (User)
```
POST /events/{eventId}/join
```

**Response 200:**
```json
{
  "success": true,
  "eventId": "uuid-event",
  "participantId": "uuid-participant",
  "status": "WAITING",
  "message": "Successfully joined event",
  "currentParticipants": 46,
  "maxParticipants": 100
}
```

---

### 10. Leave Event (User)
```
POST /events/{eventId}/leave
```

**Response 204:** No Content

---

### 11. Get Next Question (Simple Events)
```
GET /events/{eventId}/question
```

**Response 200:**
```json
{
  "id": "uuid-question",
  "categoryId": "uuid-categorie",
  "contentType": "QUIZ",
  "title": "Capitale de la France ?",
  "description": "Question simple",
  "options": ["Paris", "Londres", "Berlin", "Madrid"],
  "points": 10,
  "timeLimit": 30,
  "difficulty": "EASY",
  "order": 1,
  "questionIndex": 0,
  "totalQuestions": 10
}
```

---

### 12. Submit Answer (Simple Events)
```
POST /events/{eventId}/answer
```

**Payload:**
```json
{
  "contentId": "uuid-question",
  "questionIndex": 0,
  "selectedAnswer": "Paris"
}
```

**Response 200:**
```json
{
  "isCorrect": true,
  "pointsEarned": 10,
  "speedBonus": 0,
  "correctAnswer": "Paris",
  "explanation": "Paris est la capitale de la France",
  "currentScore": 50,
  "nextQuestion": {
    "id": "uuid-next-question",
    "title": "Question suivante...",
    "options": ["A", "B", "C", "D"]
  }
}
```

---

### 13. Get Event Leaderboard
```
GET /events/{eventId}/leaderboard
```

**Response 200:**
```json
{
  "eventId": "uuid-event",
  "entries": [
    {
      "rank": 1,
      "userId": "uuid-user",
      "username": "john_doe",
      "avatar": "https://example.com/avatar.jpg",
      "score": 150,
      "correctAnswers": 15,
      "isFriend": true,
      "isCurrentUser": false
    },
    {
      "rank": 2,
      "userId": "uuid-current-user",
      "username": "me",
      "avatar": "https://example.com/my-avatar.jpg",
      "score": 120,
      "correctAnswers": 12,
      "isFriend": false,
      "isCurrentUser": true
    }
  ]
}
```

---

### 14. Get Live Event State (WebSocket fallback)
```
GET /events/{eventId}/state
```

**Response 200:**
```json
{
  "eventId": "uuid-event",
  "status": "LIVE",
  "currentQuestionIndex": 3,
  "totalQuestions": 20,
  "currentQuestion": {
    "id": "uuid-question",
    "title": "Question en cours",
    "options": ["A", "B", "C", "D"]
  },
  "timeRemaining": 15,
  "participantCount": 245,
  "leaderboard": [
    {
      "rank": 1,
      "userId": "uuid-user",
      "username": "leader",
      "score": 80
    }
  ]
}
```

---

## SCORE & LEADERBOARD

### 15. Get My Score Summary
```
GET /scores/me
```

**Response 200:**
```json
{
  "userId": "uuid-user",
  "username": "john_doe",
  "totalScore": 1250,
  "totalEvents": 15,
  "totalCorrectAnswers": 120,
  "totalWrongAnswers": 30,
  "rank": 5
}
```

---

### 16. Get Partner Leaderboard
```
GET /scores/partner/{partnerId}?page=0&size=20
```

**Response 200:** Page of UserScoreDto

---

### 17. Get Global Leaderboard
```
GET /scores/global?page=0&size=20
```

**Response 200:** Page of UserScoreDto

---

## FRIENDSHIP MANAGEMENT

### 18. Send Friend Request
```
POST /friends/request
```

**Payload:**
```json
{
  "addresseeId": "uuid-destinataire",
  "eventId": "uuid-event-optionnel"
}
```

**Response 200:**
```json
{
  "id": "uuid-friendship",
  "requesterId": "uuid-demandeur",
  "addresseeId": "uuid-destinataire",
  "status": "PENDING",
  "requesterUsername": "john_doe",
  "addresseeUsername": "jane_doe",
  "createdAt": "2026-04-08T10:00:00"
}
```

---

### 19. Respond to Friend Request
```
PATCH /friends/{friendshipId}/respond?accept=true
```

**Query Parameters:**
- `accept`: true (accept) / false (decline)

**Response 200:** Updated FriendshipDto

---

### 20. Get My Friends
```
GET /friends
```

**Response 200:**
```json
[
  {
    "id": "uuid-friendship",
    "requesterId": "uuid-user1",
    "addresseeId": "uuid-user2",
    "status": "ACCEPTED",
    "requesterUsername": "john_doe",
    "addresseeUsername": "jane_doe",
    "createdAt": "2026-04-01T10:00:00"
  }
]
```

---

### 21. Get Pending Requests
```
GET /friends/requests
```

**Response 200:** List of FriendshipDto with status=PENDING

---

### 22. Block User
```
DELETE /friends/{userId}
```

**Response 204:** No Content

---

## NOTIFICATIONS

### 23. Get Unread Notifications
```
GET /notifications
```

**Response 200:**
```json
[
  {
    "id": "uuid-notif",
    "type": "FRIEND_REQUEST",
    "title": "Nouvelle demande d'ami",
    "body": "Vous avez reçu une demande d'ami",
    "data": {
      "userId": "uuid-user",
      "eventId": "uuid-event"
    },
    "isRead": false,
    "createdAt": "2026-04-08T10:00:00"
  }
]
```

---

### 24. Mark Notification as Read
```
PATCH /notifications/{notificationId}/read
```

**Response 204:** No Content

---

### 25. Mark All Notifications as Read
```
PATCH /notifications/read-all
```

**Response 204:** No Content

---

## WEBSOCKET (STOMP) - Live Events

### Connection
```
ws://localhost:8080/ws
```

### Subscribe to Topics:
```javascript
// Event state updates
/topic/event/{eventId}/state

// New question broadcast
/topic/event/{eventId}/question

// Leaderboard updates
/topic/event/{eventId}/leaderboard

// Event finished
/topic/event/{eventId}/finished

// Personal notifications
/user/queue/notifications

// Friend requests
/user/queue/friendRequests

// Answer result
/user/queue/answer-result
```

### Send Messages:
```javascript
// Join live event
/app/event/{eventId}/join-live

// Submit live answer
/app/event/{eventId}/submit-answer
Payload: {
  "contentId": "uuid-question",
  "questionIndex": 0,
  "selectedAnswer": "Paris"
}

// Leave live event
/app/event/{eventId}/leave-live
```

---

## CATEGORY RELATIONSHIPS

### Event Creation Flow with Category:
1. Get approved categories: `GET /categories?status=APPROVED`
2. Get category contents: `GET /categories/{categoryId}/contents`
3. Create event with `categoryId`

### Category Validation Rules:
- Category must be `APPROVED` and `ACTIVE`
- For `PUBLIC` events: Category must be `PUBLIC`
- Total questions = count of active contents in category
- Questions ordered by `order` field

---

## ENUMS

### EventType
- `SIMPLE` - Self-paced quiz, persistent progress
- `LIVE` - Real-time event with all participants

### EventStatus
- `DRAFT` - Being prepared
- `SCHEDULED` - Published, waiting to start
- `WAITING_ROOM` - Live event about to start
- `LIVE` - Currently running
- `FINISHED` - Completed
- `CANCELLED` - Cancelled

### ParticipantStatus
- `WAITING` - Registered but not started
- `ACTIVE` - Currently participating
- `COMPLETED` - Finished all questions
- `ABANDONED` - Left before completion

### NotificationType
- `EVENT_STARTED`
- `EVENT_REMINDER`
- `EVENT_CANCELLED`
- `FRIEND_REQUEST`
- `FRIEND_ACCEPTED`
- `SCORE_UPDATED`

---

## ERROR RESPONSES

### 400 Bad Request
```json
{
  "timestamp": "2026-04-08T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Event is already full",
  "path": "/api/events/{id}/join"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2026-04-08T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing token"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2026-04-08T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Only event creator can cancel"
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-04-08T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Event not found"
}
```

---

## FRONTEND IMPLEMENTATION NOTES

### Event Flow (Simple):
1. User browses events: `GET /events/partner/{id}`
2. User joins: `POST /events/{id}/join`
3. Get first question: `GET /events/{id}/question`
4. Submit answer: `POST /events/{id}/answer`
5. Display result, get next question
6. After last question, show leaderboard

### Event Flow (Live):
1. User joins before start: `POST /events/{id}/join`
2. Connect WebSocket to `/ws`
3. Subscribe to `/topic/event/{id}/state`
4. Wait for event to go LIVE
5. Receive questions via `/topic/event/{id}/question`
6. Submit answers via `/app/event/{id}/submit-answer`
7. Receive result via `/user/queue/answer-result`
8. Watch leaderboard updates via `/topic/event/{id}/leaderboard`

### Friend Feature in Leaderboard:
- Use `isFriend` flag to highlight friends
- Use `isCurrentUser` to highlight current user row
- Show friend request button for non-friends

### Score Display:
- Show `speedBonus` for quick answers (< 5 seconds)
- Display `rank` changes in real-time

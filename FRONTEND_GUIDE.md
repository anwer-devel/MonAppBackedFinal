# 📘 GUIDE FRONTEND - API Event Management (Complet & Corrigé)

## 🎯 Vue d'ensemble des rôles et flux

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         ARCHITECTURE DES FLUX                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────┐   │
│  │  ADMIN   │────▶│   Gestion    │────▶│  Approuver   │────▶│  Events  │   │
│  │          │     │   Partners   │     │  Categories  │     │  Globaux │   │
│  └──────────┘     └──────────────┘     └──────────────┘     └──────────┘   │
│                                                                             │
│  ┌──────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────┐   │
│  │ PARTNER  │────▶│   Créer      │────▶│   Ajouter    │────▶│  Créer   │   │
│  │  OWNER   │     │  Category    │     │  Questions   │     │  Events  │   │
│  └──────────┘     └──────────────┘     └──────────────┘     └──────────┘   │
│                                    │                           │            │
│                                    │      ┌────────────────────┘            │
│                                    ▼      ▼                                 │
│                           ┌──────────────┐     ┌──────────────┐           │
│                           │  En attente  │────▶│   APPROVED   │◀────────┤
│                           │  APPROVAL    │     │   par Admin    │  Admin   │
│                           └──────────────┘     └──────────────┘           │
│                                                                             │
│  ┌──────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────┐   │
│  │   USER   │────▶│   Browse     │────▶│   Rejoindre │────▶│   Jouer  │   │
│  │          │     │   Events     │     │   Event     │     │  Quiz    │   │
│  └──────────┘     └──────────────┘     └──────────────┘     └──────────┘   │
│                                    │                           │          │
│                                    └──────────┐    ┌────────────┘          │
│                                               ▼    ▼                       │
│                                      ┌──────────────────────┐              │
│                                      │   Leaderboard        │              │
│                                      │   Calcul Scores      │              │
│                                      └──────────────────────┘              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Structure des Services Angular Recommandée

```
src/app/
├── core/
│   ├── models/
│   │   ├── auth.model.ts
│   │   ├── category.model.ts
│   │   └── event.model.ts
│   ├── services/
│   │   ├── auth.service.ts
│   │   ├── category.service.ts
│   │   ├── event.service.ts
│   │   └── score.service.ts
│   └── interceptors/
│       └── jwt.interceptor.ts
├── features/
│   ├── admin/
│   │   └── category-approval/
│   ├── partner/
│   │   ├── category-management/
│   │   └── event-management/
│   └── user/
│       ├── event-browser/
│       └── gameplay/
└── shared/
    └── components/
```

---

## 🔧 1. CONFIGURATION AUTH & INTERCEPTOR

### auth.model.ts
```typescript
export interface User {
  id: string;
  email: string;
  username: string;
  role: 'ADMIN' | 'PARTNER_OWNER' | 'USER';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  data: {
    accessToken: string;
    refreshToken: string;
    user: User;
  };
}

// JWT Payload décodé
export interface JwtPayload {
  sub: string;        // userId
  userId: string;
  role: 'ROLE_ADMIN' | 'ROLE_PARTNER_OWNER' | 'ROLE_USER';
  email: string;
  username: string;
  exp: number;
}
```

### jwt.interceptor.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler } from '@angular/common/http';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const token = localStorage.getItem('accessToken');
    
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    
    return next.handle(req);
  }
}
```

### auth.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, JwtPayload } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api';
  
  constructor(private http: HttpClient) {}
  
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          if (response.data?.accessToken) {
            localStorage.setItem('accessToken', response.data.accessToken);
            localStorage.setItem('refreshToken', response.data.refreshToken);
            localStorage.setItem('user', JSON.stringify(response.data.user));
          }
        })
      );
  }
  
  logout(): void {
    localStorage.clear();
  }
  
  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }
  
  getCurrentUser(): User | null {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }
  
  decodeToken(token: string): JwtPayload {
    const base64 = token.split('.')[1];
    return JSON.parse(atob(base64));
  }
  
  isPartner(): boolean {
    const token = this.getToken();
    if (!token) return false;
    const payload = this.decodeToken(token);
    return payload.role === 'ROLE_PARTNER_OWNER';
  }
  
  isAdmin(): boolean {
    const token = this.getToken();
    if (!token) return false;
    const payload = this.decodeToken(token);
    return payload.role === 'ROLE_ADMIN';
  }
}
```

---

## 📂 2. GESTION DES CATÉGORIES

### category.model.ts
```typescript
export type CategoryType = 'QUIZ' | 'SURVEY' | 'CHALLENGE';
export type CategoryStatus = 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED';
export type CategoryVisibility = 'PUBLIC' | 'PRIVATE';

export interface Category {
  id: string;
  name: string;
  description: string;
  type: CategoryType;
  status: CategoryStatus;
  visibility: CategoryVisibility;
  partnerId: string;
  partnerName: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
}

export interface CreateCategoryRequest {
  name: string;
  description: string;
  type: CategoryType;
  visibility: CategoryVisibility;
  tags?: string[];
}

export interface CreateContentRequest {
  contentType: 'QUESTION' | 'CONTENT';
  title: string;
  description?: string;
  correctAnswer: string;
  options: {
    text: string;
    isCorrect: boolean;
  }[];
  points?: number;
  timeLimit?: number;
  difficulty?: 'EASY' | 'MEDIUM' | 'HARD';
  order?: number;
}

export interface CategoryContent {
  id: string;
  categoryId: string;
  contentType: string;
  title: string;
  correctAnswer: string;
  options: any[];
  points: number;
  timeLimit: number;
  order: number;
}
```

### category.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  Category, 
  CreateCategoryRequest, 
  CreateContentRequest,
  CategoryContent 
} from '../models/category.model';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private apiUrl = 'http://localhost:8080/api';
  
  constructor(private http: HttpClient) {}
  
  // ==================== PARTNER ENDPOINTS ====================
  
  /**
   * Créer une catégorie (PARTNER uniquement)
   * POST /api/categories/partner
   * Retourne: Category (direct, pas wrappé dans data)
   */
  createCategory(request: CreateCategoryRequest): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/categories/partner`, request);
  }
  
  /**
   * Ajouter du contenu à une catégorie
   * POST /api/categories/{categoryId}/content
   */
  addContent(categoryId: string, request: CreateContentRequest): Observable<CategoryContent> {
    return this.http.post<CategoryContent>(
      `${this.apiUrl}/categories/${categoryId}/content`, 
      request
    );
  }
  
  /**
   * Récupérer les catégories du partner connecté
   * GET /api/categories/partner/my-categories
   */
  getMyCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/categories/partner/my-categories`);
  }
  
  /**
   * Récupérer le contenu d'une catégorie
   * GET /api/categories/{categoryId}/content
   */
  getCategoryContent(categoryId: string): Observable<CategoryContent[]> {
    return this.http.get<CategoryContent[]>(
      `${this.apiUrl}/categories/${categoryId}/content`
    );
  }
  
  // ==================== ADMIN ENDPOINTS ====================
  
  /**
   * Approuver/Rejeter une catégorie (ADMIN uniquement)
   * PATCH /api/categories/admin/{categoryId}/approve
   */
  approveCategory(categoryId: string, status: 'APPROVED' | 'REJECTED'): Observable<any> {
    return this.http.patch(
      `${this.apiUrl}/categories/admin/${categoryId}/approve`,
      { status }
    );
  }
  
  /**
   * Récupérer les catégories en attente (ADMIN)
   * GET /api/categories/admin/pending
   */
  getPendingCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/categories/admin/pending`);
  }
  
  // ==================== PUBLIC ENDPOINTS ====================
  
  /**
   * Récupérer toutes les catégories approuvées
   * GET /api/categories/public
   */
  getPublicCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/categories/public`);
  }
  
  /**
   * Récupérer une catégorie par ID
   * GET /api/categories/{id}
   */
  getCategoryById(id: string): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/categories/${id}`);
  }
}
```

---

## 🎮 3. GESTION DES EVENTS

### event.model.ts
```typescript
export type EventType = 'SIMPLE' | 'LIVE';
export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'WAITING_ROOM' | 'IN_PROGRESS' | 'FINISHED' | 'CANCELLED';
export type EventVisibility = 'PUBLIC' | 'PRIVATE';

export interface Event {
  id: string;
  title: string;
  description: string;
  eventType: EventType;
  status: EventStatus;
  visibility: EventVisibility;
  categoryId: string;
  categoryName: string;
  partnerId: string;
  partnerName: string;
  scheduledAt?: string;
  maxParticipants: number;
  currentParticipants: number;
  totalQuestions: number;
  questionTimeLimit?: number;
  isActive: boolean;
  createdAt: string;
}

export interface CreateEventRequest {
  title: string;
  description: string;
  eventType: EventType;
  categoryId: string;
  visibility: EventVisibility;
  scheduledAt?: string;        // Requis pour LIVE (format: ISO 8601)
  questionTimeLimit?: number;
  maxParticipants?: number;
}

export interface JoinEventResponse {
  participantId: string;
  eventId: string;
  message: string;
}

export interface CategoryContentResponse {
  id: string;
  title: string;
  description?: string;
  options: {
    text: string;
    isCorrect?: boolean;
  }[];
  questionIndex: number;
  totalQuestions: number;
  timeLimit: number;
  points: number;
}

export interface SubmitAnswerRequest {
  contentId: string;
  questionIndex: number;
  selectedAnswer: string;
}

export interface AnswerResult {
  correct: boolean;
  correctAnswer: string;
  pointsEarned: number;
  currentScore: number;
  questionIndex: number;
  totalQuestions: number;
  isCompleted: boolean;
}

export interface LeaderboardEntry {
  rank: number;
  userId: string;
  username: string;
  score: number;
  correctAnswers: number;
  totalTime: number;
  isFriend: boolean;
}

export interface EventLeaderboard {
  eventId: string;
  entries: LeaderboardEntry[];
}

export interface LiveEventState {
  eventId: string;
  status: EventStatus;
  currentQuestionIndex: number;
  totalQuestions: number;
  timeRemaining: number;
  isParticipant: boolean;
}
```

### event.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Event,
  CreateEventRequest,
  JoinEventResponse,
  CategoryContentResponse,
  SubmitAnswerRequest,
  AnswerResult,
  EventLeaderboard,
  LiveEventState
} from '../models/event.model';

@Injectable({ providedIn: 'root' })
export class EventService {
  private apiUrl = 'http://localhost:8080/api';
  
  constructor(private http: HttpClient) {}
  
  // ==================== PARTNER ENDPOINTS ====================
  
  /**
   * Créer un event (SIMPLE ou LIVE)
   * POST /api/events/partner
   * IMPORTANT: La catégorie doit être APPROVED
   */
  createEvent(request: CreateEventRequest): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl}/events/partner`, request);
  }
  
  /**
   * Lancer un event LIVE (démarrer le quiz en direct)
   * POST /api/events/partner/{eventId}/launch
   */
  launchLiveEvent(eventId: string): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl}/events/partner/${eventId}/launch`, {});
  }
  
  /**
   * Annuler son propre event
   * DELETE /api/events/partner/{eventId}
   */
  cancelEvent(eventId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/events/partner/${eventId}`);
  }
  
  /**
   * Récupérer les events du partner
   * GET /api/events/partner/{partnerId}
   */
  getPartnerEvents(partnerId: string, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get(`${this.apiUrl}/events/partner/${partnerId}`, { params });
  }
  
  // ==================== USER ENDPOINTS (USER + PARTNER_OWNER) ====================
  
  /**
   * Rejoindre un event
   * POST /api/events/{eventId}/join
   * Roles: USER, PARTNER_OWNER
   */
  joinEvent(eventId: string): Observable<JoinEventResponse> {
    return this.http.post<JoinEventResponse>(
      `${this.apiUrl}/events/${eventId}/join`, 
      {}
    );
  }
  
  /**
   * Quitter un event
   * POST /api/events/{eventId}/leave
   */
  leaveEvent(eventId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/events/${eventId}/leave`, {});
  }
  
  /**
   * Récupérer la question suivante (events SIMPLE)
   * GET /api/events/{eventId}/question
   * Retourne 204 No Content si plus de questions
   */
  getNextQuestion(eventId: string): Observable<CategoryContentResponse | null> {
    return this.http.get<CategoryContentResponse>(
      `${this.apiUrl}/events/${eventId}/question`
    );
  }
  
  /**
   * Soumettre une réponse
   * POST /api/events/{eventId}/answer
   */
  submitAnswer(eventId: string, request: SubmitAnswerRequest): Observable<AnswerResult> {
    return this.http.post<AnswerResult>(
      `${this.apiUrl}/events/${eventId}/answer`,
      request
    );
  }
  
  /**
   * Récupérer le leaderboard d'un event
   * GET /api/events/{eventId}/leaderboard
   */
  getEventLeaderboard(eventId: string): Observable<EventLeaderboard> {
    return this.http.get<EventLeaderboard>(
      `${this.apiUrl}/events/${eventId}/leaderboard`
    );
  }
  
  /**
   * Récupérer l'état d'un event LIVE (pour reconnexion)
   * GET /api/events/{eventId}/state
   */
  getLiveEventState(eventId: string): Observable<LiveEventState> {
    return this.http.get<LiveEventState>(
      `${this.apiUrl}/events/${eventId}/state`
    );
  }
  
  // ==================== PUBLIC/ADMIN ENDPOINTS ====================
  
  /**
   * Récupérer tous les events (avec filtres)
   * GET /api/events
   */
  getEvents(
    status?: EventStatus, 
    type?: EventType, 
    page: number = 0, 
    size: number = 20
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (status) params = params.set('status', status);
    if (type) params = params.set('type', type);
    
    return this.http.get(`${this.apiUrl}/events`, { params });
  }
  
  /**
   * Récupérer un event par ID
   * GET /api/events/{id}
   */
  getEventById(id: string): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/events/${id}`);
  }
  
  /**
   * Récupérer les trending events
   * GET /api/events/trending
   */
  getTrendingEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.apiUrl}/events/trending`);
  }
}
```

---

## 🏆 4. GESTION DES SCORES

### score.model.ts
```typescript
export interface UserScore {
  userId: string;
  username: string;
  totalScore: number;
  totalEvents: number;
  correctAnswers: number;
  totalAnswers: number;
  globalRank: number;
  partnerRank?: number;
}

export interface GlobalLeaderboardEntry {
  rank: number;
  userId: string;
  username: string;
  avatar?: string;
  totalScore: number;
  totalEvents: number;
  correctAnswers: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```

### score.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserScore, GlobalLeaderboardEntry, PageResponse } from '../models/score.model';

@Injectable({ providedIn: 'root' })
export class ScoreService {
  private apiUrl = 'http://localhost:8080/api';
  
  constructor(private http: HttpClient) {}
  
  /**
   * Récupérer mon score
   * GET /api/scores/me
   */
  getMyScore(): Observable<UserScore> {
    return this.http.get<UserScore>(`${this.apiUrl}/scores/me`);
  }
  
  /**
   * Récupérer le leaderboard global
   * GET /api/scores/global
   */
  getGlobalLeaderboard(page: number = 0, size: number = 20): Observable<PageResponse<GlobalLeaderboardEntry>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PageResponse<GlobalLeaderboardEntry>>(
      `${this.apiUrl}/scores/global`,
      { params }
    );
  }
  
  /**
   * Récupérer le leaderboard d'un partner
   * GET /api/scores/partner/{partnerId}
   */
  getPartnerLeaderboard(partnerId: string, page: number = 0, size: number = 20): Observable<PageResponse<GlobalLeaderboardEntry>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PageResponse<GlobalLeaderboardEntry>>(
      `${this.apiUrl}/scores/partner/${partnerId}`,
      { params }
    );
  }
}
```

---

## 🔄 5. FLUX COMPLÈTS - EXEMPLES D'UTILISATION

### 5.1 FLUX PARTNER: Créer Category → Questions → Event

```typescript
// partner-dashboard.component.ts
import { Component } from '@angular/core';
import { CategoryService } from '../../core/services/category.service';
import { EventService } from '../../core/services/event.service';

@Component({
  selector: 'app-partner-dashboard',
  template: `
    <button (click)="createFullScenario()">Créer Scenario Complet</button>
  `
})
export class PartnerDashboardComponent {
  constructor(
    private categoryService: CategoryService,
    private eventService: EventService
  ) {}
  
  async createFullScenario() {
    try {
      // Étape 1: Créer la catégorie
      const category = await this.categoryService.createCategory({
        name: "Mon Quiz Démo",
        description: "Description du quiz",
        type: "QUIZ",
        visibility: "PUBLIC",
        tags: ["demo", "test"]
      }).toPromise();
      
      console.log("Catégorie créée:", category.id);
      
      // Étape 2: Ajouter 3 questions
      const questions = [
        {
          contentType: "QUESTION" as const,
          title: "Q1: Quelle est la capitale de la France?",
          correctAnswer: "Paris",
          options: [
            { text: "Paris", isCorrect: true },
            { text: "Londres", isCorrect: false },
            { text: "Berlin", isCorrect: false }
          ],
          points: 10,
          timeLimit: 30,
          difficulty: "EASY" as const,
          order: 0
        },
        {
          contentType: "QUESTION" as const,
          title: "Q2: 2 + 2 = ?",
          correctAnswer: "4",
          options: [
            { text: "3", isCorrect: false },
            { text: "4", isCorrect: true },
            { text: "5", isCorrect: false }
          ],
          points: 10,
          timeLimit: 20,
          order: 1
        },
        {
          contentType: "QUESTION" as const,
          title: "Q3: Quelle est la couleur du ciel?",
          correctAnswer: "Bleu",
          options: [
            { text: "Rouge", isCorrect: false },
            { text: "Vert", isCorrect: false },
            { text: "Bleu", isCorrect: true }
          ],
          points: 10,
          timeLimit: 15,
          order: 2
        }
      ];
      
      for (const q of questions) {
        await this.categoryService.addContent(category.id, q).toPromise();
      }
      
      console.log("3 questions ajoutées");
      
      // Étape 3: Attendre l'approbation Admin (manuel ou polling)
      // L'admin doit appeler: PATCH /api/categories/admin/{id}/approve
      
      alert("Catégorie créée! En attente d'approbation Admin.");
      
    } catch (error) {
      console.error("Erreur:", error);
    }
  }
  
  // Appeler APRÈS approbation admin
  async createEventAfterApproval(categoryId: string) {
    // Event SIMPLE
    const simpleEvent = await this.eventService.createEvent({
      title: "Quiz Simple",
      description: "Un quiz simple pour démo",
      eventType: "SIMPLE",
      categoryId: categoryId,
      visibility: "PUBLIC",
      questionTimeLimit: 30,
      maxParticipants: 50
    }).toPromise();
    
    console.log("Event simple créé:", simpleEvent.id);
    
    // Event LIVE (planifié dans 10 minutes)
    const scheduledTime = new Date();
    scheduledTime.setMinutes(scheduledTime.getMinutes() + 10);
    
    const liveEvent = await this.eventService.createEvent({
      title: "Quiz Live",
      description: "Un quiz en direct!",
      eventType: "LIVE",
      categoryId: categoryId,
      visibility: "PUBLIC",
      scheduledAt: scheduledTime.toISOString(),
      questionTimeLimit: 20,
      maxParticipants: 100
    }).toPromise();
    
    console.log("Event live créé:", liveEvent.id);
    
    // Lancer le live event
    const launched = await this.eventService.launchLiveEvent(liveEvent.id).toPromise();
    console.log("Live lancé:", launched.status); // WAITING_ROOM
  }
}
```

---

### 5.2 FLUX USER: Browse → Join → Play

```typescript
// gameplay.component.ts
import { Component, OnInit } from '@angular/core';
import { EventService } from '../../core/services/event.service';
import { ScoreService } from '../../core/services/score.service';

@Component({
  selector: 'app-gameplay',
  template: `
    <div *ngIf="currentQuestion">
      <h3>Question {{ currentQuestion.questionIndex + 1 }} / {{ currentQuestion.totalQuestions }}</h3>
      <p>{{ currentQuestion.title }}</p>
      
      <div *ngFor="let option of currentQuestion.options">
        <button (click)="submitAnswer(option.text)">
          {{ option.text }}
        </button>
      </div>
      
      <div *ngIf="answerResult">
        <p [class.correct]="answerResult.correct">
          {{ answerResult.correct ? '✅ Correct!' : '❌ Faux!' }}
          Réponse: {{ answerResult.correctAnswer }}
        </p>
        <p>Points: +{{ answerResult.pointsEarned }} | Total: {{ answerResult.currentScore }}</p>
      </div>
    </div>
    
    <div *ngIf="isCompleted">
      <h2>🎉 Quiz Terminé!</h2>
      <p>Score final: {{ finalScore }}</p>
      <button (click)="viewLeaderboard()">Voir le classement</button>
    </div>
  `
})
export class GameplayComponent implements OnInit {
  eventId: string = '...'; // Récupéré du route ou sélection
  currentQuestion: CategoryContentResponse | null = null;
  answerResult: AnswerResult | null = null;
  isCompleted = false;
  finalScore = 0;
  
  constructor(
    private eventService: EventService,
    private scoreService: ScoreService
  ) {}
  
  async ngOnInit() {
    await this.joinAndPlay();
  }
  
  async joinAndPlay() {
    try {
      // 1. Rejoindre l'event
      const joinResponse = await this.eventService.joinEvent(this.eventId).toPromise();
      console.log("Rejoint:", joinResponse.participantId);
      
      // 2. Démarrer le quiz - récupérer première question
      await this.loadNextQuestion();
      
    } catch (error) {
      console.error("Erreur join:", error);
      // Gérer: 403 = pas autorisé, 404 = event non trouvé, etc.
    }
  }
  
  async loadNextQuestion() {
    try {
      const question = await this.eventService.getNextQuestion(this.eventId).toPromise();
      
      if (question) {
        this.currentQuestion = question;
        this.answerResult = null;
      } else {
        // Plus de questions - quiz terminé
        this.isCompleted = true;
        await this.loadFinalScore();
      }
    } catch (error) {
      console.error("Erreur question:", error);
    }
  }
  
  async submitAnswer(selectedAnswer: string) {
    if (!this.currentQuestion) return;
    
    try {
      const result = await this.eventService.submitAnswer(this.eventId, {
        contentId: this.currentQuestion.id,
        questionIndex: this.currentQuestion.questionIndex,
        selectedAnswer: selectedAnswer
      }).toPromise();
      
      this.answerResult = result;
      this.finalScore = result.currentScore;
      
      // Attendre 2 secondes puis question suivante
      setTimeout(() => {
        this.loadNextQuestion();
      }, 2000);
      
    } catch (error) {
      console.error("Erreur réponse:", error);
    }
  }
  
  async loadFinalScore() {
    const score = await this.scoreService.getMyScore().toPromise();
    console.log("Score total:", score.totalScore);
  }
  
  async viewLeaderboard() {
    const leaderboard = await this.eventService.getEventLeaderboard(this.eventId).toPromise();
    console.log("Classement:", leaderboard.entries);
  }
}
```

---

### 5.3 FLUX ADMIN: Approbation des Catégories

```typescript
// admin-dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../../core/services/category.service';

@Component({
  selector: 'app-admin-dashboard',
  template: `
    <h2>Catégories en attente d'approbation</h2>
    
    <div *ngFor="let cat of pendingCategories">
      <h4>{{ cat.name }}</h4>
      <p>{{ cat.description }}</p>
      <p>Type: {{ cat.type }} | Créé par: {{ cat.partnerName }}</p>
      
      <button (click)="approve(cat.id, true)">✅ Approuver</button>
      <button (click)="approve(cat.id, false)">❌ Rejeter</button>
    </div>
  `
})
export class AdminDashboardComponent implements OnInit {
  pendingCategories: Category[] = [];
  
  constructor(private categoryService: CategoryService) {}
  
  ngOnInit() {
    this.loadPending();
  }
  
  loadPending() {
    this.categoryService.getPendingCategories().subscribe({
      next: (cats) => this.pendingCategories = cats,
      error: (err) => console.error("Erreur chargement:", err)
    });
  }
  
  approve(categoryId: string, isApproved: boolean) {
    const status = isApproved ? 'APPROVED' : 'REJECTED';
    
    this.categoryService.approveCategory(categoryId, status).subscribe({
      next: () => {
        alert(`Catégorie ${isApproved ? 'approuvée' : 'rejetée'}!`);
        this.loadPending(); // Rafraîchir la liste
      },
      error: (err) => console.error("Erreur approbation:", err)
    });
  }
}
```

---

## 🛡️ 6. GESTION DES ERREURS HTTP

### error-handler.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ErrorHandlerService {
  handleError(error: HttpErrorResponse) {
    let message = 'Une erreur est survenue';
    
    if (error.error instanceof ErrorEvent) {
      // Erreur client
      message = `Erreur: ${error.error.message}`;
    } else {
      // Erreur serveur
      switch (error.status) {
        case 400:
          message = 'Requête invalide. Vérifiez vos données.';
          break;
        case 401:
          message = 'Session expirée. Veuillez vous reconnecter.';
          // Rediriger vers login
          break;
        case 403:
          message = 'Vous n\'avez pas les permissions nécessaires.';
          break;
        case 404:
          message = 'Ressource non trouvée.';
          break;
        case 409:
          message = 'Conflit: cette action est impossible (ex: déjà inscrit).';
          break;
        case 500:
          message = 'Erreur serveur. Veuillez réessayer plus tard.';
          break;
      }
    }
    
    console.error('API Error:', error);
    return throwError(() => new Error(message));
  }
}
```

---

## 📝 7. RÉCAPITULATIF DES ENDPOINTS PAR RÔLE

| Endpoint | Méthode | Rôles | Description |
|----------|---------|-------|-------------|
| `/api/auth/login` | POST | PUBLIC | Connexion |
| `/api/categories/partner` | POST | PARTNER_OWNER | Créer catégorie |
| `/api/categories/{id}/content` | POST | PARTNER_OWNER | Ajouter question |
| `/api/categories/admin/{id}/approve` | PATCH | ADMIN | Approuver catégorie |
| `/api/events/partner` | POST | PARTNER_OWNER | Créer event |
| `/api/events/partner/{id}/launch` | POST | PARTNER_OWNER | Lancer live |
| `/api/events/{id}/join` | POST | USER, PARTNER_OWNER | Rejoindre event |
| `/api/events/{id}/question` | GET | USER, PARTNER_OWNER | Question suivante |
| `/api/events/{id}/answer` | POST | USER, PARTNER_OWNER | Soumettre réponse |
| `/api/events/{id}/leaderboard` | GET | USER, PARTNER_OWNER | Classement |
| `/api/scores/me` | GET | USER, PARTNER_OWNER | Mon score |
| `/api/scores/global` | GET | USER, PARTNER_OWNER | Leaderboard global |

---

## ✅ 8. CHECKLIST DÉPLOIEMENT FRONTEND

- [ ] Configurer `provideHttpClient()` dans `app.config.ts` (Angular 15+)
- [ ] Ajouter `JwtInterceptor` dans les providers
- [ ] Créer les modèles TypeScript (interfaces)
- [ ] Implémenter les services (Auth, Category, Event, Score, Partner)
- [ ] Créer les composants par rôle (Admin, Partner, User)
- [ ] Gérer la navigation/routing par rôle
- [ ] Implémenter la gestion d'erreurs globale
- [ ] Tester le flux complet avec le backend

---

# 📚 ANNEXE: RÉFÉRENCE COMPLÈTE DES APIS

## A.1 AUTHENTICATION APIS (`/api/auth`)

| Méthode | Endpoint | Auth | Request Body | Response |
|---------|----------|------|--------------|----------|
| POST | `/api/auth/register` | Public | `RegisterRequest` | `ApiResponse<AuthResponse>` |
| POST | `/api/auth/login` | Public | `LoginRequest` | `ApiResponse<AuthResponse>` |
| POST | `/api/auth/refresh` | Public | `RefreshTokenRequest` | `ApiResponse<AuthResponse>` |
| POST | `/api/auth/logout` | Bearer | - | `ApiResponse<Void>` |
| GET | `/api/auth/me` | Bearer | - | `ApiResponse<UserDto>` |

### DTOs Auth
```typescript
// LoginRequest
interface LoginRequest {
  email: string;        // @Email, @NotBlank
  password: string;     // @NotBlank, min 8 chars
}

// RegisterRequest
interface RegisterRequest {
  email: string;        // @Email, @NotBlank
  password: string;     // @NotBlank, min 8 chars
  confirmPassword: string; // @NotBlank
  username?: string;    // min 2, max 50 chars
}

// AuthResponse (retourné par login/register/refresh)
interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserDto;
  expiresIn: number;   // timestamp
}

// UserDto
interface UserDto {
  id: string;
  email: string;
  username: string;
  role: 'ADMIN' | 'PARTNER_OWNER' | 'USER';
  avatar?: string;
  title?: string;
  createdAt?: string;
}

// RefreshTokenRequest
interface RefreshTokenRequest {
  refreshToken: string;
}
```

---

## A.2 EVENT APIS (`/api/events`)

### ADMIN Endpoints
| Méthode | Endpoint | Auth | Request | Response |
|---------|----------|------|---------|----------|
| POST | `/api/events/admin` | ADMIN | `CreateEventDto` | `EventDto` |
| GET | `/api/events/admin/all` | ADMIN | Query: `status?`, `page?`, `size?` | `Page<EventDto>` |
| DELETE | `/api/events/admin/{id}` | ADMIN | - | `204 No Content` |
| PATCH | `/api/events/admin/{id}/cancel` | ADMIN | - | `204 No Content` |

### PARTNER Endpoints
| Méthode | Endpoint | Auth | Request | Response |
|---------|----------|------|---------|----------|
| POST | `/api/events/partner` | PARTNER_OWNER | `CreateEventDto` | `EventDto` |
| GET | `/api/events/partner/mine` | PARTNER_OWNER | Query: `status?`, `page?`, `size?` | `Page<EventDto>` |
| PATCH | `/api/events/partner/{id}` | PARTNER_OWNER | `UpdateEventDto` | `EventDto` |
| DELETE | `/api/events/partner/{id}` | PARTNER_OWNER | - | `204 No Content` |
| POST | `/api/events/partner/{id}/launch` | PARTNER_OWNER | - | `EventDto` |

### USER Endpoints (USER + PARTNER_OWNER)
| Méthode | Endpoint | Auth | Request | Response |
|---------|----------|------|---------|----------|
| GET | `/api/events/partner/{partnerId}` | USER/PARTNER | Query: `page?`, `size?` | `Page<EventDto>` |
| POST | `/api/events/{id}/join` | USER/PARTNER | - | `JoinEventResponseDto` |
| POST | `/api/events/{id}/leave` | USER/PARTNER | - | `204 No Content` |
| GET | `/api/events/{id}/question` | USER/PARTNER | - | `CategoryContentResponseDto` |
| POST | `/api/events/{id}/answer` | USER/PARTNER | `SubmitAnswerDto` | `AnswerResultDto` |
| GET | `/api/events/{id}/leaderboard` | USER/PARTNER | - | `EventLeaderboardDto` |
| GET | `/api/events/{id}/state` | USER/PARTNER | - | `LiveEventStateDto` |

### DTOs Event
```typescript
// Enums
type EventType = 'SIMPLE' | 'LIVE';
type EventStatus = 'DRAFT' | 'PUBLISHED' | 'WAITING_ROOM' | 'IN_PROGRESS' | 'FINISHED' | 'CANCELLED';
type EventVisibility = 'PUBLIC' | 'PRIVATE';
type ParticipantStatus = 'REGISTERED' | 'WAITING' | 'PLAYING' | 'COMPLETED' | 'DISCONNECTED';

// CreateEventDto (Partner/Admin)
interface CreateEventDto {
  title: string;              // @NotBlank
  description?: string;
  coverImage?: string;
  eventType: EventType;     // @NotNull
  categoryId: string;       // @NotNull (UUID)
  visibility: EventVisibility; // @NotNull
  scheduledAt?: string;     // ISO 8601 (requis pour LIVE)
  maxParticipants?: number;
  questionTimeLimit?: number; // en secondes
  metadata?: Record<string, any>;
}

// UpdateEventDto (Partner)
interface UpdateEventDto {
  title?: string;
  description?: string;
  coverImage?: string;
  visibility?: EventVisibility;
  scheduledAt?: string;     // ISO 8601
  maxParticipants?: number;
  questionTimeLimit?: number;
  metadata?: Record<string, any>;
  isActive?: boolean;
}

// EventDto (Response)
interface EventDto {
  id: string;               // UUID
  title: string;
  description?: string;
  coverImage?: string;
  eventType: EventType;
  status: EventStatus;
  categoryId: string;       // UUID
  categoryName: string;
  partnerId: string;        // UUID
  partnerName: string;
  createdBy: string;
  visibility: EventVisibility;
  scheduledAt?: string;     // ISO 8601
  maxParticipants?: number;
  currentParticipants: number;
  totalQuestions: number;
  isActive: boolean;
  createdAt: string;        // ISO 8601
}

// JoinEventResponseDto
interface JoinEventResponseDto {
  eventId: string;          // UUID
  participantId: string;    // UUID
  status: ParticipantStatus;
  message: string;
  currentQuestion?: CategoryContentResponseDto; // Si event déjà démarré
}

// CategoryContentResponseDto (Question)
interface CategoryContentResponseDto {
  id: string;               // UUID
  categoryId: string;       // UUID
  contentType: 'QUESTION' | 'CONTENT' | 'SPINNER';
  title: string;            // La question
  description?: string;
  options: Array<{
    text: string;
    isCorrect?: boolean;    // Caché pendant le jeu
    [key: string]: any;
  }>;
  points: number;
  timeLimit: number;        // secondes
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  order: number;
  questionIndex: number;    // Index actuel (0-based)
  totalQuestions: number;   // Total des questions
}

// SubmitAnswerDto
interface SubmitAnswerDto {
  contentId: string;        // UUID @NotNull
  questionIndex: number;    // @NotNull
  selectedAnswer: string;   // @NotBlank
  responseTimeMs?: number;  // Temps de réponse en ms (pour bonus vitesse)
}

// AnswerResultDto
interface AnswerResultDto {
  isCorrect: boolean;
  pointsEarned: number;     // Points de base + bonus
  speedBonus: number;       // Points bonus pour rapidité
  correctAnswer: string;    // La bonne réponse
  currentScore: number;     // Score total actuel de l'utilisateur
  nextQuestion?: CategoryContentResponseDto; // Question suivante ou null
}

// EventLeaderboardDto
interface EventLeaderboardDto {
  eventId: string;          // UUID
  entries: LeaderboardEntryDto[];
}

// LeaderboardEntryDto
interface LeaderboardEntryDto {
  rank: number;
  userId: string;           // UUID
  username: string;
  avatar?: string;
  score: number;
  correctAnswers: number;
  isFriend: boolean;
  isCurrentUser: boolean;
}

// LiveEventStateDto (Pour reconnexion)
interface LiveEventStateDto {
  eventId: string;          // UUID
  status: EventStatus;
  currentQuestionIndex: number;
  totalQuestions: number;
  currentQuestion?: CategoryContentResponseDto;
  timeRemaining: number;    // secondes
  participantCount: number;
  leaderboard?: LeaderboardEntryDto[];
}

// FilterEventDto (Query params)
interface FilterEventDto {
  status?: EventStatus;
  type?: EventType;
  partnerId?: string;       // UUID
  search?: string;
  page?: number;            // default: 0
  limit?: number;           // default: 20
}
```

---

## A.3 CATEGORY APIS (`/api/categories`)

### ADMIN Endpoints
| Méthode | Endpoint | Auth | Request | Response |
|---------|----------|------|---------|----------|
| POST | `/api/categories/admin` | ADMIN | `CreateCategoryDto` | `ApiResponse<CategoryDto>` |
| GET | `/api/categories/admin/all` | ADMIN | Query: `type?`, `visibility?`, `status?`, `partnerId?`, `search?`, `page?`, `limit?` | `ApiResponse<Page<CategoryDto>>` |
| PATCH | `/api/categories/admin/{id}/approve` | ADMIN | `ApproveCategoryDto` | `ApiResponse<CategoryDto>` |
| DELETE | `/api/categories/admin/{id}` | ADMIN | - | `204 No Content` |

### PARTNER Endpoints
| Méthode | Endpoint | Auth | Request | Response |
|---------|----------|------|---------|----------|
| POST | `/api/categories/partner` | PARTNER_OWNER | `CreateCategoryDto` | `ApiResponse<CategoryDto>` |
| GET | `/api/categories/partner/mine` | PARTNER_OWNER | Query: `type?`, `status?`, `page?`, `limit?` | `ApiResponse<Page<CategoryDto>>` |
| PATCH | `/api/categories/partner/{id}` | PARTNER_OWNER | `UpdateCategoryDto` | `ApiResponse<CategoryDto>` |
| DELETE | `/api/categories/partner/{id}` | PARTNER_OWNER | - | `204 No Content` |

### Content Management (ADMIN + PARTNER_OWNER)
| Méthode | Endpoint | Auth | Request | Response |
|---------|----------|------|---------|----------|
| POST | `/api/categories/{categoryId}/content` | ADMIN/PARTNER | `CreateCategoryContentDto` | `ApiResponse<CategoryContentDto>` |
| PATCH | `/api/categories/content/{contentId}` | ADMIN/PARTNER | `UpdateCategoryContentDto` | `ApiResponse<CategoryContentDto>` |
| DELETE | `/api/categories/content/{contentId}` | ADMIN/PARTNER | - | `204 No Content` |
| PATCH | `/api/categories/{categoryId}/content/reorder` | ADMIN/PARTNER | `string[]` (orderedIds) | `204 No Content` |

### Public/Shared Endpoints
| Méthode | Endpoint | Auth | Request | Response |
|---------|----------|------|---------|----------|
| GET | `/api/categories` | ADMIN/PARTNER | Query: `status=APPROVED`, `page?`, `limit?` | `ApiResponse<Page<CategoryDto>>` |
| GET | `/api/categories/partner/{partnerId}/public` | USER/PARTNER/ADMIN | Query: `page?`, `limit?` | `ApiResponse<Page<CategoryDto>>` |
| GET | `/api/categories/{categoryId}` | USER/PARTNER/ADMIN | - | `ApiResponse<CategoryWithContentDto>` |

### DTOs Category
```typescript
// Enums
type CategoryType = 'QUIZ' | 'SURVEY' | 'CHALLENGE';
type CategoryStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED';
type CategoryVisibility = 'PUBLIC' | 'PRIVATE';
type CreatedByRole = 'ADMIN' | 'PARTNER';
type ContentType = 'QUESTION' | 'CONTENT' | 'SPINNER';
type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';

// CreateCategoryDto
interface CreateCategoryDto {
  name: string;             // @NotBlank, 3-255 chars
  description?: string;
  coverImage?: string;      // URL
  type: CategoryType;       // @NotNull
  visibility: CategoryVisibility; // @NotNull
  tags?: string[];
  metadata?: Record<string, any>; // config scoring, règles
}

// UpdateCategoryDto
interface UpdateCategoryDto {
  name?: string;
  description?: string;
  coverImage?: string;
  visibility?: CategoryVisibility;
  tags?: string[];
  metadata?: Record<string, any>;
}

// ApproveCategoryDto
interface ApproveCategoryDto {
  status: 'APPROVED' | 'REJECTED';
  rejectionReason?: string; // Requis si REJECTED
}

// CategoryDto (Response)
interface CategoryDto {
  id: string;               // UUID
  name: string;
  description?: string;
  coverImage?: string;
  type: CategoryType;
  status: CategoryStatus;
  visibility: CategoryVisibility;
  createdBy: CreatedByRole;
  partnerId?: string;       // UUID (null si créé par admin)
  tags?: string[];
  metadata?: Record<string, any>;
  isActive: boolean;
  createdAt: string;        // ISO 8601
  updatedAt: string;        // ISO 8601
}

// CategoryWithContentDto
interface CategoryWithContentDto extends CategoryDto {
  contents: CategoryContentDto[];
}

// CreateCategoryContentDto
interface CreateCategoryContentDto {
  contentType: ContentType; // @NotNull
  title: string;            // @NotBlank
  description?: string;
  correctAnswer: string;    // @NotBlank
  options: Array<{
    text: string;
    isCorrect: boolean;
    [key: string]: any;
  }>;
  points?: number;          // default: 10
  timeLimit?: number;       // secondes, default: 30
  difficulty?: Difficulty;  // default: MEDIUM
  order?: number;
  metadata?: Record<string, any>;
}

// UpdateCategoryContentDto
interface UpdateCategoryContentDto {
  title?: string;
  description?: string;
  correctAnswer?: string;
  options?: Array<{
    text: string;
    isCorrect: boolean;
  }>;
  points?: number;
  timeLimit?: number;
  difficulty?: Difficulty;
  order?: number;
  metadata?: Record<string, any>;
}

// CategoryContentDto (Response)
interface CategoryContentDto {
  id: string;               // UUID
  categoryId: string;       // UUID
  contentType: ContentType;
  title: string;
  description?: string;
  correctAnswer: string;
  options: Array<{
    text: string;
    isCorrect: boolean;
  }>;
  points: number;
  timeLimit: number;
  difficulty: Difficulty;
  order: number;
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

// FilterCategoryDto (Query params)
interface FilterCategoryDto {
  type?: CategoryType;
  visibility?: CategoryVisibility;
  status?: CategoryStatus;
  partnerId?: string;       // UUID
  search?: string;
  page?: number;
  limit?: number;
}
```

---

## A.4 SCORE & LEADERBOARD APIS

### ScoreController (`/api/scores`)
| Méthode | Endpoint | Auth | Response |
|---------|----------|------|----------|
| GET | `/api/scores/me` | USER/PARTNER | `UserScoreDto` |
| GET | `/api/scores/partner/{partnerId}` | USER/PARTNER | `Page<UserScoreDto>` |
| GET | `/api/scores/global` | USER/PARTNER | `Page<UserScoreDto>` |

### LeaderboardController (`/api/leaderboard`)
| Méthode | Endpoint | Auth | Query | Response |
|---------|----------|------|-------|----------|
| GET | `/api/leaderboard/top` | USER | `limit` (1-100, default: 5) | `ApiResponse<Page<UserScoreDto>>` |

### DTOs Score
```typescript
// UserScoreDto
interface UserScoreDto {
  userId: string;           // UUID
  username: string;
  totalScore: number;       // Long
  rank: number;             // Rank dans le contexte actuel
  totalEvents: number;
  partnerScore?: number;    // Long - score dans un partner spécifique
  globalRank?: number;      // Rank global
}

// PageResponse<T> (Spring Data Page)
interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;           // Page actuelle (0-based)
  first: boolean;
  last: boolean;
  empty: boolean;
}
```

---

## A.5 NOTIFICATION APIS (`/api/notifications`)

| Méthode | Endpoint | Auth | Response |
|---------|----------|------|----------|
| GET | `/api/notifications` | USER | `NotificationDto[]` |
| PATCH | `/api/notifications/{id}/read` | USER | `204 No Content` |
| PATCH | `/api/notifications/read-all` | USER | `204 No Content` |

### DTOs Notification
```typescript
interface NotificationDto {
  id: string;               // UUID
  type: string;             // EVENT_START, EVENT_END, FRIEND_REQUEST, etc.
  title: string;
  body: string;
  data?: Record<string, any>; // Payload additionnel
  isRead: boolean;
  createdAt: string;        // ISO 8601
}
```

---

## A.6 PARTNER APIS (`/api/partners`)

### Public Endpoints
| Méthode | Endpoint | Auth | Query | Response |
|---------|----------|------|-------|----------|
| GET | `/api/partners` | Public | `zoneId` (required), `page?`, `size?`, `sortBy?`, `direction?` | `ApiResponse<Page<PartnerDto>>` |
| GET | `/api/partners/top` | Public | `zoneId` (required), `page?`, `size?` | `ApiResponse<Page<PartnerDto>>` |
| GET | `/api/partners/{id}` | Public | - | `ApiResponse<PartnerDto>` |

### Partner Owner Endpoints
| Méthode | Endpoint | Auth | Request | Response |
|---------|----------|------|---------|----------|
| GET | `/api/partners/me/list` | PARTNER_OWNER | - | `ApiResponse<PartnerDto[]>` |
| POST | `/api/partners` | PARTNER_OWNER | `CreatePartnerRequest` | `ApiResponse<PartnerDto>` |
| PUT | `/api/partners/{id}` | PARTNER_OWNER | `CreatePartnerRequest` | `ApiResponse<PartnerDto>` |
| DELETE | `/api/partners/{id}` | PARTNER_OWNER | - | `ApiResponse<Void>` |

### Admin Endpoints
| Méthode | Endpoint | Auth | Response |
|---------|----------|------|----------|
| POST | `/api/partners/{id}/verify` | ADMIN | `ApiResponse<Void>` |

### Location Endpoints
| Méthode | Endpoint | Auth | Request | Response |
|---------|----------|------|---------|----------|
| POST | `/api/partners/{partnerId}/location` | PARTNER_OWNER | `LocationDto` | `ApiResponse<LocationDto>` |
| GET | `/api/partners/{partnerId}/location` | Public | - | `ApiResponse<LocationDto>` |

### Asset Endpoints
| Méthode | Endpoint | Auth | Request | Response |
|---------|----------|------|---------|----------|
| POST | `/api/partners/{partnerId}/assets/upload` | PARTNER_OWNER | `MultipartFile`, `type` | `ApiResponse<PartnerAssetDto>` |
| GET | `/api/partners/{partnerId}/assets` | Public | - | `ApiResponse<PartnerAssetDto[]>` |
| DELETE | `/api/partners/assets/{assetId}` | PARTNER_OWNER | - | `ApiResponse<Void>` |

### DTOs Partner
```typescript
// CreatePartnerRequest
interface CreatePartnerRequest {
  name: string;
  description?: string;
  zoneId: string;           // UUID
  address?: string;
  phone?: string;
  email?: string;
  website?: string;
  businessType?: string;
}

// PartnerDto
interface PartnerDto {
  id: string;               // UUID
  name: string;
  description?: string;
  zoneId: string;           // UUID
  zoneName?: string;
  ownerId: string;          // UUID
  address?: string;
  phone?: string;
  email?: string;
  website?: string;
  businessType?: string;
  isVerified: boolean;
  rating?: number;
  createdAt: string;
  updatedAt: string;
}

// LocationDto
interface LocationDto {
  id?: string;              // UUID
  partnerId?: string;       // UUID
  latitude: number;         // @NotNull
  longitude: number;        // @NotNull
  address?: string;
  city?: string;
  country?: string;
  postalCode?: string;
}

// PartnerAssetDto
interface PartnerAssetDto {
  id: string;               // UUID
  partnerId: string;        // UUID
  type: 'IMAGE' | 'COVER' | 'LOGO';
  url: string;
  filename?: string;
  size?: number;
  createdAt: string;
}
```

---

# 🛠️ SERVICES ANGULAR COMPLÈTS

## EventService (Corrigé et Complet)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// All Event DTOs
export interface CreateEventDto {
  title: string;
  description?: string;
  coverImage?: string;
  eventType: 'SIMPLE' | 'LIVE';
  categoryId: string;
  visibility: 'PUBLIC' | 'PRIVATE';
  scheduledAt?: string;
  maxParticipants?: number;
  questionTimeLimit?: number;
  metadata?: Record<string, any>;
}

export interface UpdateEventDto {
  title?: string;
  description?: string;
  coverImage?: string;
  visibility?: 'PUBLIC' | 'PRIVATE';
  scheduledAt?: string;
  maxParticipants?: number;
  questionTimeLimit?: number;
  metadata?: Record<string, any>;
  isActive?: boolean;
}

export interface EventDto {
  id: string;
  title: string;
  description?: string;
  coverImage?: string;
  eventType: 'SIMPLE' | 'LIVE';
  status: 'DRAFT' | 'PUBLISHED' | 'WAITING_ROOM' | 'IN_PROGRESS' | 'FINISHED' | 'CANCELLED';
  categoryId: string;
  categoryName: string;
  partnerId: string;
  partnerName: string;
  createdBy: string;
  visibility: 'PUBLIC' | 'PRIVATE';
  scheduledAt?: string;
  maxParticipants?: number;
  currentParticipants: number;
  totalQuestions: number;
  isActive: boolean;
  createdAt: string;
}

export interface JoinEventResponseDto {
  eventId: string;
  participantId: string;
  status: 'REGISTERED' | 'WAITING' | 'PLAYING' | 'COMPLETED' | 'DISCONNECTED';
  message: string;
  currentQuestion?: CategoryContentResponseDto;
}

export interface CategoryContentResponseDto {
  id: string;
  categoryId: string;
  contentType: 'QUESTION' | 'CONTENT' | 'SPINNER';
  title: string;
  description?: string;
  options: Array<{ text: string; isCorrect?: boolean; [key: string]: any }>;
  points: number;
  timeLimit: number;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  order: number;
  questionIndex: number;
  totalQuestions: number;
}

export interface SubmitAnswerDto {
  contentId: string;
  questionIndex: number;
  selectedAnswer: string;
  responseTimeMs?: number;
}

export interface AnswerResultDto {
  isCorrect: boolean;
  pointsEarned: number;
  speedBonus: number;
  correctAnswer: string;
  currentScore: number;
  nextQuestion?: CategoryContentResponseDto;
}

export interface LeaderboardEntryDto {
  rank: number;
  userId: string;
  username: string;
  avatar?: string;
  score: number;
  correctAnswers: number;
  isFriend: boolean;
  isCurrentUser: boolean;
}

export interface EventLeaderboardDto {
  eventId: string;
  entries: LeaderboardEntryDto[];
}

export interface LiveEventStateDto {
  eventId: string;
  status: EventDto['status'];
  currentQuestionIndex: number;
  totalQuestions: number;
  currentQuestion?: CategoryContentResponseDto;
  timeRemaining: number;
  participantCount: number;
  leaderboard?: LeaderboardEntryDto[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

@Injectable({ providedIn: 'root' })
export class EventService {
  private readonly apiUrl = `${environment.apiUrl}/events`;

  constructor(private http: HttpClient) {}

  // ==================== ADMIN ENDPOINTS ====================

  /** POST /api/events/admin - Créer un event public (Admin) */
  createAdminEvent(dto: CreateEventDto): Observable<EventDto> {
    return this.http.post<EventDto>(`${this.apiUrl}/admin`, dto);
  }

  /** GET /api/events/admin/all - Liste tous les events (Admin) */
  getAllEvents(status?: EventDto['status'], page = 0, size = 20): Observable<PageResponse<EventDto>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<EventDto>>(`${this.apiUrl}/admin/all`, { params });
  }

  /** DELETE /api/events/admin/{id} - Supprimer un event (Admin) */
  deleteEvent(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/${id}`);
  }

  /** PATCH /api/events/admin/{id}/cancel - Annuler un event (Admin) */
  cancelEvent(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/admin/${id}/cancel`, {});
  }

  // ==================== PARTNER ENDPOINTS ====================

  /** POST /api/events/partner - Créer un event (Partner) */
  createEvent(dto: CreateEventDto): Observable<EventDto> {
    return this.http.post<EventDto>(`${this.apiUrl}/partner`, dto);
  }

  /** GET /api/events/partner/mine - Mes events (Partner) */
  getMyEvents(status?: EventDto['status'], page = 0, size = 20): Observable<PageResponse<EventDto>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<EventDto>>(`${this.apiUrl}/partner/mine`, { params });
  }

  /** PATCH /api/events/partner/{id} - Modifier mon event (Partner) */
  updateMyEvent(id: string, dto: UpdateEventDto): Observable<EventDto> {
    return this.http.patch<EventDto>(`${this.apiUrl}/partner/${id}`, dto);
  }

  /** DELETE /api/events/partner/{id} - Annuler mon event (Partner) */
  cancelMyEvent(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/partner/${id}`);
  }

  /** POST /api/events/partner/{id}/launch - Lancer un event LIVE (Partner) */
  launchLiveEvent(id: string): Observable<EventDto> {
    return this.http.post<EventDto>(`${this.apiUrl}/partner/${id}/launch`, {});
  }

  // ==================== USER ENDPOINTS ====================

  /** GET /api/events/partner/{partnerId} - Events d'un partner (User) */
  getEventsByPartner(partnerId: string, page = 0, size = 20): Observable<PageResponse<EventDto>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<EventDto>>(`${this.apiUrl}/partner/${partnerId}`, { params });
  }

  /** POST /api/events/{id}/join - Rejoindre un event (User/Partner) */
  joinEvent(id: string): Observable<JoinEventResponseDto> {
    return this.http.post<JoinEventResponseDto>(`${this.apiUrl}/${id}/join`, {});
  }

  /** POST /api/events/{id}/leave - Quitter un event (User/Partner) */
  leaveEvent(id: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/leave`, {});
  }

  /** GET /api/events/{id}/question - Question suivante (User/Partner) */
  getNextQuestion(id: string): Observable<CategoryContentResponseDto> {
    return this.http.get<CategoryContentResponseDto>(`${this.apiUrl}/${id}/question`);
  }

  /** POST /api/events/{id}/answer - Soumettre une réponse (User/Partner) */
  submitAnswer(id: string, dto: SubmitAnswerDto): Observable<AnswerResultDto> {
    return this.http.post<AnswerResultDto>(`${this.apiUrl}/${id}/answer`, dto);
  }

  /** GET /api/events/{id}/leaderboard - Classement d'un event (User/Partner) */
  getEventLeaderboard(id: string): Observable<EventLeaderboardDto> {
    return this.http.get<EventLeaderboardDto>(`${this.apiUrl}/${id}/leaderboard`);
  }

  /** GET /api/events/{id}/state - État d'un event LIVE (User/Partner) */
  getLiveEventState(id: string): Observable<LiveEventStateDto> {
    return this.http.get<LiveEventStateDto>(`${this.apiUrl}/${id}/state`);
  }
}
```

---

## CategoryService (Complet)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from './event.service';

export type CategoryType = 'QUIZ' | 'SURVEY' | 'CHALLENGE';
export type CategoryStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED';
export type CategoryVisibility = 'PUBLIC' | 'PRIVATE';
export type ContentType = 'QUESTION' | 'CONTENT' | 'SPINNER';
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';

export interface CreateCategoryDto {
  name: string;
  description?: string;
  coverImage?: string;
  type: CategoryType;
  visibility: CategoryVisibility;
  tags?: string[];
  metadata?: Record<string, any>;
}

export interface UpdateCategoryDto {
  name?: string;
  description?: string;
  coverImage?: string;
  visibility?: CategoryVisibility;
  tags?: string[];
  metadata?: Record<string, any>;
}

export interface ApproveCategoryDto {
  status: 'APPROVED' | 'REJECTED';
  rejectionReason?: string;
}

export interface CategoryDto {
  id: string;
  name: string;
  description?: string;
  coverImage?: string;
  type: CategoryType;
  status: CategoryStatus;
  visibility: CategoryVisibility;
  createdBy: 'ADMIN' | 'PARTNER';
  partnerId?: string;
  tags?: string[];
  metadata?: Record<string, any>;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CategoryContentDto {
  id: string;
  categoryId: string;
  contentType: ContentType;
  title: string;
  description?: string;
  correctAnswer: string;
  options: Array<{ text: string; isCorrect: boolean }>;
  points: number;
  timeLimit: number;
  difficulty: Difficulty;
  order: number;
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCategoryContentDto {
  contentType: ContentType;
  title: string;
  description?: string;
  correctAnswer: string;
  options: Array<{ text: string; isCorrect: boolean }>;
  points?: number;
  timeLimit?: number;
  difficulty?: Difficulty;
  order?: number;
  metadata?: Record<string, any>;
}

export interface UpdateCategoryContentDto {
  title?: string;
  description?: string;
  correctAnswer?: string;
  options?: Array<{ text: string; isCorrect: boolean }>;
  points?: number;
  timeLimit?: number;
  difficulty?: Difficulty;
  order?: number;
  metadata?: Record<string, any>;
}

export interface CategoryWithContentDto extends CategoryDto {
  contents: CategoryContentDto[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly apiUrl = `${environment.apiUrl}/categories`;

  constructor(private http: HttpClient) {}

  // ==================== ADMIN ENDPOINTS ====================

  /** POST /api/categories/admin - Créer catégorie publique (Admin) */
  createAdminCategory(dto: CreateCategoryDto): Observable<ApiResponse<CategoryDto>> {
    return this.http.post<ApiResponse<CategoryDto>>(`${this.apiUrl}/admin`, dto);
  }

  /** GET /api/categories/admin/all - Toutes les catégories (Admin) */
  getAllCategories(
    filters: {
      type?: CategoryType;
      visibility?: CategoryVisibility;
      status?: CategoryStatus;
      partnerId?: string;
      search?: string;
      page?: number;
      limit?: number;
    } = {}
  ): Observable<ApiResponse<PageResponse<CategoryDto>>> {
    let params = new HttpParams();
    if (filters.type) params = params.set('type', filters.type);
    if (filters.visibility) params = params.set('visibility', filters.visibility);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.partnerId) params = params.set('partnerId', filters.partnerId);
    if (filters.search) params = params.set('search', filters.search);
    params = params.set('page', (filters.page ?? 0).toString());
    params = params.set('limit', (filters.limit ?? 20).toString());
    return this.http.get<ApiResponse<PageResponse<CategoryDto>>>(`${this.apiUrl}/admin/all`, { params });
  }

  /** PATCH /api/categories/admin/{id}/approve - Approuver/Rejeter (Admin) */
  approveCategory(id: string, dto: ApproveCategoryDto): Observable<ApiResponse<CategoryDto>> {
    return this.http.patch<ApiResponse<CategoryDto>>(`${this.apiUrl}/admin/${id}/approve`, dto);
  }

  /** DELETE /api/categories/admin/{id} - Supprimer catégorie (Admin) */
  deleteCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/${id}`);
  }

  // ==================== PARTNER ENDPOINTS ====================

  /** POST /api/categories/partner - Créer catégorie (Partner) */
  createCategory(dto: CreateCategoryDto): Observable<ApiResponse<CategoryDto>> {
    return this.http.post<ApiResponse<CategoryDto>>(`${this.apiUrl}/partner`, dto);
  }

  /** GET /api/categories/partner/mine - Mes catégories (Partner) */
  getMyCategories(
    filters: { type?: CategoryType; status?: CategoryStatus; page?: number; limit?: number } = {}
  ): Observable<ApiResponse<PageResponse<CategoryDto>>> {
    let params = new HttpParams();
    if (filters.type) params = params.set('type', filters.type);
    if (filters.status) params = params.set('status', filters.status);
    params = params.set('page', (filters.page ?? 0).toString());
    params = params.set('limit', (filters.limit ?? 20).toString());
    return this.http.get<ApiResponse<PageResponse<CategoryDto>>>(`${this.apiUrl}/partner/mine`, { params });
  }

  /** PATCH /api/categories/partner/{id} - Modifier ma catégorie (Partner) */
  updateMyCategory(id: string, dto: UpdateCategoryDto): Observable<ApiResponse<CategoryDto>> {
    return this.http.patch<ApiResponse<CategoryDto>>(`${this.apiUrl}/partner/${id}`, dto);
  }

  /** DELETE /api/categories/partner/{id} - Supprimer ma catégorie (Partner) */
  deleteMyCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/partner/${id}`);
  }

  // ==================== CONTENT MANAGEMENT ====================

  /** POST /api/categories/{categoryId}/content - Ajouter contenu */
  addContent(categoryId: string, dto: CreateCategoryContentDto): Observable<ApiResponse<CategoryContentDto>> {
    return this.http.post<ApiResponse<CategoryContentDto>>(`${this.apiUrl}/${categoryId}/content`, dto);
  }

  /** PATCH /api/categories/content/{contentId} - Modifier contenu */
  updateContent(contentId: string, dto: UpdateCategoryContentDto): Observable<ApiResponse<CategoryContentDto>> {
    return this.http.patch<ApiResponse<CategoryContentDto>>(`${this.apiUrl}/content/${contentId}`, dto);
  }

  /** DELETE /api/categories/content/{contentId} - Supprimer contenu */
  deleteContent(contentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/content/${contentId}`);
  }

  /** PATCH /api/categories/{categoryId}/content/reorder - Réordonner contenu */
  reorderContent(categoryId: string, orderedIds: string[]): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${categoryId}/content/reorder`, orderedIds);
  }

  // ==================== PUBLIC/SHARED ENDPOINTS ====================

  /** GET /api/categories - Catégories par statut (APPROVED par défaut) */
  getCategoriesByStatus(
    status: CategoryStatus = 'APPROVED',
    page = 0,
    limit = 20
  ): Observable<ApiResponse<PageResponse<CategoryDto>>> {
    const params = new HttpParams()
      .set('status', status)
      .set('page', page.toString())
      .set('limit', limit.toString());
    return this.http.get<ApiResponse<PageResponse<CategoryDto>>>(this.apiUrl, { params });
  }

  /** GET /api/categories/partner/{partnerId}/public - Catégories publiques d'un partner */
  getPublicCategoriesByPartner(partnerId: string, page = 0, limit = 20): Observable<ApiResponse<PageResponse<CategoryDto>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('limit', limit.toString());
    return this.http.get<ApiResponse<PageResponse<CategoryDto>>>(
      `${this.apiUrl}/partner/${partnerId}/public`,
      { params }
    );
  }

  /** GET /api/categories/{categoryId} - Détails catégorie avec contenu */
  getCategoryWithContent(categoryId: string): Observable<ApiResponse<CategoryWithContentDto>> {
    return this.http.get<ApiResponse<CategoryWithContentDto>>(`${this.apiUrl}/${categoryId}`);
  }
}
```

---

## ScoreService (Complet)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from './event.service';
import { ApiResponse } from './category.service';

export interface UserScoreDto {
  userId: string;
  username: string;
  totalScore: number;
  rank: number;
  totalEvents: number;
  partnerScore?: number;
  globalRank?: number;
}

@Injectable({ providedIn: 'root' })
export class ScoreService {
  private readonly scoresUrl = `${environment.apiUrl}/scores`;
  private readonly leaderboardUrl = `${environment.apiUrl}/leaderboard`;

  constructor(private http: HttpClient) {}

  /** GET /api/scores/me - Mon score personnel */
  getMyScore(): Observable<UserScoreDto> {
    return this.http.get<UserScoreDto>(`${this.scoresUrl}/me`);
  }

  /** GET /api/scores/partner/{partnerId} - Leaderboard d'un partner */
  getPartnerLeaderboard(partnerId: string, page = 0, size = 20): Observable<PageResponse<UserScoreDto>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<UserScoreDto>>(`${this.scoresUrl}/partner/${partnerId}`, { params });
  }

  /** GET /api/scores/global - Leaderboard global */
  getGlobalLeaderboard(page = 0, size = 20): Observable<PageResponse<UserScoreDto>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<UserScoreDto>>(`${this.scoresUrl}/global`, { params });
  }

  /** GET /api/leaderboard/top - Top utilisateurs (global) */
  getTopLeaderboard(limit = 5): Observable<ApiResponse<PageResponse<UserScoreDto>>> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<ApiResponse<PageResponse<UserScoreDto>>>(`${this.leaderboardUrl}/top`, { params });
  }
}
```

---

## NotificationService

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface NotificationDto {
  id: string;
  type: string;             // EVENT_START, EVENT_END, FRIEND_REQUEST, etc.
  title: string;
  body: string;
  data?: Record<string, any>;
  isRead: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly apiUrl = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  /** GET /api/notifications - Notifications non lues */
  getUnreadNotifications(): Observable<NotificationDto[]> {
    return this.http.get<NotificationDto[]>(this.apiUrl);
  }

  /** PATCH /api/notifications/{id}/read - Marquer comme lue */
  markAsRead(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/read`, {});
  }

  /** PATCH /api/notifications/read-all - Tout marquer comme lu */
  markAllAsRead(): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/read-all`, {});
  }
}
```

---

## PartnerService

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from './event.service';
import { ApiResponse } from './category.service';

export interface CreatePartnerRequest {
  name: string;
  description?: string;
  zoneId: string;
  address?: string;
  phone?: string;
  email?: string;
  website?: string;
  businessType?: string;
}

export interface PartnerDto {
  id: string;
  name: string;
  description?: string;
  zoneId: string;
  zoneName?: string;
  ownerId: string;
  address?: string;
  phone?: string;
  email?: string;
  website?: string;
  businessType?: string;
  isVerified: boolean;
  rating?: number;
  createdAt: string;
  updatedAt: string;
}

export interface LocationDto {
  id?: string;
  partnerId?: string;
  latitude: number;
  longitude: number;
  address?: string;
  city?: string;
  country?: string;
  postalCode?: string;
}

export interface PartnerAssetDto {
  id: string;
  partnerId: string;
  type: 'IMAGE' | 'COVER' | 'LOGO';
  url: string;
  filename?: string;
  size?: number;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class PartnerService {
  private readonly apiUrl = `${environment.apiUrl}/partners`;

  constructor(private http: HttpClient) {}

  // ==================== PUBLIC ENDPOINTS ====================

  /** GET /api/partners - Liste des partners par zone */
  getPartnersByZone(
    zoneId: string,
    page = 0,
    size = 20,
    sortBy = 'createdAt',
    direction: 'ASC' | 'DESC' = 'DESC'
  ): Observable<ApiResponse<PageResponse<PartnerDto>>> {
    const params = new HttpParams()
      .set('zoneId', zoneId)
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get<ApiResponse<PageResponse<PartnerDto>>>(this.apiUrl, { params });
  }

  /** GET /api/partners/top - Top partners par zone */
  getTopPartnersByZone(zoneId: string, page = 0, size = 10): Observable<ApiResponse<PageResponse<PartnerDto>>> {
    const params = new HttpParams()
      .set('zoneId', zoneId)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponse<PartnerDto>>>(`${this.apiUrl}/top`, { params });
  }

  /** GET /api/partners/{id} - Détails d'un partner */
  getPartner(id: string): Observable<ApiResponse<PartnerDto>> {
    return this.http.get<ApiResponse<PartnerDto>>(`${this.apiUrl}/${id}`);
  }

  // ==================== PARTNER OWNER ENDPOINTS ====================

  /** GET /api/partners/me/list - Mes partners */
  getMyPartners(): Observable<ApiResponse<PartnerDto[]>> {
    return this.http.get<ApiResponse<PartnerDto[]>>(`${this.apiUrl}/me/list`);
  }

  /** POST /api/partners - Créer un partner */
  createPartner(dto: CreatePartnerRequest): Observable<ApiResponse<PartnerDto>> {
    return this.http.post<ApiResponse<PartnerDto>>(this.apiUrl, dto);
  }

  /** PUT /api/partners/{id} - Modifier un partner */
  updatePartner(id: string, dto: CreatePartnerRequest): Observable<ApiResponse<PartnerDto>> {
    return this.http.put<ApiResponse<PartnerDto>>(`${this.apiUrl}/${id}`, dto);
  }

  /** DELETE /api/partners/{id} - Supprimer un partner */
  deletePartner(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  // ==================== ADMIN ENDPOINTS ====================

  /** POST /api/partners/{id}/verify - Vérifier un partner (Admin) */
  verifyPartner(id: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${id}/verify`, {});
  }

  // ==================== LOCATION ENDPOINTS ====================

  /** POST /api/partners/{partnerId}/location - Définir la localisation */
  setLocation(partnerId: string, dto: LocationDto): Observable<ApiResponse<LocationDto>> {
    return this.http.post<ApiResponse<LocationDto>>(`${this.apiUrl}/${partnerId}/location`, dto);
  }

  /** GET /api/partners/{partnerId}/location - Obtenir la localisation */
  getLocation(partnerId: string): Observable<ApiResponse<LocationDto>> {
    return this.http.get<ApiResponse<LocationDto>>(`${this.apiUrl}/${partnerId}/location`);
  }

  // ==================== ASSET ENDPOINTS ====================

  /** POST /api/partners/{partnerId}/assets/upload - Uploader un asset */
  uploadAsset(partnerId: string, file: File, type: 'IMAGE' | 'COVER' | 'LOGO'): Observable<ApiResponse<PartnerAssetDto>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    return this.http.post<ApiResponse<PartnerAssetDto>>(`${this.apiUrl}/${partnerId}/assets/upload`, formData);
  }

  /** GET /api/partners/{partnerId}/assets - Liste des assets */
  getAssets(partnerId: string): Observable<ApiResponse<PartnerAssetDto[]>> {
    return this.http.get<ApiResponse<PartnerAssetDto[]>>(`${this.apiUrl}/${partnerId}/assets`);
  }

  /** DELETE /api/partners/assets/{assetId} - Supprimer un asset */
  deleteAsset(assetId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/assets/${assetId}`);
  }
}
```

---

## AuthService (Complet)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  confirmPassword: string;
  username?: string;
}

export interface UserDto {
  id: string;
  email: string;
  username: string;
  role: 'ADMIN' | 'PARTNER_OWNER' | 'USER';
  avatar?: string;
  title?: string;
  createdAt?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserDto;
  expiresIn: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface JwtPayload {
  sub: string;              // userId
  userId: string;
  role: 'ROLE_ADMIN' | 'ROLE_PARTNER_OWNER' | 'ROLE_USER';
  email: string;
  username: string;
  exp: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  /** POST /api/auth/register - Inscription */
  register(request: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/register`, request).pipe(
      tap(response => this.saveTokens(response))
    );
  }

  /** POST /api/auth/login - Connexion */
  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/login`, request).pipe(
      tap(response => this.saveTokens(response))
    );
  }

  /** POST /api/auth/refresh - Rafraîchir le token */
  refreshToken(refreshToken: string): Observable<ApiResponse<AuthResponse>> {
    const request: RefreshTokenRequest = { refreshToken };
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/refresh`, request).pipe(
      tap(response => this.saveTokens(response))
    );
  }

  /** POST /api/auth/logout - Déconnexion */
  logout(): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/logout`, {}).pipe(
      tap(() => this.clearTokens())
    );
  }

  /** GET /api/auth/me - Utilisateur courant */
  getCurrentUser(): Observable<ApiResponse<UserDto>> {
    return this.http.get<ApiResponse<UserDto>>(`${this.apiUrl}/me`);
  }

  // ==================== TOKEN MANAGEMENT ====================

  saveTokens(response: ApiResponse<AuthResponse>): void {
    if (response.data) {
      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('refreshToken', response.data.refreshToken);
      localStorage.setItem('user', JSON.stringify(response.data.user));
    }
  }

  clearTokens(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  }

  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  getUser(): UserDto | null {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  decodeToken(token: string): JwtPayload {
    const base64 = token.split('.')[1];
    return JSON.parse(atob(base64));
  }

  isAuthenticated(): boolean {
    const token = this.getAccessToken();
    if (!token) return false;
    try {
      const payload = this.decodeToken(token);
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  isAdmin(): boolean {
    const token = this.getAccessToken();
    if (!token) return false;
    try {
      const payload = this.decodeToken(token);
      return payload.role === 'ROLE_ADMIN';
    } catch {
      return false;
    }
  }

  isPartnerOwner(): boolean {
    const token = this.getAccessToken();
    if (!token) return false;
    try {
      const payload = this.decodeToken(token);
      return payload.role === 'ROLE_PARTNER_OWNER';
    } catch {
      return false;
    }
  }

  isUser(): boolean {
    const token = this.getAccessToken();
    if (!token) return false;
    try {
      const payload = this.decodeToken(token);
      return payload.role === 'ROLE_USER';
    } catch {
      return false;
    }
  }
}
```

---

## JwtInterceptor

```typescript
import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';
import { AuthService } from './auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject = new BehaviorSubject<string | null>(null);

  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getAccessToken();

    if (token) {
      req = this.addToken(req, token);
    }

    return next.handle(req).pipe(
      catchError(error => {
        if (error instanceof HttpErrorResponse && error.status === 401) {
          return this.handle401Error(req, next);
        }
        return throwError(() => error);
      })
    );
  }

  private addToken(req: HttpRequest<any>, token: string): HttpRequest<any> {
    return req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  private handle401Error(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      const refreshToken = this.authService.getRefreshToken();

      if (refreshToken) {
        return this.authService.refreshToken(refreshToken).pipe(
          switchMap(response => {
            this.isRefreshing = false;
            this.refreshTokenSubject.next(response.data.accessToken);
            return next.handle(this.addToken(req, response.data.accessToken));
          }),
          catchError(error => {
            this.isRefreshing = false;
            this.authService.clearTokens();
            // Rediriger vers login
            window.location.href = '/login';
            return throwError(() => error);
          })
        );
      } else {
        this.isRefreshing = false;
        this.authService.clearTokens();
        window.location.href = '/login';
        return throwError(() => new Error('No refresh token'));
      }
    } else {
      return this.refreshTokenSubject.pipe(
        filter(token => token !== null),
        take(1),
        switchMap(token => next.handle(this.addToken(req, token!)))
      );
    }
  }
}
```

---

# 📝 EXEMPLES DE SCÉNARIOS COMPLÈTS

## Scénario 1: Partner crée un Event SIMPLE

```typescript
import { Component } from '@angular/core';
import { CategoryService, CreateCategoryDto, CreateCategoryContentDto } from './services/category.service';
import { EventService, CreateEventDto } from './services/event.service';

@Component({...})
export class PartnerCreateEventComponent {
  constructor(
    private categoryService: CategoryService,
    private eventService: EventService
  ) {}

  async createSimpleEventScenario() {
    try {
      // 1. Créer une catégorie
      const categoryDto: CreateCategoryDto = {
        name: 'Quiz Football 2024',
        description: 'Testez vos connaissances sur le football',
        type: 'QUIZ',
        visibility: 'PUBLIC',
        tags: ['football', 'sport', '2024']
      };
      const categoryRes = await this.categoryService.createCategory(categoryDto).toPromise();
      const categoryId = categoryRes!.data.id;
      console.log('Catégorie créée:', categoryId);

      // 2. Ajouter des questions
      const questions: CreateCategoryContentDto[] = [
        {
          contentType: 'QUESTION',
          title: 'Quelle équipe a gagné la Coupe du Monde 2022?',
          correctAnswer: 'Argentine',
          options: [
            { text: 'France', isCorrect: false },
            { text: 'Argentine', isCorrect: true },
            { text: 'Brésil', isCorrect: false },
            { text: 'Allemagne', isCorrect: false }
          ],
          points: 10,
          timeLimit: 30,
          difficulty: 'EASY',
          order: 0
        },
        {
          contentType: 'QUESTION',
          title: 'Qui est le meilleur buteur de l\'histoire des World Cups?',
          correctAnswer: 'Miroslav Klose',
          options: [
            { text: 'Pelé', isCorrect: false },
            { text: 'Ronaldo', isCorrect: false },
            { text: 'Miroslav Klose', isCorrect: true },
            { text: 'Messi', isCorrect: false }
          ],
          points: 15,
          timeLimit: 45,
          difficulty: 'MEDIUM',
          order: 1
        }
      ];

      for (const q of questions) {
        await this.categoryService.addContent(categoryId, q).toPromise();
      }
      console.log('Questions ajoutées');

      // 3. Attendre l'approbation Admin (le statut est PENDING_APPROVAL)
      alert('Catégorie créée et en attente d\'approbation par un Admin!');

    } catch (error) {
      console.error('Erreur création:', error);
    }
  }

  // Appeler APRÈS approbation Admin
  async createEventAfterApproval(categoryId: string) {
    const eventDto: CreateEventDto = {
      title: 'Quiz Football Live!',
      description: 'Un super quiz pour les fans de foot',
      eventType: 'SIMPLE',
      categoryId: categoryId,
      visibility: 'PUBLIC',
      questionTimeLimit: 30,
      maxParticipants: 100
    };

    const eventRes = await this.eventService.createEvent(eventDto).toPromise();
    console.log('Event SIMPLE créé:', eventRes);
    return eventRes;
  }
}
```

---

## Scénario 2: Partner crée et lance un Event LIVE

```typescript
async createAndLaunchLiveEvent(categoryId: string) {
  try {
    // 1. Créer l'event LIVE planifié dans 15 minutes
    const scheduledAt = new Date();
    scheduledAt.setMinutes(scheduledAt.getMinutes() + 15);

    const eventDto: CreateEventDto = {
      title: 'Quiz Live - Football Extrême!',
      description: 'Quiz en direct avec questions difficiles',
      eventType: 'LIVE',
      categoryId: categoryId,
      visibility: 'PUBLIC',
      scheduledAt: scheduledAt.toISOString(),
      questionTimeLimit: 20,
      maxParticipants: 500
    };

    const eventRes = await this.eventService.createEvent(eventDto).toPromise();
    const eventId = eventRes!.id;
    console.log('Event LIVE créé:', eventId);

    // 2. Lorsque l'heure arrive, lancer l'event
    // Cette action met le statut à WAITING_ROOM
    const launchedEvent = await this.eventService.launchLiveEvent(eventId).toPromise();
    console.log('Event lancé, statut:', launchedEvent!.status); // WAITING_ROOM

    // 3. Les utilisateurs peuvent rejoindre, puis le partner démarre le quiz
    // Le backend passe alors à IN_PROGRESS

  } catch (error) {
    console.error('Erreur:', error);
  }
}
```

---

## Scénario 3: User rejoint et joue à un Event

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { EventService, CategoryContentResponseDto, AnswerResultDto } from './services/event.service';

@Component({
  template: `
    <div *ngIf="loading">Chargement...</div>
    
    <div *ngIf="!loading && !gameCompleted && currentQuestion">
      <div class="question-header">
        <span>Question {{ currentQuestion.questionIndex + 1 }} / {{ currentQuestion.totalQuestions }}</span>
        <span class="timer">{{ timeRemaining }}s</span>
      </div>
      
      <h2>{{ currentQuestion.title }}</h2>
      <p *ngIf="currentQuestion.description">{{ currentQuestion.description }}</p>
      
      <div class="options">
        <button 
          *ngFor="let option of currentQuestion.options"
          (click)="submitAnswer(option.text)"
          [disabled]="answerSubmitted"
          [class.selected]="selectedAnswer === option.text"
          [class.correct]="showResult && option.text === correctAnswer"
          [class.wrong]="showResult && option.text === selectedAnswer && option.text !== correctAnswer">
          {{ option.text }}
        </button>
      </div>
      
      <div *ngIf="showResult" class="result">
        <p [class.correct]="lastResult?.isCorrect" [class.wrong]="!lastResult?.isCorrect">
          {{ lastResult?.isCorrect ? '✅ Correct!' : '❌ Faux!' }}
        </p>
        <p>Réponse: {{ lastResult?.correctAnswer }}</p>
        <p>Points: +{{ lastResult?.pointsEarned }} (Bonus: {{ lastResult?.speedBonus }})</p>
        <p>Score total: {{ lastResult?.currentScore }}</p>
      </div>
    </div>
    
    <div *ngIf="gameCompleted" class="completed">
      <h2>🎉 Quiz Terminé!</h2>
      <p>Score final: {{ finalScore }}</p>
      <button (click)="viewLeaderboard()">Voir le classement</button>
      <button (click)="viewGlobalLeaderboard()">Classement global</button>
    </div>
  `
})
export class GameplayComponent implements OnInit, OnDestroy {
  eventId = '...';           // Récupéré du route ou sélection
  loading = true;
  
  // Game state
  currentQuestion: CategoryContentResponseDto | null = null;
  selectedAnswer: string | null = null;
  answerSubmitted = false;
  showResult = false;
  lastResult: AnswerResultDto | null = null;
  correctAnswer: string | null = null;
  
  // Timer
  timeRemaining = 0;
  private timerInterval: any;
  
  // Progress
  gameCompleted = false;
  finalScore = 0;

  constructor(private eventService: EventService) {}

  async ngOnInit() {
    await this.joinAndStartGame();
  }

  ngOnDestroy() {
    if (this.timerInterval) clearInterval(this.timerInterval);
  }

  async joinAndStartGame() {
    try {
      // 1. Rejoindre l'event
      const joinRes = await this.eventService.joinEvent(this.eventId).toPromise();
      console.log('Rejoint:', joinRes);

      // Si l'event est déjà démarré, on reçoit la question courante
      if (joinRes?.currentQuestion) {
        this.currentQuestion = joinRes.currentQuestion;
        this.startTimer(this.currentQuestion.timeLimit);
      } else {
        // Sinon récupérer la première question
        await this.loadNextQuestion();
      }

    } catch (error: any) {
      console.error('Erreur rejoindre:', error);
      // Gérer: 403 = pas autorisé, 404 = event non trouvé, 409 = déjà rejoint
    } finally {
      this.loading = false;
    }
  }

  async loadNextQuestion() {
    try {
      this.resetQuestionState();
      const question = await this.eventService.getNextQuestion(this.eventId).toPromise();
      
      if (question) {
        this.currentQuestion = question;
        this.startTimer(question.timeLimit);
      } else {
        // Plus de questions - jeu terminé
        this.gameCompleted = true;
        this.finalScore = this.lastResult?.currentScore || 0;
      }
    } catch (error) {
      console.error('Erreur chargement question:', error);
    }
  }

  resetQuestionState() {
    this.selectedAnswer = null;
    this.answerSubmitted = false;
    this.showResult = false;
    this.lastResult = null;
    this.correctAnswer = null;
    if (this.timerInterval) clearInterval(this.timerInterval);
  }

  startTimer(seconds: number) {
    this.timeRemaining = seconds;
    this.timerInterval = setInterval(() => {
      this.timeRemaining--;
      if (this.timeRemaining <= 0) {
        clearInterval(this.timerInterval);
        if (!this.answerSubmitted) {
          this.submitAnswer(''); // Soumission vide si temps écoulé
        }
      }
    }, 1000);
  }

  async submitAnswer(answer: string) {
    if (this.answerSubmitted || !this.currentQuestion) return;
    
    this.selectedAnswer = answer;
    this.answerSubmitted = true;
    clearInterval(this.timerInterval);

    const responseTimeMs = (this.currentQuestion.timeLimit - this.timeRemaining) * 1000;

    try {
      const result = await this.eventService.submitAnswer(this.eventId, {
        contentId: this.currentQuestion.id,
        questionIndex: this.currentQuestion.questionIndex,
        selectedAnswer: answer,
        responseTimeMs: responseTimeMs
      }).toPromise();

      this.lastResult = result || null;
      this.correctAnswer = result?.correctAnswer || null;
      this.showResult = true;

      // Attendre 2 secondes puis charger la question suivante
      setTimeout(() => {
        if (result?.nextQuestion) {
          this.currentQuestion = result.nextQuestion;
          this.resetQuestionState();
          this.startTimer(result.nextQuestion.timeLimit);
        } else {
          this.gameCompleted = true;
          this.finalScore = result?.currentScore || 0;
        }
      }, 2000);

    } catch (error) {
      console.error('Erreur soumission:', error);
    }
  }

  viewLeaderboard() {
    this.eventService.getEventLeaderboard(this.eventId).subscribe({
      next: (leaderboard) => {
        console.log('Classement event:', leaderboard);
        // Afficher le classement
      },
      error: (err) => console.error('Erreur classement:', err)
    });
  }

  viewGlobalLeaderboard() {
    // Utiliser ScoreService pour le classement global
  }
}
```

---

## Scénario 4: Admin approuve une catégorie

```typescript
import { Component, OnInit } from '@angular/core';
import { CategoryService, CategoryDto } from './services/category.service';

@Component({
  template: `
    <h2>Catégories en attente d'approbation</h2>
    
    <div *ngFor="let cat of pendingCategories" class="category-card">
      <div class="header">
        <h3>{{ cat.name }}</h3>
        <span class="badge">{{ cat.type }}</span>
      </div>
      <p>{{ cat.description }}</p>
      <p class="meta">Créé par Partner: {{ cat.partnerId }}</p>
      <p class="meta">Date: {{ cat.createdAt | date }}</p>
      
      <div class="actions">
        <button class="approve" (click)="approve(cat.id, true)">✅ Approuver</button>
        <button class="reject" (click)="approve(cat.id, false)">❌ Rejeter</button>
      </div>
    </div>
    
    <div *ngIf="pendingCategories.length === 0" class="empty">
      Aucune catégorie en attente
    </div>
  `
})
export class AdminApprovalComponent implements OnInit {
  pendingCategories: CategoryDto[] = [];

  constructor(private categoryService: CategoryService) {}

  ngOnInit() {
    this.loadPendingCategories();
  }

  loadPendingCategories() {
    // Récupérer avec statut PENDING_APPROVAL
    this.categoryService.getCategoriesByStatus('PENDING_APPROVAL').subscribe({
      next: (res) => {
        this.pendingCategories = res.data.content;
      },
      error: (err) => console.error('Erreur chargement:', err)
    });
  }

  approve(categoryId: string, isApproved: boolean) {
    const dto = {
      status: isApproved ? 'APPROVED' as const : 'REJECTED' as const,
      rejectionReason: isApproved ? undefined : 'Non conforme aux critères'
    };

    this.categoryService.approveCategory(categoryId, dto).subscribe({
      next: () => {
        console.log(`Catégorie ${isApproved ? 'approuvée' : 'rejetée'}`);
        this.loadPendingCategories(); // Rafraîchir
      },
      error: (err) => console.error('Erreur approbation:', err)
    });
  }
}
```

---

## Scénario 5: Reconnexion à un Event LIVE

```typescript
async reconnectToLiveEvent(eventId: string) {
  try {
    // Récupérer l'état actuel de l'event LIVE
    const state = await this.eventService.getLiveEventState(eventId).toPromise();
    
    console.log('Statut event:', state?.status);
    console.log('Question actuelle:', state?.currentQuestionIndex, '/', state?.totalQuestions);
    console.log('Temps restant:', state?.timeRemaining);
    
    if (state?.status === 'IN_PROGRESS' && state.currentQuestion) {
      // Reprendre le jeu avec la question actuelle
      this.currentQuestion = state.currentQuestion;
      this.timeRemaining = state.timeRemaining;
      // ... continuer le jeu
    } else if (state?.status === 'WAITING_ROOM') {
      // Attendre le démarrage
      console.log('En salle d\'attente...');
    } else if (state?.status === 'FINISHED') {
      // Event terminé, afficher le classement
      this.gameCompleted = true;
      const leaderboard = await this.eventService.getEventLeaderboard(eventId).toPromise();
      console.log('Classement final:', leaderboard);
    }
  } catch (error) {
    console.error('Erreur reconnexion:', error);
  }
}
```

---

# 🗂️ TABLE RÉCAPITULATIVE DES ENDPOINTS

| Endpoint | Méthode | Rôles | Description | Request | Response |
|----------|---------|-------|-------------|---------|----------|
| **AUTH** |||||
| `/api/auth/register` | POST | Public | Inscription | `RegisterRequest` | `ApiResponse<AuthResponse>` |
| `/api/auth/login` | POST | Public | Connexion | `LoginRequest` | `ApiResponse<AuthResponse>` |
| `/api/auth/refresh` | POST | Public | Rafraîchir token | `RefreshTokenRequest` | `ApiResponse<AuthResponse>` |
| `/api/auth/logout` | POST | Bearer | Déconnexion | - | `ApiResponse<Void>` |
| `/api/auth/me` | GET | Bearer | Utilisateur courant | - | `ApiResponse<UserDto>` |
| **EVENTS - ADMIN** |||||
| `/api/events/admin` | POST | ADMIN | Créer event public | `CreateEventDto` | `EventDto` |
| `/api/events/admin/all` | GET | ADMIN | Tous les events | Query: `status?`, `page?`, `size?` | `Page<EventDto>` |
| `/api/events/admin/{id}` | DELETE | ADMIN | Supprimer event | - | `204` |
| `/api/events/admin/{id}/cancel` | PATCH | ADMIN | Annuler event | - | `204` |
| **EVENTS - PARTNER** |||||
| `/api/events/partner` | POST | PARTNER | Créer event | `CreateEventDto` | `EventDto` |
| `/api/events/partner/mine` | GET | PARTNER | Mes events | Query: `status?`, `page?`, `size?` | `Page<EventDto>` |
| `/api/events/partner/{id}` | PATCH | PARTNER | Modifier event | `UpdateEventDto` | `EventDto` |
| `/api/events/partner/{id}` | DELETE | PARTNER | Annuler mon event | - | `204` |
| `/api/events/partner/{id}/launch` | POST | PARTNER | Lancer LIVE | - | `EventDto` |
| **EVENTS - USER** |||||
| `/api/events/partner/{partnerId}` | GET | USER/PARTNER | Events par partner | Query: `page?`, `size?` | `Page<EventDto>` |
| `/api/events/{id}/join` | POST | USER/PARTNER | Rejoindre event | - | `JoinEventResponseDto` |
| `/api/events/{id}/leave` | POST | USER/PARTNER | Quitter event | - | `204` |
| `/api/events/{id}/question` | GET | USER/PARTNER | Question suivante | - | `CategoryContentResponseDto` |
| `/api/events/{id}/answer` | POST | USER/PARTNER | Soumettre réponse | `SubmitAnswerDto` | `AnswerResultDto` |
| `/api/events/{id}/leaderboard` | GET | USER/PARTNER | Classement event | - | `EventLeaderboardDto` |
| `/api/events/{id}/state` | GET | USER/PARTNER | État LIVE | - | `LiveEventStateDto` |
| **CATEGORIES - ADMIN** |||||
| `/api/categories/admin` | POST | ADMIN | Créer catégorie | `CreateCategoryDto` | `ApiResponse<CategoryDto>` |
| `/api/categories/admin/all` | GET | ADMIN | Toutes catégories | Query filters | `ApiResponse<Page<CategoryDto>>` |
| `/api/categories/admin/{id}/approve` | PATCH | ADMIN | Approuver/Rejeter | `ApproveCategoryDto` | `ApiResponse<CategoryDto>` |
| `/api/categories/admin/{id}` | DELETE | ADMIN | Supprimer | - | `204` |
| **CATEGORIES - PARTNER** |||||
| `/api/categories/partner` | POST | PARTNER | Créer catégorie | `CreateCategoryDto` | `ApiResponse<CategoryDto>` |
| `/api/categories/partner/mine` | GET | PARTNER | Mes catégories | Query filters | `ApiResponse<Page<CategoryDto>>` |
| `/api/categories/partner/{id}` | PATCH | PARTNER | Modifier | `UpdateCategoryDto` | `ApiResponse<CategoryDto>` |
| `/api/categories/partner/{id}` | DELETE | PARTNER | Supprimer | - | `204` |
| **CATEGORIES - CONTENT** |||||
| `/api/categories/{id}/content` | POST | ADMIN/PARTNER | Ajouter contenu | `CreateCategoryContentDto` | `ApiResponse<CategoryContentDto>` |
| `/api/categories/content/{id}` | PATCH | ADMIN/PARTNER | Modifier contenu | `UpdateCategoryContentDto` | `ApiResponse<CategoryContentDto>` |
| `/api/categories/content/{id}` | DELETE | ADMIN/PARTNER | Supprimer contenu | - | `204` |
| `/api/categories/{id}/content/reorder` | PATCH | ADMIN/PARTNER | Réordonner | `UUID[]` | `204` |
| **CATEGORIES - PUBLIC** |||||
| `/api/categories` | GET | ADMIN/PARTNER | Par statut | `status=APPROVED` | `ApiResponse<Page<CategoryDto>>` |
| `/api/categories/partner/{id}/public` | GET | USER/PARTNER/ADMIN | Publiques du partner | Query: `page?`, `limit?` | `ApiResponse<Page<CategoryDto>>` |
| `/api/categories/{id}` | GET | USER/PARTNER/ADMIN | Détails avec contenu | - | `ApiResponse<CategoryWithContentDto>` |
| **SCORES** |||||
| `/api/scores/me` | GET | USER/PARTNER | Mon score | - | `UserScoreDto` |
| `/api/scores/partner/{id}` | GET | USER/PARTNER | Leaderboard partner | Query: `page?`, `size?` | `Page<UserScoreDto>` |
| `/api/scores/global` | GET | USER/PARTNER | Leaderboard global | Query: `page?`, `size?` | `Page<UserScoreDto>` |
| `/api/leaderboard/top` | GET | USER | Top utilisateurs | `limit` (1-100) | `ApiResponse<Page<UserScoreDto>>` |
| **NOTIFICATIONS** |||||
| `/api/notifications` | GET | USER | Non lues | - | `NotificationDto[]` |
| `/api/notifications/{id}/read` | PATCH | USER | Marquer lue | - | `204` |
| `/api/notifications/read-all` | PATCH | USER | Tout marquer lu | - | `204` |
| **PARTNERS** |||||
| `/api/partners` | GET | Public | Par zone | `zoneId`, `page?`, `size?` | `ApiResponse<Page<PartnerDto>>` |
| `/api/partners/top` | GET | Public | Top zone | `zoneId`, `page?`, `size?` | `ApiResponse<Page<PartnerDto>>` |
| `/api/partners/{id}` | GET | Public | Détails | - | `ApiResponse<PartnerDto>` |
| `/api/partners/me/list` | GET | PARTNER | Mes partners | - | `ApiResponse<PartnerDto[]>` |
| `/api/partners` | POST | PARTNER | Créer | `CreatePartnerRequest` | `ApiResponse<PartnerDto>` |
| `/api/partners/{id}` | PUT | PARTNER | Modifier | `CreatePartnerRequest` | `ApiResponse<PartnerDto>` |
| `/api/partners/{id}` | DELETE | PARTNER | Supprimer | - | `ApiResponse<Void>` |
| `/api/partners/{id}/verify` | POST | ADMIN | Vérifier | - | `ApiResponse<Void>` |
| `/api/partners/{id}/location` | POST | PARTNER | Localisation | `LocationDto` | `ApiResponse<LocationDto>` |
| `/api/partners/{id}/location` | GET | Public | Obtenir loc | - | `ApiResponse<LocationDto>` |
| `/api/partners/{id}/assets/upload` | POST | PARTNER | Upload asset | `MultipartFile`, `type` | `ApiResponse<PartnerAssetDto>` |
| `/api/partners/{id}/assets` | GET | Public | Assets | - | `ApiResponse<PartnerAssetDto[]>` |
| `/api/partners/assets/{id}` | DELETE | PARTNER | Supprimer asset | - | `ApiResponse<Void>` |

---

**🎉 Guide complet et corrigé prêt pour le développement Frontend!**


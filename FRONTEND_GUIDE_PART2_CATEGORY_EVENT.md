# Frontend Guide - Partie 2 & 3 : Category & Event Management (All Roles)

## 🎯 Objectif
Implémenter la gestion complète des Catégories et Événements pour USER, PARTNER et ADMIN.

---

## 📊 Architecture des Rôles & Permissions

| Feature | USER | PARTNER | ADMIN |
|---------|------|---------|-------|
| **Browse Events** | ✅ | ✅ | ✅ |
| **Join/Play Events** | ✅ | ✅ | ✅ |
| **Create Categories** | ❌ | ✅ (pending approval) | ✅ (auto-approved) |
| **Manage Own Categories** | ❌ | ✅ | ✅ |
| **Approve Categories** | ❌ | ❌ | ✅ |
| **Create Events** | ❌ | ✅ (own categories) | ✅ (any categories) |
| **Manage Own Events** | ❌ | ✅ | ✅ |
| **Cancel Any Event** | ❌ | ❌ | ✅ |

---

## 1. Types TypeScript Complets (src/types/index.ts)

### Enums
```typescript
// Category Enums
export enum CategoryType {
  QUIZ = 'QUIZ',
  QUESTION = 'QUESTION',
  SPINNER = 'SPINNER',
  MIXED = 'MIXED'
}

export enum CategoryStatus {
  DRAFT = 'DRAFT',
  PENDING_APPROVAL = 'PENDING_APPROVAL',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

export enum CategoryVisibility {
  PUBLIC = 'PUBLIC',
  PRIVATE = 'PRIVATE',
  PARTNER_ONLY = 'PARTNER_ONLY'
}

export enum CreatedByRole {
  ADMIN = 'ADMIN',
  PARTNER = 'PARTNER'
}

// Event Enums
export enum EventType {
  SIMPLE = 'SIMPLE',
  LIVE = 'LIVE'
}

export enum EventStatus {
  DRAFT = 'DRAFT',
  SCHEDULED = 'SCHEDULED',
  WAITING_ROOM = 'WAITING_ROOM',
  LIVE = 'LIVE',
  FINISHED = 'FINISHED',
  CANCELLED = 'CANCELLED'
}

export enum EventVisibility {
  PUBLIC = 'PUBLIC',
  PRIVATE = 'PRIVATE'
}

export enum ParticipantStatus {
  WAITING = 'WAITING',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  ABANDONED = 'ABANDONED'
}

export enum ContentType {
  QUIZ = 'QUIZ',
  QUESTION = 'QUESTION',
  SPINNER = 'SPINNER'
}

export enum Difficulty {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD'
}
```

### Category Types
```typescript
// Category DTO
export interface Category {
  id: string;
  name: string;
  description?: string;
  coverImage?: string;
  type: CategoryType;
  status: CategoryStatus;
  visibility: CategoryVisibility;
  createdBy: CreatedByRole;
  partnerId?: string;
  tags?: string[];
  metadata?: Record<string, any>;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// Category with Content
export interface CategoryWithContent extends Category {
  contents: CategoryContent[];
}

// Category Content
export interface CategoryContent {
  id: string;
  categoryId: string;
  contentType: ContentType;
  title: string;
  description?: string;
  correctAnswer?: string;
  options?: Array<{
    id?: string;
    label: string;
    value: string;
    [key: string]: any;
  }>;
  points: number;
  timeLimit?: number;
  difficulty: Difficulty;
  order: number;
  metadata?: Record<string, any>;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// Create/Update DTOs
export interface CreateCategoryRequest {
  name: string;
  description?: string;
  coverImage?: string;
  type: CategoryType;
  visibility: CategoryVisibility;
  tags?: string[];
  metadata?: Record<string, any>;
}

export interface UpdateCategoryRequest {
  name?: string;
  description?: string;
  coverImage?: string;
  visibility?: CategoryVisibility;
  tags?: string[];
  metadata?: Record<string, any>;
}

export interface CreateCategoryContentRequest {
  contentType: ContentType;
  title: string;
  description?: string;
  correctAnswer?: string;
  options?: Array<{
    label: string;
    value: string;
  }>;
  points?: number;
  timeLimit?: number;
  difficulty?: Difficulty;
  order?: number;
  metadata?: Record<string, any>;
}

export interface UpdateCategoryContentRequest {
  title?: string;
  description?: string;
  correctAnswer?: string;
  options?: Array<{
    label: string;
    value: string;
  }>;
  points?: number;
  timeLimit?: number;
  difficulty?: Difficulty;
  order?: number;
  metadata?: Record<string, any>;
}

// Approve/Reject Category
export interface ApproveCategoryRequest {
  approved: boolean;
  rejectionReason?: string;
}

// Filter DTO
export interface CategoryFilter {
  page?: number;
  limit?: number;
  status?: CategoryStatus;
  type?: CategoryType;
  visibility?: CategoryVisibility;
  partnerId?: string;
  search?: string;
}
```

### Event Types
```typescript
// Event DTO
export interface Event {
  id: string;
  title: string;
  description?: string;
  coverImage?: string;
  eventType: EventType;
  status: EventStatus;
  categoryId: string;
  categoryName?: string;
  partnerId?: string;
  partnerName?: string;
  createdBy: string;
  visibility: EventVisibility;
  scheduledAt?: string;
  maxParticipants?: number;
  currentParticipants: number;
  totalQuestions: number;
  isActive: boolean;
  createdAt: string;
}

// Create/Update Event
export interface CreateEventRequest {
  title: string;
  description?: string;
  coverImage?: string;
  eventType: EventType;
  categoryId: string;
  visibility: EventVisibility;
  scheduledAt?: string; // ISO 8601
  maxParticipants?: number;
  questionTimeLimit?: number;
  metadata?: Record<string, any>;
}

export interface UpdateEventRequest {
  title?: string;
  description?: string;
  coverImage?: string;
  visibility?: EventVisibility;
  scheduledAt?: string;
  maxParticipants?: number;
  questionTimeLimit?: number;
  metadata?: Record<string, any>;
  isActive?: boolean;
}

// Filter DTO
export interface EventFilter {
  page?: number;
  limit?: number;
  status?: EventStatus;
  partnerId?: string;
}

// Join Event
export interface JoinEventResponse {
  eventId: string;
  participantId: string;
  status: ParticipantStatus;
  message: string;
  currentQuestion?: CategoryContentResponse;
}

// Question/Answer
export interface CategoryContentResponse {
  id: string;
  categoryId: string;
  contentType: ContentType;
  title: string;
  description?: string;
  options?: Array<{
    label: string;
    value: string;
  }>;
  points: number;
  timeLimit?: number;
  difficulty: Difficulty;
  order: number;
  questionIndex: number;
  totalQuestions: number;
}

export interface SubmitAnswerRequest {
  contentId: string;
  questionIndex: number;
  selectedAnswer: string;
  responseTimeMs?: number;
}

export interface AnswerResult {
  isCorrect: boolean;
  pointsEarned: number;
  speedBonus: number;
  correctAnswer: string;
  currentScore: number;
  nextQuestion?: CategoryContentResponse;
}

// Leaderboard
export interface LeaderboardEntry {
  rank: number;
  userId: string;
  username: string;
  avatar?: string;
  score: number;
  correctAnswers: number;
  isFriend: boolean;
  isCurrentUser: boolean;
}

export interface EventLeaderboard {
  eventId: string;
  entries: LeaderboardEntry[];
}

// Pagination
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```

---

## 2. Category API (src/api/category.ts)

```typescript
import apiClient from './client';
import {
  ApiResponse,
  PageResponse,
  Category,
  CategoryWithContent,
  CategoryContent,
  CategoryFilter,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  CreateCategoryContentRequest,
  UpdateCategoryContentRequest,
  ApproveCategoryRequest,
  CategoryStatus
} from '../types';

// ==========================================
// ADMIN ENDPOINTS
// ==========================================

export const adminCategoryApi = {
  /** POST /api/categories/admin - Create public category (auto-approved) */
  createPublicCategory: async (data: CreateCategoryRequest): Promise<ApiResponse<Category>> => {
    const response = await apiClient.post<ApiResponse<Category>>('/categories/admin', data);
    return response.data;
  },

  /** GET /api/categories/admin/all - Get all categories with filters */
  getAllCategories: async (filter: CategoryFilter): Promise<ApiResponse<PageResponse<Category>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Category>>>('/categories/admin/all', {
      params: filter
    });
    return response.data;
  },

  /** PATCH /api/categories/admin/{id}/approve - Approve or reject category */
  approveCategory: async (id: string, data: ApproveCategoryRequest): Promise<ApiResponse<Category>> => {
    const response = await apiClient.patch<ApiResponse<Category>>(`/categories/admin/${id}/approve`, data);
    return response.data;
  },

  /** DELETE /api/categories/admin/{id} - Delete any category */
  deleteCategory: async (id: string): Promise<void> => {
    await apiClient.delete(`/categories/admin/${id}`);
  }
};

// ==========================================
// PARTNER ENDPOINTS
// ==========================================

export const partnerCategoryApi = {
  /** POST /api/categories/partner - Create category (pending approval) */
  createCategory: async (data: CreateCategoryRequest): Promise<ApiResponse<Category>> => {
    const response = await apiClient.post<ApiResponse<Category>>('/categories/partner', data);
    return response.data;
  },

  /** GET /api/categories/partner/mine - Get my categories */
  getMyCategories: async (filter?: CategoryFilter): Promise<ApiResponse<PageResponse<Category>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Category>>>('/categories/partner/mine', {
      params: filter
    });
    return response.data;
  },

  /** PATCH /api/categories/partner/{id} - Update my category (only DRAFT/PENDING) */
  updateCategory: async (id: string, data: UpdateCategoryRequest): Promise<ApiResponse<Category>> => {
    const response = await apiClient.patch<ApiResponse<Category>>(`/categories/partner/${id}`, data);
    return response.data;
  },

  /** DELETE /api/categories/partner/{id} - Delete my category */
  deleteCategory: async (id: string): Promise<void> => {
    await apiClient.delete(`/categories/partner/${id}`);
  }
};

// ==========================================
// SHARED ENDPOINTS (Admin + Partner)
// ==========================================

export const categoryContentApi = {
  /** POST /api/categories/{categoryId}/content - Add content */
  addContent: async (categoryId: string, data: CreateCategoryContentRequest): Promise<ApiResponse<CategoryContent>> => {
    const response = await apiClient.post<ApiResponse<CategoryContent>>(`/categories/${categoryId}/content`, data);
    return response.data;
  },

  /** PATCH /api/categories/content/{contentId} - Update content */
  updateContent: async (contentId: string, data: UpdateCategoryContentRequest): Promise<ApiResponse<CategoryContent>> => {
    const response = await apiClient.patch<ApiResponse<CategoryContent>>(`/categories/content/${contentId}`, data);
    return response.data;
  },

  /** DELETE /api/categories/content/{contentId} - Delete content */
  deleteContent: async (contentId: string): Promise<void> => {
    await apiClient.delete(`/categories/content/${contentId}`);
  },

  /** PATCH /api/categories/{categoryId}/content/reorder - Reorder content */
  reorderContent: async (categoryId: string, orderedIds: string[]): Promise<void> => {
    await apiClient.patch(`/categories/${categoryId}/content/reorder`, orderedIds);
  },

  /** GET /api/categories?status=APPROVED - Get approved categories for event creation */
  getApprovedCategories: async (page = 0, limit = 20): Promise<ApiResponse<PageResponse<Category>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Category>>>('/categories', {
      params: { status: CategoryStatus.APPROVED, page, limit }
    });
    return response.data;
  }
};

// ==========================================
// PUBLIC ENDPOINTS (All authenticated users)
// ==========================================

export const publicCategoryApi = {
  /** GET /api/categories/partner/{partnerId}/public - Get partner's public categories */
  getPublicCategoriesByPartner: async (partnerId: string, page = 0, limit = 20): Promise<ApiResponse<PageResponse<Category>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Category>>>(`/categories/partner/${partnerId}/public`, {
      params: { page, limit }
    });
    return response.data;
  },

  /** GET /api/categories/{categoryId} - Get category with full content */
  getCategoryWithContent: async (categoryId: string): Promise<ApiResponse<CategoryWithContent>> => {
    const response = await apiClient.get<ApiResponse<CategoryWithContent>>(`/categories/${categoryId}`);
    return response.data;
  }
};
```

---

## 3. Event API (src/api/event.ts)

```typescript
import apiClient from './client';
import {
  ApiResponse,
  PageResponse,
  Event,
  EventFilter,
  CreateEventRequest,
  UpdateEventRequest,
  JoinEventResponse,
  CategoryContentResponse,
  SubmitAnswerRequest,
  AnswerResult,
  EventLeaderboard,
  EventStatus
} from '../types';

// ==========================================
// ADMIN ENDPOINTS
// ==========================================

export const adminEventApi = {
  /** POST /api/events/admin - Create public event */
  createEvent: async (data: CreateEventRequest): Promise<ApiResponse<Event>> => {
    const response = await apiClient.post<ApiResponse<Event>>('/events/admin', data);
    return response.data;
  },

  /** GET /api/events/admin/all - Get all events */
  getAllEvents: async (filter?: EventFilter): Promise<ApiResponse<PageResponse<Event>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Event>>>('/events/admin/all', {
      params: filter
    });
    return response.data;
  },

  /** DELETE /api/events/admin/{id} - Delete any event */
  deleteEvent: async (id: string): Promise<void> => {
    await apiClient.delete(`/events/admin/${id}`);
  },

  /** PATCH /api/events/admin/{id}/cancel - Cancel any event */
  cancelEvent: async (id: string): Promise<void> => {
    await apiClient.patch(`/events/admin/${id}/cancel`);
  }
};

// ==========================================
// PARTNER ENDPOINTS
// ==========================================

export const partnerEventApi = {
  /** POST /api/events/partner - Create partner event */
  createEvent: async (data: CreateEventRequest): Promise<ApiResponse<Event>> => {
    const response = await apiClient.post<ApiResponse<Event>>('/events/partner', data);
    return response.data;
  },

  /** GET /api/events/partner/mine - Get my events */
  getMyEvents: async (filter?: EventFilter): Promise<ApiResponse<PageResponse<Event>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Event>>>('/events/partner/mine', {
      params: filter
    });
    return response.data;
  },

  /** PATCH /api/events/partner/{id} - Update my event */
  updateEvent: async (id: string, data: UpdateEventRequest): Promise<ApiResponse<Event>> => {
    const response = await apiClient.patch<ApiResponse<Event>>(`/events/partner/${id}`, data);
    return response.data;
  },

  /** DELETE /api/events/partner/{id} - Cancel my event */
  cancelEvent: async (id: string): Promise<void> => {
    await apiClient.delete(`/events/partner/${id}`);
  },

  /** POST /api/events/partner/{id}/launch - Launch live event */
  launchLiveEvent: async (id: string): Promise<ApiResponse<Event>> => {
    const response = await apiClient.post<ApiResponse<Event>>(`/events/partner/${id}/launch`);
    return response.data;
  }
};

// ==========================================
// USER ENDPOINTS (Participation)
// ==========================================

export const userEventApi = {
  /** GET /api/events/partner/{partnerId} - Get events by partner */
  getEventsByPartner: async (partnerId: string, page = 0, limit = 20): Promise<ApiResponse<PageResponse<Event>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Event>>>(`/events/partner/${partnerId}`, {
      params: { page, size: limit }
    });
    return response.data;
  },

  /** POST /api/events/{id}/join - Join event */
  joinEvent: async (id: string): Promise<ApiResponse<JoinEventResponse>> => {
    const response = await apiClient.post<ApiResponse<JoinEventResponse>>(`/events/${id}/join`);
    return response.data;
  },

  /** POST /api/events/{id}/leave - Leave event */
  leaveEvent: async (id: string): Promise<void> => {
    await apiClient.post(`/events/${id}/leave`);
  },

  /** GET /api/events/{id}/question - Get next question (Simple events) */
  getNextQuestion: async (id: string): Promise<ApiResponse<CategoryContentResponse>> => {
    const response = await apiClient.get<ApiResponse<CategoryContentResponse>>(`/events/${id}/question`);
    return response.data;
  },

  /** POST /api/events/{id}/answer - Submit answer */
  submitAnswer: async (id: string, data: SubmitAnswerRequest): Promise<ApiResponse<AnswerResult>> => {
    const response = await apiClient.post<ApiResponse<AnswerResult>>(`/events/${id}/answer`, data);
    return response.data;
  },

  /** GET /api/events/{id}/leaderboard - Get leaderboard */
  getLeaderboard: async (id: string): Promise<ApiResponse<EventLeaderboard>> => {
    const response = await apiClient.get<ApiResponse<EventLeaderboard>>(`/events/${id}/leaderboard`);
    return response.data;
  },

  /** GET /api/events/{id}/state - Get live event state */
  getLiveState: async (id: string): Promise<ApiResponse<any>> => {
    const response = await apiClient.get<ApiResponse<any>>(`/events/${id}/state`);
    return response.data;
  }
};
```

---

## 4. React Hooks (src/hooks/)

### useCategories.ts
```typescript
import { useState, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  adminCategoryApi,
  partnerCategoryApi,
  categoryContentApi,
  publicCategoryApi
} from '../api/category';
import {
  Category,
  CategoryContent,
  CategoryFilter,
  CategoryStatus,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  CreateCategoryContentRequest,
  ApproveCategoryRequest
} from '../types';

export const useCategories = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isAdmin = user?.role === 'ADMIN';
  const isPartner = user?.role === 'PARTNER_OWNER';

  // ==========================================
  // CATEGORY CRUD
  // ==========================================

  const createCategory = useCallback(async (data: CreateCategoryRequest): Promise<Category | null> => {
    setLoading(true);
    setError(null);
    try {
      const response = isAdmin
        ? await adminCategoryApi.createPublicCategory(data)
        : await partnerCategoryApi.createCategory(data);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create category');
      return null;
    } finally {
      setLoading(false);
    }
  }, [isAdmin]);

  const getCategories = useCallback(async (filter?: CategoryFilter) => {
    setLoading(true);
    setError(null);
    try {
      const response = isAdmin
        ? await adminCategoryApi.getAllCategories(filter || {})
        : await partnerCategoryApi.getMyCategories(filter);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch categories');
      return null;
    } finally {
      setLoading(false);
    }
  }, [isAdmin]);

  const getApprovedCategories = useCallback(async (page = 0, limit = 20) => {
    setLoading(true);
    setError(null);
    try {
      const response = await categoryContentApi.getApprovedCategories(page, limit);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch categories');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const updateCategory = useCallback(async (id: string, data: UpdateCategoryRequest): Promise<Category | null> => {
    setLoading(true);
    setError(null);
    try {
      const response = await partnerCategoryApi.updateCategory(id, data);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update category');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const deleteCategory = useCallback(async (id: string): Promise<boolean> => {
    setLoading(true);
    setError(null);
    try {
      isAdmin
        ? await adminCategoryApi.deleteCategory(id)
        : await partnerCategoryApi.deleteCategory(id);
      return true;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete category');
      return false;
    } finally {
      setLoading(false);
    }
  }, [isAdmin]);

  const approveCategory = useCallback(async (id: string, data: ApproveCategoryRequest): Promise<Category | null> => {
    setLoading(true);
    setError(null);
    try {
      const response = await adminCategoryApi.approveCategory(id, data);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to approve category');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  // ==========================================
  // CONTENT MANAGEMENT
  // ==========================================

  const addContent = useCallback(async (categoryId: string, data: CreateCategoryContentRequest): Promise<CategoryContent | null> => {
    setLoading(true);
    setError(null);
    try {
      const response = await categoryContentApi.addContent(categoryId, data);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to add content');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const getCategoryWithContent = useCallback(async (categoryId: string) => {
    setLoading(true);
    setError(null);
    try {
      const response = await publicCategoryApi.getCategoryWithContent(categoryId);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch category');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    loading,
    error,
    createCategory,
    getCategories,
    getApprovedCategories,
    updateCategory,
    deleteCategory,
    approveCategory,
    addContent,
    getCategoryWithContent
  };
};
```

### useEvents.ts
```typescript
import { useState, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  adminEventApi,
  partnerEventApi,
  userEventApi
} from '../api/event';
import {
  Event,
  EventFilter,
  CreateEventRequest,
  UpdateEventRequest,
  JoinEventResponse,
  CategoryContentResponse,
  SubmitAnswerRequest,
  AnswerResult,
  EventLeaderboard
} from '../types';

export const useEvents = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isAdmin = user?.role === 'ADMIN';
  const isPartner = user?.role === 'PARTNER_OWNER';

  // ==========================================
  // EVENT CRUD
  // ==========================================

  const createEvent = useCallback(async (data: CreateEventRequest): Promise<Event | null> => {
    setLoading(true);
    setError(null);
    try {
      const response = isAdmin
        ? await adminEventApi.createEvent(data)
        : await partnerEventApi.createEvent(data);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create event');
      return null;
    } finally {
      setLoading(false);
    }
  }, [isAdmin]);

  const getEvents = useCallback(async (filter?: EventFilter) => {
    setLoading(true);
    setError(null);
    try {
      const response = isAdmin
        ? await adminEventApi.getAllEvents(filter)
        : await partnerEventApi.getMyEvents(filter);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch events');
      return null;
    } finally {
      setLoading(false);
    }
  }, [isAdmin]);

  const getEventsByPartner = useCallback(async (partnerId: string, page = 0, limit = 20) => {
    setLoading(true);
    setError(null);
    try {
      const response = await userEventApi.getEventsByPartner(partnerId, page, limit);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch events');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const updateEvent = useCallback(async (id: string, data: UpdateEventRequest): Promise<Event | null> => {
    setLoading(true);
    setError(null);
    try {
      const response = await partnerEventApi.updateEvent(id, data);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update event');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const cancelEvent = useCallback(async (id: string): Promise<boolean> => {
    setLoading(true);
    setError(null);
    try {
      isAdmin
        ? await adminEventApi.cancelEvent(id)
        : await partnerEventApi.cancelEvent(id);
      return true;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to cancel event');
      return false;
    } finally {
      setLoading(false);
    }
  }, [isAdmin]);

  const deleteEvent = useCallback(async (id: string): Promise<boolean> => {
    setLoading(true);
    setError(null);
    try {
      await adminEventApi.deleteEvent(id);
      return true;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete event');
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  const launchLiveEvent = useCallback(async (id: string): Promise<Event | null> => {
    setLoading(true);
    setError(null);
    try {
      const response = await partnerEventApi.launchLiveEvent(id);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to launch event');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  // ==========================================
  // PARTICIPATION
  // ==========================================

  const joinEvent = useCallback(async (id: string): Promise<JoinEventResponse | null> => {
    setLoading(true);
    setError(null);
    try {
      const response = await userEventApi.joinEvent(id);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to join event');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const leaveEvent = useCallback(async (id: string): Promise<boolean> => {
    setLoading(true);
    setError(null);
    try {
      await userEventApi.leaveEvent(id);
      return true;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to leave event');
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  const getNextQuestion = useCallback(async (id: string): Promise<CategoryContentResponse | null> => {
    setLoading(true);
    setError(null);
    try {
      const response = await userEventApi.getNextQuestion(id);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to get question');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const submitAnswer = useCallback(async (id: string, data: SubmitAnswerRequest): Promise<AnswerResult | null> => {
    setLoading(true);
    setError(null);
    try {
      const response = await userEventApi.submitAnswer(id, data);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to submit answer');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const getLeaderboard = useCallback(async (id: string): Promise<EventLeaderboard | null> => {
    setLoading(true);
    setError(null);
    try {
      const response = await userEventApi.getLeaderboard(id);
      return response.success ? response.data : null;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to get leaderboard');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    loading,
    error,
    createEvent,
    getEvents,
    getEventsByPartner,
    updateEvent,
    cancelEvent,
    deleteEvent,
    launchLiveEvent,
    joinEvent,
    leaveEvent,
    getNextQuestion,
    submitAnswer,
    getLeaderboard
  };
};
```

---

## 5. Composants UI

### CategoryManager.tsx (Admin/Partner)
```typescript
import React, { useState, useEffect } from 'react';
import { useCategories } from '../hooks/useCategories';
import { Category, CategoryStatus, CreateCategoryRequest } from '../types';

interface CategoryManagerProps {
  mode: 'admin' | 'partner';
}

export const CategoryManager: React.FC<CategoryManagerProps> = ({ mode }) => {
  const { 
    loading, 
    error, 
    getCategories, 
    createCategory, 
    updateCategory, 
    deleteCategory,
    approveCategory 
  } = useCategories();
  
  const [categories, setCategories] = useState<Category[]>([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    const result = await getCategories();
    if (result) {
      setCategories(result.content);
    }
  };

  const handleCreate = async (data: CreateCategoryRequest) => {
    const result = await createCategory(data);
    if (result) {
      setShowCreateModal(false);
      loadCategories();
    }
  };

  const handleApprove = async (id: string, approved: boolean) => {
    const result = await approveCategory(id, { 
      approved,
      rejectionReason: approved ? undefined : 'Content does not meet guidelines'
    });
    if (result) {
      loadCategories();
    }
  };

  const getStatusColor = (status: CategoryStatus) => {
    switch (status) {
      case CategoryStatus.APPROVED: return 'green';
      case CategoryStatus.PENDING_APPROVAL: return 'orange';
      case CategoryStatus.REJECTED: return 'red';
      case CategoryStatus.DRAFT: return 'gray';
      default: return 'gray';
    }
  };

  return (
    <div className="category-manager">
      <h2>{mode === 'admin' ? 'All Categories' : 'My Categories'}</h2>
      
      <button onClick={() => setShowCreateModal(true)}>
        + Create Category
      </button>

      {loading && <div>Loading...</div>}
      {error && <div style={{ color: 'red' }}>{error}</div>}

      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Status</th>
            <th>Visibility</th>
            <th>Content Count</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {categories.map(cat => (
            <tr key={cat.id}>
              <td>{cat.name}</td>
              <td>{cat.type}</td>
              <td style={{ color: getStatusColor(cat.status) }}>
                {cat.status}
              </td>
              <td>{cat.visibility}</td>
              <td>{/* content count */}</td>
              <td>
                <button onClick={() => setEditingCategory(cat)}>Edit</button>
                <button onClick={() => deleteCategory(cat.id)}>Delete</button>
                
                {/* Admin approval buttons */}
                {mode === 'admin' && cat.status === CategoryStatus.PENDING_APPROVAL && (
                  <>
                    <button onClick={() => handleApprove(cat.id, true)}>
                      Approve
                    </button>
                    <button onClick={() => handleApprove(cat.id, false)}>
                      Reject
                    </button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Create/Edit Modals would go here */}
    </div>
  );
};
```

### EventList.tsx (User View)
```typescript
import React, { useState, useEffect } from 'react';
import { useEvents } from '../hooks/useEvents';
import { Event, EventStatus } from '../types';

interface EventListProps {
  partnerId?: string;
}

export const EventList: React.FC<EventListProps> = ({ partnerId }) => {
  const { loading, error, getEventsByPartner } = useEvents();
  const [events, setEvents] = useState<Event[]>([]);

  useEffect(() => {
    loadEvents();
  }, [partnerId]);

  const loadEvents = async () => {
    const result = await getEventsByPartner(partnerId || 'partner-uuid-here');
    if (result) {
      setEvents(result.content);
    }
  };

  const getStatusBadge = (status: EventStatus) => {
    const colors: Record<EventStatus, string> = {
      [EventStatus.DRAFT]: '#999',
      [EventStatus.SCHEDULED]: '#007bff',
      [EventStatus.WAITING_ROOM]: '#ffc107',
      [EventStatus.LIVE]: '#dc3545',
      [EventStatus.FINISHED]: '#28a745',
      [EventStatus.CANCELLED]: '#6c757d'
    };
    return (
      <span style={{ 
        background: colors[status], 
        color: 'white',
        padding: '4px 8px',
        borderRadius: '4px',
        fontSize: '12px'
      }}>
        {status}
      </span>
    );
  };

  return (
    <div className="event-list">
      <h2>Available Events</h2>
      
      {loading && <div>Loading events...</div>}
      {error && <div style={{ color: 'red' }}>{error}</div>}

      <div className="events-grid">
        {events.map(event => (
          <div key={event.id} className="event-card" style={{
            border: '1px solid #ddd',
            borderRadius: '8px',
            padding: '16px',
            marginBottom: '16px'
          }}>
            <img 
              src={event.coverImage || '/default-event.jpg'} 
              alt={event.title}
              style={{ width: '100%', height: '150px', objectFit: 'cover', borderRadius: '4px' }}
            />
            
            <h3>{event.title}</h3>
            <p>{event.description}</p>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
              {getStatusBadge(event.status as EventStatus)}
              <span>{event.eventType}</span>
            </div>
            
            <div style={{ fontSize: '14px', color: '#666', marginBottom: '12px' }}>
              <div>📅 {new Date(event.scheduledAt || '').toLocaleString()}</div>
              <div>👥 {event.currentParticipants}/{event.maxParticipants || '∞'} participants</div>
              <div>❓ {event.totalQuestions} questions</div>
            </div>
            
            <button 
              disabled={event.status === EventStatus.LIVE || event.status === EventStatus.CANCELLED}
              onClick={() => {/* Navigate to event detail */}}
              style={{
                width: '100%',
                padding: '10px',
                background: event.status === EventStatus.LIVE ? '#dc3545' : '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              {event.status === EventStatus.LIVE ? '🔴 LIVE - Join Now!' : 
               event.status === EventStatus.SCHEDULED ? 'Join Event' : 
               event.status === EventStatus.FINISHED ? 'View Results' : 'Not Available'}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};
```

### EventPlayer.tsx (Quiz Interface)
```typescript
import React, { useState, useEffect, useCallback } from 'react';
import { useEvents } from '../hooks/useEvents';
import { CategoryContentResponse, AnswerResult, EventLeaderboard } from '../types';

interface EventPlayerProps {
  eventId: string;
}

export const EventPlayer: React.FC<EventPlayerProps> = ({ eventId }) => {
  const { 
    loading, 
    error, 
    joinEvent, 
    getNextQuestion, 
    submitAnswer, 
    getLeaderboard 
  } = useEvents();
  
  const [joined, setJoined] = useState(false);
  const [currentQuestion, setCurrentQuestion] = useState<CategoryContentResponse | null>(null);
  const [answerResult, setAnswerResult] = useState<AnswerResult | null>(null);
  const [leaderboard, setLeaderboard] = useState<EventLeaderboard | null>(null);
  const [isFinished, setIsFinished] = useState(false);
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [timeLeft, setTimeLeft] = useState<number>(0);

  // Initial join
  useEffect(() => {
    handleJoin();
  }, [eventId]);

  const handleJoin = async () => {
    const result = await joinEvent(eventId);
    if (result) {
      setJoined(true);
      if (result.currentQuestion) {
        setCurrentQuestion(result.currentQuestion);
        setTimeLeft(result.currentQuestion.timeLimit || 30);
      }
    }
  };

  // Timer countdown
  useEffect(() => {
    if (timeLeft > 0 && !answerResult) {
      const timer = setTimeout(() => setTimeLeft(t => t - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [timeLeft, answerResult]);

  const handleSubmit = async () => {
    if (!currentQuestion || !selectedAnswer) return;

    const result = await submitAnswer(eventId, {
      contentId: currentQuestion.id,
      questionIndex: currentQuestion.questionIndex,
      selectedAnswer,
      responseTimeMs: (currentQuestion.timeLimit || 30) - timeLeft
    });

    if (result) {
      setAnswerResult(result);
      
      if (result.nextQuestion) {
        // Show next question after delay
        setTimeout(() => {
          setCurrentQuestion(result.nextQuestion!);
          setTimeLeft(result.nextQuestion!.timeLimit || 30);
          setSelectedAnswer(null);
          setAnswerResult(null);
        }, 3000);
      } else {
        // No more questions - show final results
        setIsFinished(true);
        loadLeaderboard();
      }
    }
  };

  const loadLeaderboard = async () => {
    const result = await getLeaderboard(eventId);
    if (result) {
      setLeaderboard(result);
    }
  };

  if (!joined) {
    return <div>Joining event...</div>;
  }

  if (isFinished) {
    return (
      <div className="event-finished">
        <h2>Event Completed!</h2>
        <p>Final Score: {answerResult?.currentScore || 0}</p>
        
        {leaderboard && (
          <div className="leaderboard">
            <h3>Leaderboard</h3>
            {leaderboard.entries.map((entry, index) => (
              <div 
                key={entry.userId}
                style={{
                  padding: '12px',
                  background: entry.isCurrentUser ? '#e3f2fd' : 'white',
                  border: '1px solid #ddd',
                  marginBottom: '8px',
                  borderRadius: '4px',
                  display: 'flex',
                  justifyContent: 'space-between'
                }}
              >
                <span>#{index + 1} {entry.username} {entry.isFriend && '👥'}</span>
                <span>{entry.score} pts</span>
              </div>
            ))}
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="event-player">
      {/* Progress */}
      <div className="progress-bar" style={{ marginBottom: '20px' }}>
        <div style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>
          Question {currentQuestion?.questionIndex! + 1} of {currentQuestion?.totalQuestions}
        </div>
        <div style={{ 
          height: '8px', 
          background: '#e0e0e0', 
          borderRadius: '4px',
          overflow: 'hidden'
        }}>
          <div style={{
            height: '100%',
            width: `${((currentQuestion?.questionIndex! + 1) / (currentQuestion?.totalQuestions || 1)) * 100}%`,
            background: '#007bff',
            transition: 'width 0.3s'
          }}/>
        </div>
      </div>

      {/* Timer */}
      <div style={{ 
        textAlign: 'center', 
        fontSize: '24px', 
        fontWeight: 'bold',
        color: timeLeft < 5 ? '#dc3545' : '#333',
        marginBottom: '20px'
      }}>
        ⏱️ {timeLeft}s
      </div>

      {/* Question */}
      {currentQuestion && (
        <div className="question-card" style={{
          background: 'white',
          padding: '24px',
          borderRadius: '8px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
        }}>
          <h2 style={{ marginBottom: '16px' }}>{currentQuestion.title}</h2>
          {currentQuestion.description && (
            <p style={{ color: '#666', marginBottom: '24px' }}>{currentQuestion.description}</p>
          )}

          {/* Answer Options */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {currentQuestion.options?.map((option, index) => {
              const isSelected = selectedAnswer === option.value;
              const isCorrect = answerResult?.correctAnswer === option.value;
              const isWrong = isSelected && !answerResult?.isCorrect;

              return (
                <button
                  key={index}
                  disabled={!!answerResult}
                  onClick={() => setSelectedAnswer(option.value)}
                  style={{
                    padding: '16px',
                    textAlign: 'left',
                    border: '2px solid',
                    borderColor: isCorrect ? '#28a745' : isWrong ? '#dc3545' : isSelected ? '#007bff' : '#ddd',
                    background: isCorrect ? '#d4edda' : isWrong ? '#f8d7da' : isSelected ? '#e3f2fd' : 'white',
                    borderRadius: '8px',
                    cursor: answerResult ? 'default' : 'pointer',
                    transition: 'all 0.2s'
                  }}
                >
                  <span style={{ fontWeight: 'bold', marginRight: '12px' }}>
                    {String.fromCharCode(65 + index)}.
                  </span>
                  {option.label}
                </button>
              );
            })}
          </div>

          {/* Answer Result */}
          {answerResult && (
            <div style={{
              marginTop: '24px',
              padding: '16px',
              background: answerResult.isCorrect ? '#d4edda' : '#f8d7da',
              borderRadius: '8px',
              textAlign: 'center'
            }}>
              <h3 style={{ color: answerResult.isCorrect ? '#155724' : '#721c24' }}>
                {answerResult.isCorrect ? '✅ Correct!' : '❌ Wrong!'}
              </h3>
              <p>Points: +{answerResult.pointsEarned}</p>
              {answerResult.speedBonus > 0 && (
                <p>⚡ Speed Bonus: +{answerResult.speedBonus}</p>
              )}
              <p>Total Score: {answerResult.currentScore}</p>
            </div>
          )}

          {/* Submit Button */}
          {!answerResult && (
            <button
              onClick={handleSubmit}
              disabled={!selectedAnswer || loading}
              style={{
                width: '100%',
                marginTop: '24px',
                padding: '16px',
                background: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                fontSize: '18px',
                fontWeight: 'bold',
                cursor: 'pointer'
              }}
            >
              {loading ? 'Submitting...' : 'Submit Answer'}
            </button>
          )}
        </div>
      )}

      {error && <div style={{ color: 'red', marginTop: '16px' }}>{error}</div>}
    </div>
  );
};
```

---

## 6. Workflows Métiers

### Flow 1: Partner Crée une Catégorie
```
1. Partner: POST /api/categories/partner
   Body: { name, description, type, visibility, tags }
   
2. Response: Category with status=PENDING_APPROVAL

3. Admin: GET /api/categories/admin/all?status=PENDING_APPROVAL
   
4. Admin: PATCH /api/categories/admin/{id}/approve
   Body: { approved: true }
   
5. Category status devient APPROVED
```

### Flow 2: Partner Crée un Événement
```
1. Partner: GET /api/categories?status=APPROVED
   (récupère ses catégories approved + catégories publiques admin)
   
2. Partner: POST /api/events/partner
   Body: { 
     title, description, 
     categoryId,           // <- from step 1
     eventType: SIMPLE|LIVE,
     scheduledAt,
     maxParticipants 
   }
   
3. Response: Event with status=SCHEDULED
```

### Flow 3: User Participe à un Événement Simple
```
1. User: GET /api/events/partner/{partnerId}
   (liste les événements publics)
   
2. User: POST /api/events/{id}/join
   Response: { participantId, status=WAITING, currentQuestion }
   
3. Loop jusqu'à fin:
   a. User: GET /api/events/{id}/question
      Response: Question avec options
      
   b. User: POST /api/events/{id}/answer
      Body: { contentId, questionIndex, selectedAnswer, responseTimeMs }
      Response: { isCorrect, pointsEarned, nextQuestion }
      
4. Event fini: User: GET /api/events/{id}/leaderboard
```

### Flow 4: Live Event (Partner + WebSocket)
```
1. Partner: POST /api/events/partner (eventType=LIVE)

2. Partner: POST /api/events/partner/{id}/launch
   Event status passe à LIVE
   
3. Users connectent WebSocket: ws://localhost:8080/ws
   Subscribe: /topic/event/{id}/state
   
4. Partner contrôle via WebSocket:
   - Start question → broadcast /topic/event/{id}/question
   - End question → broadcast /topic/event/{id}/leaderboard
   - End event → broadcast /topic/event/{id}/finished
   
5. Users submit via WebSocket: /app/event/{id}/submit-answer
   Reçoivent résultat: /user/queue/answer-result
```

---

## 📡 API Reference Complète

### Categories
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/categories/admin` | ADMIN | Create public category (auto-approved) |
| GET | `/api/categories/admin/all` | ADMIN | List all categories with filters |
| PATCH | `/api/categories/admin/{id}/approve` | ADMIN | Approve/reject category |
| DELETE | `/api/categories/admin/{id}` | ADMIN | Delete any category |
| POST | `/api/categories/partner` | PARTNER | Create category (pending approval) |
| GET | `/api/categories/partner/mine` | PARTNER | List my categories |
| PATCH | `/api/categories/partner/{id}` | PARTNER | Update my category (DRAFT/PENDING only) |
| DELETE | `/api/categories/partner/{id}` | PARTNER | Delete my category |
| POST | `/api/categories/{id}/content` | ADMIN/PARTNER | Add question/quiz to category |
| PATCH | `/api/categories/content/{id}` | ADMIN/PARTNER | Update content |
| DELETE | `/api/categories/content/{id}` | ADMIN/PARTNER | Delete content |
| PATCH | `/api/categories/{id}/content/reorder` | ADMIN/PARTNER | Reorder questions |
| GET | `/api/categories?status=APPROVED` | ADMIN/PARTNER | Get approved categories (for event creation) |
| GET | `/api/categories/partner/{id}/public` | USER+ | Get partner's public categories |
| GET | `/api/categories/{id}` | USER+ | Get category with full content |

### Events
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/events/admin` | ADMIN | Create public event |
| GET | `/api/events/admin/all` | ADMIN | List all events |
| DELETE | `/api/events/admin/{id}` | ADMIN | Delete any event |
| PATCH | `/api/events/admin/{id}/cancel` | ADMIN | Cancel any event |
| POST | `/api/events/partner` | PARTNER | Create event |
| GET | `/api/events/partner/mine` | PARTNER | List my events |
| PATCH | `/api/events/partner/{id}` | PARTNER | Update my event |
| DELETE | `/api/events/partner/{id}` | PARTNER | Cancel my event |
| POST | `/api/events/partner/{id}/launch` | PARTNER | Launch live event |
| GET | `/api/events/partner/{id}` | USER+ | Get events by partner |
| POST | `/api/events/{id}/join` | USER+ | Join event |
| POST | `/api/events/{id}/leave` | USER+ | Leave event |
| GET | `/api/events/{id}/question` | USER+ | Get next question |
| POST | `/api/events/{id}/answer` | USER+ | Submit answer |
| GET | `/api/events/{id}/leaderboard` | USER+ | Get leaderboard |
| GET | `/api/events/{id}/state` | USER+ | Get live event state |

---

## ✅ Tests Recommandés

### Category Tests
1. Partner crée category → status=PENDING
2. Admin approuve → status=APPROVED
3. Partner crée event avec cette category
4. Partner update category (pendant PENDING) ✓
5. Partner update category (pendant APPROVED) ✗
6. Admin delete category → soft delete

### Event Tests
1. Partner crée event SIMPLE
2. User join → participe → termine
3. Partner crée event LIVE
4. Partner launch → users rejoignent WebSocket
5. Admin cancel event → users notifiés
6. Event full → user cannot join

### Permission Tests
| Action | User | Partner (own) | Partner (other) | Admin |
|--------|------|---------------|-----------------|-------|
| Create Category | ❌ | ✅ (pending) | ❌ | ✅ (approved) |
| Update Category | ❌ | ✅ | ❌ | ✅ |
| Delete Category | ❌ | ✅ | ❌ | ✅ |
| Create Event | ❌ | ✅ | ❌ | ✅ |
| Cancel Event | ❌ | ✅ | ❌ | ✅ |
| Join Event | ✅ | ✅ | ✅ | ✅ |

---

## 🚀 Prochaine Partie

**Partie 4 : Admin Panel & Partie 5 : WebSocket Live Events**

Dites-moi si cette partie est claire et je continue avec :
- Dashboard Admin complet
- WebSocket STOMP pour live events
- Real-time leaderboard updates

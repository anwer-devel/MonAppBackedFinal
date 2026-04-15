# Frontend Guide - Partie 1 : Système d'Authentification

## 🎯 Objectif
Implémenter le système d'authentification complet : Login, Register, Token Management, Auth Context.

---

## 📁 Structure des Fichiers à Créer

```
frontend/
├── src/
│   ├── api/
│   │   ├── client.ts           # Axios/Fetch client configuré
│   │   └── auth.ts             # API auth endpoints
│   ├── context/
│   │   └── AuthContext.tsx     # Global auth state
│   ├── hooks/
│   │   └── useAuth.ts          # Hook pour accès facile
│   ├── types/
│   │   └── auth.ts             # TypeScript interfaces
│   ├── utils/
│   │   └── tokenStorage.ts     # localStorage/sessionStorage
│   ├── components/
│   │   ├── auth/
│   │   │   ├── LoginForm.tsx
│   │   │   ├── RegisterForm.tsx
│   │   │   └── ProtectedRoute.tsx
│   │   └── common/
│   │       └── Loading.tsx
│   └── pages/
│       ├── Login.tsx
│       ├── Register.tsx
│       └── Dashboard.tsx
```

---

## 1. Types TypeScript (src/types/auth.ts)

```typescript
// User Role Enum
export enum UserRole {
  USER = 'USER',
  PARTNER = 'PARTNER',
  ADMIN = 'ADMIN'
}

// User DTO
export interface User {
  id: string;
  email: string;
  role: UserRole;
  emailVerified: boolean;
  username: string;
  avatarUrl?: string;
  title?: string;
  level: number;
  xp: number;
  nextLevelXp: number;
  streak: number;
  rank: number;
  eventsPlayed: number;
  teamPoints: number;
  couponsCount: number;
  isOnline: boolean;
}

// API Response Wrapper
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errors?: Record<string, string> | Array<{field: string, message: string}>;
  timestamp: string;
}

// Auth Response (Login/Register/Refresh)
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
  expiresIn: number;  // seconds
}

// Login Request
export interface LoginRequest {
  email: string;
  password: string;
}

// Register Request
export interface RegisterRequest {
  email: string;
  password: string;
  confirmPassword: string;
  username: string;
}

// Refresh Token Request
export interface RefreshTokenRequest {
  refreshToken: string;
}

// Auth Context State
export interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}
```

---

## 2. Token Storage (src/utils/tokenStorage.ts)

```typescript
const TOKEN_KEY = 'monapp_access_token';
const REFRESH_KEY = 'monapp_refresh_token';
const USER_KEY = 'monapp_user';

export const tokenStorage = {
  // Access Token
  setAccessToken: (token: string) => {
    localStorage.setItem(TOKEN_KEY, token);
  },
  getAccessToken: (): string | null => {
    return localStorage.getItem(TOKEN_KEY);
  },
  removeAccessToken: () => {
    localStorage.removeItem(TOKEN_KEY);
  },

  // Refresh Token
  setRefreshToken: (token: string) => {
    localStorage.setItem(REFRESH_KEY, token);
  },
  getRefreshToken: (): string | null => {
    return localStorage.getItem(REFRESH_KEY);
  },
  removeRefreshToken: () => {
    localStorage.removeItem(REFRESH_KEY);
  },

  // User
  setUser: (user: User) => {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  },
  getUser: (): User | null => {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  },
  removeUser: () => {
    localStorage.removeItem(USER_KEY);
  },

  // Clear All
  clearAll: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem(USER_KEY);
  }
};
```

---

## 3. API Client (src/api/client.ts)

```typescript
import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { tokenStorage } from '../utils/tokenStorage';
import { ApiResponse, AuthResponse } from '../types/auth';

const API_BASE_URL = 'http://localhost:8080/api';

// Create axios instance
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 10000
});

// Request Interceptor - Add Bearer Token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = tokenStorage.getAccessToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor - Handle Token Refresh
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // If 401 and not already retrying
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Queue the request while refreshing
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        }).catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = tokenStorage.getRefreshToken();
        if (!refreshToken) {
          throw new Error('No refresh token');
        }

        // Call refresh endpoint
        const response = await axios.post<ApiResponse<AuthResponse>>(
          `${API_BASE_URL}/auth/refresh`,
          { refreshToken }
        );

        if (response.data.success) {
          const { accessToken, refreshToken: newRefreshToken, user } = response.data.data;
          
          // Store new tokens
          tokenStorage.setAccessToken(accessToken);
          tokenStorage.setRefreshToken(newRefreshToken);
          tokenStorage.setUser(user);

          // Update header for original request
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          
          // Process queued requests
          processQueue(null, accessToken);
          
          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        processQueue(refreshError as Error, null);
        // Clear tokens and redirect to login
        tokenStorage.clearAll();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
```

---

## 4. Auth API (src/api/auth.ts)

```typescript
import apiClient from './client';
import { ApiResponse, AuthResponse, LoginRequest, RegisterRequest, RefreshTokenRequest, User } from '../types/auth';

export const authApi = {
  // POST /api/auth/login
  login: async (data: LoginRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', data);
    return response.data;
  },

  // POST /api/auth/register
  register: async (data: RegisterRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', data);
    return response.data;
  },

  // POST /api/auth/refresh
  refreshToken: async (data: RefreshTokenRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/refresh', data);
    return response.data;
  },

  // POST /api/auth/logout (Protected)
  logout: async (): Promise<ApiResponse<void>> => {
    const response = await apiClient.post<ApiResponse<void>>('/auth/logout');
    return response.data;
  },

  // GET /api/auth/me (Protected)
  getCurrentUser: async (): Promise<ApiResponse<User>> => {
    const response = await apiClient.get<ApiResponse<User>>('/auth/me');
    return response.data;
  }
};
```

---

## 5. Auth Context (src/context/AuthContext.tsx)

```typescript
import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { authApi } from '../api/auth';
import { tokenStorage } from '../utils/tokenStorage';
import { AuthState, User, LoginRequest, RegisterRequest } from '../types/auth';

interface AuthContextType extends AuthState {
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
  updateUser: (user: User) => void;
}

const initialState: AuthState = {
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: true,
  error: null
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [state, setState] = useState<AuthState>(initialState);

  // Initialize auth state from storage on mount
  useEffect(() => {
    const initAuth = async () => {
      const accessToken = tokenStorage.getAccessToken();
      const refreshToken = tokenStorage.getRefreshToken();
      const savedUser = tokenStorage.getUser();

      if (accessToken && savedUser) {
        setState(prev => ({
          ...prev,
          accessToken,
          refreshToken,
          user: savedUser,
          isAuthenticated: true,
          isLoading: false
        }));

        // Validate token by fetching current user
        try {
          const response = await authApi.getCurrentUser();
          if (response.success) {
            tokenStorage.setUser(response.data);
            setState(prev => ({ ...prev, user: response.data }));
          }
        } catch (error) {
          // Token invalid, clear storage
          tokenStorage.clearAll();
          setState(initialState);
        }
      } else {
        setState(prev => ({ ...prev, isLoading: false }));
      }
    };

    initAuth();
  }, []);

  const login = useCallback(async (data: LoginRequest) => {
    setState(prev => ({ ...prev, isLoading: true, error: null }));
    
    try {
      const response = await authApi.login(data);
      
      if (response.success && response.data) {
        const { accessToken, refreshToken, user } = response.data;
        
        tokenStorage.setAccessToken(accessToken);
        tokenStorage.setRefreshToken(refreshToken);
        tokenStorage.setUser(user);
        
        setState({
          user,
          accessToken,
          refreshToken,
          isAuthenticated: true,
          isLoading: false,
          error: null
        });
      } else {
        throw new Error(response.message || 'Login failed');
      }
    } catch (error: any) {
      const message = error.response?.data?.message || error.message || 'Login failed';
      setState(prev => ({ ...prev, isLoading: false, error: message }));
      throw error;
    }
  }, []);

  const register = useCallback(async (data: RegisterRequest) => {
    setState(prev => ({ ...prev, isLoading: true, error: null }));
    
    try {
      const response = await authApi.register(data);
      
      if (response.success && response.data) {
        const { accessToken, refreshToken, user } = response.data;
        
        tokenStorage.setAccessToken(accessToken);
        tokenStorage.setRefreshToken(refreshToken);
        tokenStorage.setUser(user);
        
        setState({
          user,
          accessToken,
          refreshToken,
          isAuthenticated: true,
          isLoading: false,
          error: null
        });
      } else {
        throw new Error(response.message || 'Registration failed');
      }
    } catch (error: any) {
      const message = error.response?.data?.message || error.message || 'Registration failed';
      setState(prev => ({ ...prev, isLoading: false, error: message }));
      throw error;
    }
  }, []);

  const logout = useCallback(async () => {
    setState(prev => ({ ...prev, isLoading: true }));
    
    try {
      await authApi.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      tokenStorage.clearAll();
      setState({ ...initialState, isLoading: false });
    }
  }, []);

  const clearError = useCallback(() => {
    setState(prev => ({ ...prev, error: null }));
  }, []);

  const updateUser = useCallback((user: User) => {
    tokenStorage.setUser(user);
    setState(prev => ({ ...prev, user }));
  }, []);

  return (
    <AuthContext.Provider value={{
      ...state,
      login,
      register,
      logout,
      clearError,
      updateUser
    }}>
      {children}
    </AuthContext.Provider>
  );
};

// Custom hook to use auth context
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
```

---

## 6. Protected Route Component (src/components/auth/ProtectedRoute.tsx)

```typescript
import React from 'react';
import { Navigate, useLocation, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { UserRole } from '../../types/auth';

interface ProtectedRouteProps {
  children?: React.ReactNode;
  allowedRoles?: UserRole[];
  fallback?: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  allowedRoles,
  fallback
}) => {
  const { isAuthenticated, user, isLoading } = useAuth();
  const location = useLocation();

  // Show loading while checking auth
  if (isLoading) {
    return fallback || <div>Loading...</div>;
  }

  // Not authenticated -> redirect to login
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Check role permissions
  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  // Render children or outlet
  return children ? <>{children}</> : <Outlet />;
};

// Role-specific route helpers
export const AdminRoute: React.FC<{ children?: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>{children}</ProtectedRoute>
);

export const PartnerRoute: React.FC<{ children?: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute allowedRoles={[UserRole.PARTNER, UserRole.ADMIN]}>{children}</ProtectedRoute>
);

export const UserRoute: React.FC<{ children?: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute allowedRoles={[UserRole.USER, UserRole.PARTNER, UserRole.ADMIN]}>{children}</ProtectedRoute>
);
```

---

## 7. Login Form (src/components/auth/LoginForm.tsx)

```typescript
import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';

export const LoginForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login, isLoading, error, clearError } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = (location.state as any)?.from?.pathname || '/';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();
    
    try {
      await login({ email, password });
      navigate(from, { replace: true });
    } catch (error) {
      // Error is handled by auth context
    }
  };

  return (
    <form onSubmit={handleSubmit} className="login-form">
      <h2>Login</h2>
      
      {error && (
        <div className="error-message" style={{ color: 'red', marginBottom: '1rem' }}>
          {error}
        </div>
      )}
      
      <div className="form-group">
        <label htmlFor="email">Email</label>
        <input
          type="email"
          id="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          disabled={isLoading}
        />
      </div>
      
      <div className="form-group">
        <label htmlFor="password">Password</label>
        <input
          type="password"
          id="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          minLength={8}
          disabled={isLoading}
        />
      </div>
      
      <button type="submit" disabled={isLoading}>
        {isLoading ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
};
```

---

## 8. Register Form (src/components/auth/RegisterForm.tsx)

```typescript
import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export const RegisterForm: React.FC = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    username: ''
  });
  const [validationError, setValidationError] = useState<string | null>(null);
  
  const { register, isLoading, error, clearError } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
    setValidationError(null);
    clearError();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();

    // Client validation
    if (formData.password !== formData.confirmPassword) {
      setValidationError('Passwords do not match');
      return;
    }

    if (formData.password.length < 8) {
      setValidationError('Password must be at least 8 characters');
      return;
    }

    try {
      await register({
        email: formData.email,
        password: formData.password,
        confirmPassword: formData.confirmPassword,
        username: formData.username
      });
      navigate('/');
    } catch (error) {
      // Error handled by context
    }
  };

  return (
    <form onSubmit={handleSubmit} className="register-form">
      <h2>Register</h2>
      
      {(error || validationError) && (
        <div className="error-message" style={{ color: 'red', marginBottom: '1rem' }}>
          {error || validationError}
        </div>
      )}
      
      <div className="form-group">
        <label htmlFor="username">Username</label>
        <input
          type="text"
          id="username"
          name="username"
          value={formData.username}
          onChange={handleChange}
          required
          minLength={2}
          maxLength={50}
          disabled={isLoading}
        />
      </div>
      
      <div className="form-group">
        <label htmlFor="email">Email</label>
        <input
          type="email"
          id="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          required
          disabled={isLoading}
        />
      </div>
      
      <div className="form-group">
        <label htmlFor="password">Password</label>
        <input
          type="password"
          id="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
          required
          minLength={8}
          disabled={isLoading}
        />
      </div>
      
      <div className="form-group">
        <label htmlFor="confirmPassword">Confirm Password</label>
        <input
          type="password"
          id="confirmPassword"
          name="confirmPassword"
          value={formData.confirmPassword}
          onChange={handleChange}
          required
          disabled={isLoading}
        />
      </div>
      
      <button type="submit" disabled={isLoading}>
        {isLoading ? 'Creating account...' : 'Register'}
      </button>
    </form>
  );
};
```

---

## 9. Router Configuration (App.tsx)

```typescript
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute, AdminRoute, PartnerRoute } from './components/auth/ProtectedRoute';
import { LoginForm } from './components/auth/LoginForm';
import { RegisterForm } from './components/auth/RegisterForm';

// Page components (to be implemented)
const HomePage = () => <div>Home Page</div>;
const DashboardPage = () => <div>User Dashboard</div>;
const PartnerDashboard = () => <div>Partner Dashboard</div>;
const AdminDashboard = () => <div>Admin Dashboard</div>;
const UnauthorizedPage = () => <div>Unauthorized</div>;

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginForm />} />
          <Route path="/register" element={<RegisterForm />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          {/* Protected User Routes */}
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            {/* Add more user routes here */}
          </Route>

          {/* Partner Routes */}
          <Route element={<PartnerRoute />}>
            <Route path="/partner" element={<PartnerDashboard />} />
          </Route>

          {/* Admin Routes */}
          <Route element={<AdminRoute />}>
            <Route path="/admin" element={<AdminDashboard />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
```

---

## 📡 Test Endpoints

| Action | Endpoint | Request | Response |
|--------|----------|---------|----------|
| **Login** | `POST /api/auth/login` | `{email, password}` | `{accessToken, refreshToken, user, expiresIn}` |
| **Register** | `POST /api/auth/register` | `{email, password, confirmPassword, username}` | `{accessToken, refreshToken, user, expiresIn}` |
| **Refresh** | `POST /api/auth/refresh` | `{refreshToken}` | `{accessToken, refreshToken, user, expiresIn}` |
| **Logout** | `POST /api/auth/logout` | Bearer token | `{success, message}` |
| **Get Me** | `GET /api/auth/me` | Bearer token | `User` object |

---

## ✅ Validation Checklist

Testez ces scénarios après implémentation :

1. **Login Success** → Token stored, user redirected
2. **Login Failure** → Error message displayed
3. **Register Success** → Account created, auto-login
4. **Password Mismatch** → Client-side validation
5. **Token Expiry** → Auto-refresh happens
6. **Refresh Failure** → Redirect to login
7. **Protected Route** → Redirects if not authenticated
8. **Role-based Access** → 403 for unauthorized roles
9. **Logout** → Tokens cleared, redirect to login
10. **Remember on Refresh** → User stays logged in after F5

---

## 🚀 Prochaine Partie

**Partie 2 : Event Management (User View)**
- List events
- Join event
- Answer questions
- View leaderboard

Dites-moi quand vous avez terminé cette partie et je vous donne la suite !

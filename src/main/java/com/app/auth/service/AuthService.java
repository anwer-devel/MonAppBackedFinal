package com.app.auth.service;

import com.app.auth.dto.*;
import java.util.UUID;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(UUID userId);
}


package com.app.config;

import com.app.auth.service.JwtService;
import com.app.auth.repository.UserRepository;
import com.app.auth.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

/**
 * WebSocket authentication with dual support:
 * 1. HTTP upgrade handshake (HandshakeInterceptor) - legacy support
 * 2. STOMP frame authentication (ChannelInterceptor) - Fix #3 SECURITY
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor, ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    // ========== HTTP Handshake Authentication ==========

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = httpRequest.getParameter("token");

            if (token != null && !token.isBlank()) {
                try {
                    java.util.UUID userUuid = jwtService.extractUserId(token);
                    if (userUuid != null && jwtService.isTokenValid(token)) {
                        String userId = userUuid.toString();
                        // Store authenticated principal in WebSocket session attributes
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userId, null,
                                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                );
                        attributes.put("user", auth);
                        attributes.put("userId", userId);
                        log.debug("WebSocket handshake authenticated for user: {}", userId);
                        return true;
                    }
                } catch (Exception e) {
                    log.warn("WebSocket handshake JWT validation failed: {}", e.getMessage());
                }
            }
        }

        // Allow anonymous connections (they won't be able to send STOMP messages to protected destinations)
        log.debug("WebSocket handshake without token — anonymous connection");
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // No-op
    }

    // ========== STOMP Frame Authentication (Fix #3) ==========

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        // ✅ Extract and validate JWT token on CONNECT (Fix #3)
        if (StompCommand.CONNECT == accessor.getCommand()) {
            String token = getTokenFromHeaders(accessor);

            if (token == null || token.isEmpty()) {
                log.warn("WebSocket CONNECT: no authentication token provided");
                throw new RuntimeException("Authentication token required for WebSocket connection");
            }

            try {
                // Validate JWT
                String username = jwtService.extractEmail(token);
                User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

                // Store userId in session for later reference
                assert accessor.getSessionAttributes() != null;
                accessor.getSessionAttributes().put("userId", user.getId().toString());
                accessor.getSessionAttributes().put("userEmail", user.getEmail());
                accessor.getSessionAttributes().put("userRole", user.getRole().toString());

                log.info("WebSocket CONNECT authenticated: user={}, role={}", user.getId(), user.getRole());

            } catch (Exception e) {
                log.warn("WebSocket CONNECT authentication failed: {}", e.getMessage());
                throw new RuntimeException("Authentication failed: " + e.getMessage());
            }
        }

        return message;
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getTokenFromHeaders(StompHeaderAccessor accessor) {
        List<String> authorization = accessor.getNativeHeader("Authorization");

        if (authorization != null && !authorization.isEmpty()) {
            String authHeader = authorization.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }

        return null;
    }
}

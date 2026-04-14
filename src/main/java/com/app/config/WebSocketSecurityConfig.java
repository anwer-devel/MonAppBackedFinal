package com.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP message-level configuration.
 * - Enables WebSocket message broker for real-time communication
 * - Configures STOMP endpoints and message broker
 * - CONNECT requires authentication (JWT validated at handshake)
 * - SUBSCRIBE/SEND to /app/** and /topic/** handled by Spring Security
 * - DISCONNECT is always allowed
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable in-memory message broker for /topic and /queue destinations
        config.enableSimpleBroker("/topic", "/queue", "/user");
        // Set the prefix for messages bound for @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");
        // Enable user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Inbound channel is configured through Spring Security and interceptors if needed
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Outbound channel is configured through Spring Security and interceptors if needed
    }
}

package com.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple broker for /topic (broadcast) and /queue (private)
        config.enableSimpleBroker("/topic", "/queue")
            .setHeartbeatValue(new long[]{60000, 60000});  // ✅ Fix #11: 60 seconds heartbeat

        // Client sends to /app/...
        config.setApplicationDestinationPrefixes("/app");
        // User-specific messages via /user/{userId}/queue/...
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Quiz battle WebSocket endpoint
        registry.addEndpoint("/ws/quiz")
                .setAllowedOrigins("*")
                .addInterceptors(webSocketAuthInterceptor)
                .withSockJS();

        // Room WebSocket endpoint
        registry.addEndpoint("/ws/room")
                .setAllowedOrigins("*")
                .addInterceptors(webSocketAuthInterceptor)
                .withSockJS();

        // Battle WebSocket endpoint
        registry.addEndpoint("/ws/battle")
                .setAllowedOrigins("*")
                .addInterceptors(webSocketAuthInterceptor)
                .withSockJS();

        // Legacy events endpoint (backward compatibility)
        registry.addEndpoint("/ws/events")
                .setAllowedOrigins("*")
                .addInterceptors(webSocketAuthInterceptor)
                .withSockJS();
    }
}

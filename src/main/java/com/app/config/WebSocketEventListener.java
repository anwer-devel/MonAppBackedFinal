package com.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Handles WebSocket lifecycle events: connect and disconnect.
 * On disconnect:
 * - Marks GamePlayer.isConnected = false
 * - Stores disconnect timestamp in Redis with 60s TTL (reconnect window)
 * - Removes from room:players Redis set
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplateObject;

    // Track session → userId mapping for disconnect handling
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    // Track session → active room/battle for cleanup
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();
    private final Map<String, String> sessionBattleMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs != null && attrs.containsKey("userId")) {
            String userId = (String) attrs.get("userId");
            sessionUserMap.put(sessionId, userId);
            log.info("WebSocket connected: session={}, user={}", sessionId, userId);
        } else {
            log.info("WebSocket connected: session={} (anonymous)", sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        String userId = sessionUserMap.remove(sessionId);
        if (userId == null) {
            log.debug("WebSocket disconnected: session={} (no user mapped)", sessionId);
            return;
        }

        log.info("WebSocket disconnected: session={}, user={}", sessionId, userId);

        // Handle room disconnect
        String roomId = sessionRoomMap.remove(sessionId);
        if (roomId != null) {
            handleRoomDisconnect(userId, roomId);
        }

        // Handle battle disconnect — store in Redis with 60s TTL for reconnect window
        String battleId = sessionBattleMap.remove(sessionId);
        if (battleId != null) {
            handleBattleDisconnect(userId, battleId);
        }
    }

    /**
     * Register a session as being in a specific room (called by RoomWebSocketController)
     */
    public void registerSessionRoom(String sessionId, String roomId) {
        sessionRoomMap.put(sessionId, roomId);
    }

    /**
     * Register a session as being in a specific battle
     */
    public void registerSessionBattle(String sessionId, String battleId) {
        sessionBattleMap.put(sessionId, battleId);
    }

    public String getUserIdForSession(String sessionId) {
        return sessionUserMap.get(sessionId);
    }

    private void handleRoomDisconnect(String userId, String roomId) {
        // Remove from Redis room:players set
        String roomPlayersKey = "room:" + roomId + ":players";
        redisTemplateObject.opsForSet().remove(roomPlayersKey, userId);

        // Notify other players
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                Map.of("type", "room.player_left", "userId", userId)
        );
        log.info("Player {} removed from room {} on disconnect", userId, roomId);
    }

    private void handleBattleDisconnect(String userId, String battleId) {
        // Store disconnect timestamp in Redis with 60s TTL → allows reconnect
        String disconnectKey = "battle:" + battleId + ":disconnected:" + userId;
        redisTemplateObject.opsForValue().set(disconnectKey, System.currentTimeMillis(), 60, TimeUnit.SECONDS);

        // Notify other players that this player disconnected
        messagingTemplate.convertAndSend(
                "/topic/battle/" + battleId,
                Map.of("type", "player.disconnected", "userId", userId)
        );
        log.info("Player {} disconnected from battle {} — 60s reconnect window started", userId, battleId);
    }
}

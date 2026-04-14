package com.app.event.consumer;

import com.app.event.dto.NotificationDto;
import com.app.event.entity.Notification;
import com.app.event.message.*;
import com.app.event.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.app.event.config.EventRabbitMQConfig.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventEventConsumer {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = EVENT_NOTIFICATIONS_QUEUE)
    public void handleEventLiveStarted(EventLiveStartedMessage message) {
        log.info("Live event started: {} with {} participants", message.getEventId(), message.getParticipantCount());

        // Send push notification to all waiting participants
        Notification notification = Notification.builder()
                .userId(null) // Will be set per participant
                .type(Notification.NotificationType.EVENT_STARTED)
                .title("Ton event commence!")
                .body("L'event live vient de démarrer!")
                .data(createEventData(message.getEventId()))
                .build();

        // In real implementation, iterate over participants and send to each
        messagingTemplate.convertAndSend("/topic/event/" + message.getEventId() + "/notifications",
                "Event started!");
    }

    @RabbitListener(queues = EVENT_NOTIFICATIONS_QUEUE)
    public void handleScoreUpdated(ScoreUpdatedMessage message) {
        log.info("Score updated for user {}: new score = {}, delta = {}",
                message.getUserId(), message.getNewScore(), message.getDelta());

        // Invalidate leaderboard cache
        invalidateLeaderboardCache(message.getPartnerId());
        invalidateLeaderboardCache(null); // Global leaderboard
    }

    @RabbitListener(queues = EVENT_ANALYTICS_QUEUE)
    public void handleEventSimpleCompleted(EventSimpleCompletedMessage message) {
        log.info("Simple event completed: user {} scored {} (rank {})",
                message.getUserId(), message.getScore(), message.getRank());

        // Store analytics data for future processing
        // This could update achievements, statistics, etc.
    }

    @RabbitListener(queues = EVENT_NOTIFICATIONS_QUEUE)
    public void handleFriendshipRequested(FriendshipRequestedMessage message) {
        log.info("Friendship requested from {} to {}", message.getRequesterId(), message.getAddresseeId());

        // Create notification for addressee
        Notification notification = Notification.builder()
                .userId(message.getAddresseeId())
                .type(Notification.NotificationType.FRIEND_REQUEST)
                .title("Nouvelle demande d'ami")
                .body("Vous avez reçu une demande d'ami")
                .data(createFriendshipData(message.getRequesterId(), message.getEventId()))
                .build();

        notificationRepository.save(notification);

        // Send WebSocket notification
        messagingTemplate.convertAndSendToUser(
                message.getAddresseeId().toString(),
                "/queue/notifications",
                mapToDto(notification));
    }

    @RabbitListener(queues = EVENT_NOTIFICATIONS_QUEUE)
    public void handleFriendshipAccepted(FriendshipAcceptedMessage message) {
        log.info("Friendship accepted: {} and {}", message.getRequesterId(), message.getAddresseeId());

        // Create notification for requester
        Notification notification = Notification.builder()
                .userId(message.getRequesterId())
                .type(Notification.NotificationType.FRIEND_ACCEPTED)
                .title("Demande d'ami acceptée")
                .body("Votre demande d'ami a été acceptée!")
                .data(createFriendshipData(message.getAddresseeId(), null))
                .build();

        notificationRepository.save(notification);

        // Send WebSocket notification
        messagingTemplate.convertAndSendToUser(
                message.getRequesterId().toString(),
                "/queue/notifications",
                mapToDto(notification));
    }

    // ===== HELPER METHODS =====

    private Map<String, Object> createEventData(UUID eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId.toString());
        data.put("type", "LIVE_STARTED");
        return data;
    }

    private Map<String, Object> createFriendshipData(UUID userId, UUID eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        if (eventId != null) {
            data.put("eventId", eventId.toString());
        }
        return data;
    }

    private void invalidateLeaderboardCache(UUID partnerId) {
        String pattern = partnerId != null
                ? "leaderboards:partner:" + partnerId + ":*"
                : "leaderboards:global:*";

        // Note: In production, use Redis SCAN or a cache manager
        // This is a simplified version
        log.info("Invalidating cache for pattern: {}", pattern);
    }

    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .data(notification.getData())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

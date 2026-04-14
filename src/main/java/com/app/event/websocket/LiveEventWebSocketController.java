package com.app.event.websocket;

import com.app.event.dto.AnswerResultDto;
import com.app.event.dto.LiveEventStateDto;
import com.app.event.dto.SubmitAnswerDto;
import com.app.event.service.LiveEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LiveEventWebSocketController {

    private final LiveEventService liveEventService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/event/{eventId}/join-live")
    public void joinLiveEvent(@DestinationVariable UUID eventId, Principal principal) {
        UUID userId = extractUserId(principal);
        log.info("User {} joining live event {}", userId, eventId);

        // Return current state if event already LIVE
        LiveEventStateDto state = liveEventService.getCurrentState(eventId);
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/event-state", state);
    }

    @MessageMapping("/event/{eventId}/submit-answer")
    @SendToUser("/queue/answer-result")
    public AnswerResultDto submitLiveAnswer(
            @DestinationVariable UUID eventId,
            @Payload SubmitAnswerDto dto,
            Principal principal) {
        UUID userId = extractUserId(principal);
        log.info("User {} submitting answer for event {}", userId, eventId);
        return liveEventService.submitLiveAnswer(eventId, dto, userId);
    }

    @MessageMapping("/event/{eventId}/leave-live")
    public void leaveLiveEvent(@DestinationVariable UUID eventId, Principal principal) {
        UUID userId = extractUserId(principal);
        log.info("User {} leaving live event {}", userId, eventId);
        // Participant status will be updated via REST API
    }

    private UUID extractUserId(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return UUID.fromString(principal.getName());
    }
}

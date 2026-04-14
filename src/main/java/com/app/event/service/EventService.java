package com.app.event.service;

import com.app.event.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {

    // ===== ADMIN OPERATIONS =====
    EventDto createPublicEvent(CreateEventDto dto, UUID adminId);
    Page<EventDto> getAllEvents(FilterEventDto filter);
    void cancelEvent(UUID eventId);
    void deleteEvent(UUID eventId);

    // ===== PARTNER OPERATIONS =====
    EventDto createPartnerEvent(CreateEventDto dto, UUID partnerId);
    Page<EventDto> getMyEvents(UUID partnerId, FilterEventDto filter);
    EventDto updateMyEvent(UUID eventId, UUID partnerId, UpdateEventDto dto);
    EventDto launchLiveEvent(UUID eventId, UUID partnerId);
    void cancelMyEvent(UUID eventId, UUID partnerId);

    // ===== USER OPERATIONS =====
    Page<EventDto> getEventsByPartner(UUID partnerId, FilterEventDto filter);
    JoinEventResponseDto joinEvent(UUID eventId, UUID userId);
    void leaveEvent(UUID eventId, UUID userId);

    // ===== DISCOVERY OPERATIONS =====
    Page<EventDto> getTrendingEvents(Pageable pageable);

    // ===== SIMPLE EVENT FLOW =====
    CategoryContentResponseDto getNextQuestion(UUID eventId, UUID userId);
    AnswerResultDto submitAnswer(UUID eventId, SubmitAnswerDto dto, UUID userId);
    EventSummaryDto completeSimpleEvent(UUID participantId);

    // ===== SCORING & LEADERBOARDS =====
    void updateUserScore(UUID userId, UUID partnerId, Integer points, Integer correctAnswers, Integer wrongAnswers);
    Page<UserScoreDto> getPartnerLeaderboard(UUID partnerId, Pageable pageable);
    Page<UserScoreDto> getGlobalLeaderboard(Pageable pageable);
    EventLeaderboardDto getEventLeaderboard(UUID eventId, UUID currentUserId);
    UserScoreDto getUserScoreSummary(UUID userId);
}

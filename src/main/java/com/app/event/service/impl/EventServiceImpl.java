package com.app.event.service.impl;

import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.category.entity.Category;
import com.app.category.entity.CategoryContent;
import com.app.category.repository.CategoryContentRepository;
import com.app.category.repository.CategoryRepository;
import com.app.common.exception.BadRequestException;
import com.app.common.exception.ForbiddenException;
import com.app.common.exception.ResourceNotFoundException;
import com.app.event.dto.*;
import com.app.event.entity.Event;
import com.app.event.entity.EventAnswer;
import com.app.event.entity.EventParticipant;
import com.app.event.entity.UserScore;
import com.app.event.message.*;
import com.app.event.repository.*;
import com.app.event.service.EventService;
import com.app.friendship.entity.Friendship;
import com.app.partner.entity.Partner;
import com.app.partner.repository.PartnerRepository;
import com.app.friendship.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.app.event.config.EventRabbitMQConfig.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final EventAnswerRepository answerRepository;
    private final UserScoreRepository userScoreRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryContentRepository contentRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final PartnerRepository partnerRepository;
    private final RabbitTemplate rabbitTemplate;

    // ===== ADMIN OPERATIONS =====

    @Override
    @Transactional
    public EventDto createPublicEvent(CreateEventDto dto, UUID adminId) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getStatus() != Category.CategoryStatus.APPROVED) {
            throw new BadRequestException("Category must be APPROVED to create an event");
        }
        if (category.getVisibility() != Category.CategoryVisibility.PUBLIC) {
            throw new BadRequestException("Only PUBLIC categories can be used for public events");
        }

        int totalQuestions = countContentsByCategoryId(category.getId());

        Event event = Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .coverImage(dto.getCoverImage())
                .eventType(dto.getEventType())
                .status(Event.EventStatus.SCHEDULED)
                .categoryId(category.getId())
                .partnerId(null)
                .createdBy(Event.CreatedByRole.ADMIN)
                .visibility(dto.getVisibility())
                .scheduledAt(dto.getScheduledAt())
                .maxParticipants(dto.getMaxParticipants())
                .questionTimeLimit(dto.getQuestionTimeLimit() != null ? dto.getQuestionTimeLimit() : 30)
                .totalQuestions(totalQuestions)
                .metadata(dto.getMetadata())
                .build();

        Event savedEvent = eventRepository.save(event);
        publishEvent(EVENT_CREATED, new EventCreatedMessage(savedEvent.getId(), null, savedEvent.getEventType(), savedEvent.getCategoryId()));

        return mapToEventDto(savedEvent, category, null);
    }

    @Override
    public Page<EventDto> getAllEvents(FilterEventDto filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getLimit());
        Page<Event> events = eventRepository.findAll(pageable);
        return events.map(event -> mapToEventDto(event, null, null));
    }

    @Override
    @Transactional
    @CacheEvict(value = "events", key = "#eventId")
    public void cancelEvent(UUID eventId) {
        Event event = eventRepository.findByIdAndIsActiveTrue(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        event.setStatus(Event.EventStatus.CANCELLED);
        event.setIsActive(false);
        eventRepository.save(event);
        publishEvent(EVENT_CANCELLED, new EventCancelledMessage(eventId, event.getPartnerId()));
    }

    @Override
    @Transactional
    public void deleteEvent(UUID eventId) {
        Event event = eventRepository.findByIdAndIsActiveTrue(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        event.setIsActive(false);
        eventRepository.save(event);
    }

    // ===== PARTNER OPERATIONS =====

    @Override
    @Transactional
    public EventDto createPartnerEvent(CreateEventDto dto, UUID ownerId) {
        // Find partner by ownerId (the user creating the event)
        Partner partner = partnerRepository.findByOwnerIdAndIsActiveTrue(ownerId)
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No partner found for this user. Please register as a partner first."));
        
        UUID partnerId = partner.getId();
        
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // FIX: category.getPartnerId() contient l'ID du propriétaire (utilisateur), pas l'ID du Partner (table partners)
        if (!category.getPartnerId().equals(ownerId)) {
            throw new ForbiddenException("Category does not belong to this partner");
        }
        if (category.getStatus() != Category.CategoryStatus.APPROVED) {
            throw new BadRequestException("Category must be APPROVED to create an event");
        }

        int totalQuestions = countContentsByCategoryId(category.getId());

        Event event = Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .coverImage(dto.getCoverImage())
                .eventType(dto.getEventType())
                .status(Event.EventStatus.SCHEDULED)
                .categoryId(category.getId())
                .partnerId(partnerId)
                .createdBy(Event.CreatedByRole.PARTNER)
                .visibility(dto.getVisibility())
                .scheduledAt(dto.getScheduledAt())
                .maxParticipants(dto.getMaxParticipants())
                .questionTimeLimit(dto.getQuestionTimeLimit() != null ? dto.getQuestionTimeLimit() : 30)
                .totalQuestions(totalQuestions)
                .metadata(dto.getMetadata())
                .build();

        Event savedEvent = eventRepository.save(event);
        publishEvent(EVENT_CREATED, new EventCreatedMessage(savedEvent.getId(), partnerId, savedEvent.getEventType(), savedEvent.getCategoryId()));

        return mapToEventDto(savedEvent, category, null);
    }

    @Override
    public Page<EventDto> getMyEvents(UUID ownerId, FilterEventDto filter) {
        // Find partner by ownerId
        Partner partner = partnerRepository.findByOwnerIdAndIsActiveTrue(ownerId)
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No partner found for this user"));
        
        UUID partnerId = partner.getId();
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getLimit());
        Page<Event> events = filter.getStatus() != null
                ? eventRepository.findByPartnerIdAndStatus(partnerId, filter.getStatus(), pageable)
                : eventRepository.findByPartnerId(partnerId, pageable);
        return events.map(event -> mapToEventDto(event, null, null));
    }

    @Override
    @Transactional
    public EventDto updateMyEvent(UUID eventId, UUID ownerId, UpdateEventDto dto) {
        // Find partner by ownerId
        Partner partner = partnerRepository.findByOwnerIdAndIsActiveTrue(ownerId)
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No partner found for this user"));
        
        UUID partnerId = partner.getId();
        
        Event event = eventRepository.findByIdAndIsActiveTrue(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (!partnerId.equals(event.getPartnerId())) {
            throw new ForbiddenException("Not authorized to update this event");
        }
        if (event.getStatus() != Event.EventStatus.DRAFT && event.getStatus() != Event.EventStatus.SCHEDULED) {
            throw new BadRequestException("Can only update events in DRAFT or SCHEDULED status");
        }

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getCoverImage() != null) event.setCoverImage(dto.getCoverImage());
        if (dto.getVisibility() != null) event.setVisibility(dto.getVisibility());
        if (dto.getScheduledAt() != null) event.setScheduledAt(dto.getScheduledAt());
        if (dto.getMaxParticipants() != null) event.setMaxParticipants(dto.getMaxParticipants());
        if (dto.getQuestionTimeLimit() != null) event.setQuestionTimeLimit(dto.getQuestionTimeLimit());
        if (dto.getMetadata() != null) event.setMetadata(dto.getMetadata());
        if (dto.getIsActive() != null) event.setIsActive(dto.getIsActive());

        return mapToEventDto(eventRepository.save(event), null, null);
    }

    @Override
    @Transactional
    public EventDto launchLiveEvent(UUID eventId, UUID ownerId) {
        // Find partner by ownerId
        Partner partner = partnerRepository.findByOwnerIdAndIsActiveTrue(ownerId)
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No partner found for this user"));
        
        UUID partnerId = partner.getId();
        
        Event event = eventRepository.findByIdAndIsActiveTrue(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (!partnerId.equals(event.getPartnerId())) {
            throw new ForbiddenException("Not authorized to launch this event");
        }
        if (event.getEventType() != Event.EventType.LIVE) {
            throw new BadRequestException("Can only launch LIVE events");
        }
        if (event.getStatus() != Event.EventStatus.SCHEDULED && event.getStatus() != Event.EventStatus.DRAFT) {
            throw new BadRequestException("Event must be in SCHEDULED or DRAFT status");
        }

        event.setStatus(Event.EventStatus.WAITING_ROOM);
        return mapToEventDto(eventRepository.save(event), null, null);
    }

    @Override
    @Transactional
    public void cancelMyEvent(UUID eventId, UUID ownerId) {
        // Find partner by ownerId
        Partner partner = partnerRepository.findByOwnerIdAndIsActiveTrue(ownerId)
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No partner found for this user"));
        
        UUID partnerId = partner.getId();
        
        Event event = eventRepository.findByIdAndIsActiveTrue(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (!partnerId.equals(event.getPartnerId())) {
            throw new ForbiddenException("Not authorized to cancel this event");
        }
        event.setStatus(Event.EventStatus.CANCELLED);
        event.setIsActive(false);
        eventRepository.save(event);
        publishEvent(EVENT_CANCELLED, new EventCancelledMessage(eventId, partnerId));
    }

    // ===== USER OPERATIONS =====

    @Override
    @Cacheable(value = "events", key = "#partnerId")
    public Page<EventDto> getEventsByPartner(UUID partnerId, FilterEventDto filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getLimit());
        List<Event> activeEvents = eventRepository.findActiveEventsByPartnerId(partnerId);
        List<EventDto> eventDtos = activeEvents.stream()
                .map(event -> mapToEventDto(event, null, null))
                .collect(Collectors.toList());
        return new PageImpl<>(eventDtos, pageable, eventDtos.size());
    }

    // ===== DISCOVERY OPERATIONS =====

    @Override
    @Cacheable(value = "trendingEvents")
    public Page<EventDto> getTrendingEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findPublicApprovedEvents(pageable);
        return events.map(event -> mapToEventDto(event, null, null));
    }

    @Override
    @Transactional
    public JoinEventResponseDto joinEvent(UUID eventId, UUID userId) {
        log.info("🎯 [EVENT] User {} joining event {}", userId, eventId);

        // FIX: Guard 1 - event existe et actif
        Event event = eventRepository.findByIdAndIsActiveTrue(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        // FIX: Guard 2 - event joinable (status check)
        List<Event.EventStatus> joinableStatuses = List.of(
                Event.EventStatus.SCHEDULED,
                Event.EventStatus.WAITING_ROOM,
                Event.EventStatus.LIVE
        );
        if (!joinableStatuses.contains(event.getStatus())) {
            throw new BadRequestException("Event cannot be joined. Status: " + event.getStatus());
        }

        // FIX: Guard 3 - pour LIVE events, vérifier le status spécifique
        if (event.getEventType() == Event.EventType.LIVE) {
            if (event.getStatus() != Event.EventStatus.WAITING_ROOM && event.getStatus() != Event.EventStatus.LIVE) {
                throw new BadRequestException("Live event is not open for joining. Status: " + event.getStatus());
            }
        }

        // FIX: Guard 4 - places disponibles
        if (event.getMaxParticipants() != null && event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new BadRequestException("Event is full (max: " + event.getMaxParticipants() + ")");
        }

        // Check if user already participated (for SIMPLE events)
        if (event.getEventType() == Event.EventType.SIMPLE) {
            Optional<EventParticipant> existing = participantRepository.findByEventIdAndUserId(eventId, userId);
            if (existing.isPresent() && existing.get().getStatus() == EventParticipant.ParticipantStatus.COMPLETED) {
                throw new BadRequestException("You have already completed this event");
            }
            if (existing.isPresent()) {
                log.info("User {} rejoined event {}", userId, eventId);
                return JoinEventResponseDto.builder()
                        .eventId(eventId)
                        .participantId(existing.get().getId())
                        .status(existing.get().getStatus())
                        .message("Already joined this event")
                        .build();
            }
        }

        EventParticipant participant = EventParticipant.builder()
                .eventId(eventId)
                .userId(userId)
                .status(event.getEventType() == Event.EventType.LIVE ? EventParticipant.ParticipantStatus.WAITING : EventParticipant.ParticipantStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .currentQuestionIndex(0)
                .score(0)
                .correctAnswers(0)
                .wrongAnswers(0)
                .isOnline(true)
                .build();

        EventParticipant saved = participantRepository.save(participant);
        eventRepository.incrementParticipantCount(eventId);

        return JoinEventResponseDto.builder()
                .eventId(eventId)
                .participantId(saved.getId())
                .status(saved.getStatus())
                .message("Successfully joined event")
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "events", key = "#eventId")
    public void leaveEvent(UUID eventId, UUID userId) {
        EventParticipant participant = participantRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        participant.setStatus(EventParticipant.ParticipantStatus.ABANDONED);
        participant.setIsOnline(false);
        participantRepository.save(participant);

        eventRepository.decrementParticipantCount(eventId);
    }

    // ===== SIMPLE EVENT FLOW =====

    @Override
    @Transactional(readOnly = true)
    public CategoryContentResponseDto getNextQuestion(UUID eventId, UUID userId) {
        log.debug("Getting next question for user {} in event {}", userId, eventId);

        EventParticipant participant = participantRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("You are not participating in this event"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        // FIX: Guard - vérifier que le participant est actif
        if (participant.getStatus() != EventParticipant.ParticipantStatus.ACTIVE) {
            throw new BadRequestException("Cannot get question. Participant status: " + participant.getStatus());
        }

        int currentIndex = participant.getCurrentQuestionIndex();

        // FIX: Guard - vérifier si toutes les questions sont répondues
        if (currentIndex >= event.getTotalQuestions()) {
            log.info("User {} completed all questions in event {}", userId, eventId);
            return null; // Event completed
        }

        List<CategoryContent> contents = contentRepository.findAllByCategoryId(event.getCategoryId());

        // FIX: Guard - vérifier si la catégorie a du contenu
        if (contents.isEmpty()) {
            throw new BadRequestException("This category has no active content");
        }

        if (currentIndex >= contents.size()) {
            log.info("User {} reached end of available content in event {}", userId, eventId);
            return null;
        }

        CategoryContent content = contents.get(currentIndex);
        log.debug("Returning question {} of {} for user {}", currentIndex, event.getTotalQuestions(), userId);
        return mapToContentResponseDto(content, currentIndex, event.getTotalQuestions());
    }

    @Override
    @Transactional
    public AnswerResultDto submitAnswer(UUID eventId, SubmitAnswerDto dto, UUID userId) {
        log.debug("User {} submitting answer for question {} in event {}", userId, dto.getQuestionIndex(), eventId);

        EventParticipant participant = participantRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        // FIX: Guard - vérifier que le participant est actif
        if (participant.getStatus() != EventParticipant.ParticipantStatus.ACTIVE) {
            throw new BadRequestException("Cannot submit answer. Participant status: " + participant.getStatus());
        }

        // FIX: Guard - vérifier que le questionIndex correspond au currentQuestionIndex du participant
        if (dto.getQuestionIndex() != participant.getCurrentQuestionIndex()) {
            throw new BadRequestException("Wrong question index. Expected: " + participant.getCurrentQuestionIndex());
        }

        // FIX: Guard - vérifier si déjà répondu à cette question (par index)
        if (answerRepository.existsByParticipantIdAndQuestionIndex(participant.getId(), dto.getQuestionIndex())) {
            throw new BadRequestException("Already answered question index: " + dto.getQuestionIndex());
        }

        CategoryContent content = contentRepository.findById(dto.getContentId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // FIX: Guard - calcul score sans NPE
        boolean isCorrect = content.getCorrectAnswer() != null &&
                content.getCorrectAnswer().trim().equalsIgnoreCase(
                        dto.getSelectedAnswer() != null ? dto.getSelectedAnswer().trim() : "");

        int pointsEarned = isCorrect ? (content.getPoints() != null ? content.getPoints() : 10) : 0;

        EventAnswer answer = EventAnswer.builder()
                .eventId(eventId)
                .participantId(participant.getId())
                .userId(userId)
                .contentId(dto.getContentId())
                .questionIndex(dto.getQuestionIndex())
                .selectedAnswer(dto.getSelectedAnswer())
                .isCorrect(isCorrect)
                .pointsEarned(pointsEarned)
                .responseTimeMs(dto.getResponseTimeMs())
                .answeredAt(LocalDateTime.now())
                .build();

        answerRepository.save(answer);

        // Update participant score and progress
        participant.setCurrentQuestionIndex(dto.getQuestionIndex() + 1);
        participant.setScore(participant.getScore() + pointsEarned);
        if (isCorrect) {
            participant.setCorrectAnswers(participant.getCorrectAnswers() + 1);
        } else {
            participant.setWrongAnswers(participant.getWrongAnswers() + 1);
        }
        participantRepository.save(participant);

        log.info("✅ [ANSWER] User {} Q{} correct={} points={} totalScore={}",
                userId, dto.getQuestionIndex(), isCorrect, pointsEarned, participant.getScore());

        // Check if event completed
        CategoryContentResponseDto nextQuestion = null;
        boolean isEventCompleted = dto.getQuestionIndex() + 1 >= event.getTotalQuestions();

        if (isEventCompleted) {
            log.info("🎯 [EVENT] User {} completed event {}", userId, eventId);
            completeSimpleEvent(participant.getId());
        } else {
            // Get next question preview (without correct answer)
            List<CategoryContent> contents = contentRepository.findAllByCategoryId(event.getCategoryId());
            if (dto.getQuestionIndex() + 1 < contents.size()) {
                nextQuestion = mapToContentResponseDto(contents.get(dto.getQuestionIndex() + 1), dto.getQuestionIndex() + 1, event.getTotalQuestions());
            }
        }

        return AnswerResultDto.builder()
                .isCorrect(isCorrect)
                .pointsEarned(pointsEarned)
                .speedBonus(0) // No speed bonus for simple events
                .correctAnswer(isCorrect ? null : content.getCorrectAnswer())
                .currentScore(participant.getScore())
                .nextQuestion(nextQuestion)
                .build();
    }

    @Override
    @Transactional
    public EventSummaryDto completeSimpleEvent(UUID participantId) {
        EventParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));
        Event event = eventRepository.findById(participant.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        participant.setStatus(EventParticipant.ParticipantStatus.COMPLETED);
        participant.setCompletedAt(LocalDateTime.now());
        participantRepository.save(participant);

        // Calculate rank
        participantRepository.updateRanksByEventId(event.getId());

        // Reload to get updated rank
        participant = participantRepository.findById(participantId).orElseThrow();

        // Update user score
        updateUserScore(participant.getUserId(), event.getPartnerId(), participant.getScore(),
                participant.getCorrectAnswers(), participant.getWrongAnswers());

        // Get user's global score
        UserScore globalScore = userScoreRepository.findByUserIdAndPartnerIdIsNull(participant.getUserId())
                .orElse(null);

        // Publish event
        publishEvent(EVENT_SIMPLE_COMPLETED, new EventSimpleCompletedMessage(
                event.getId(), participant.getUserId(), participant.getScore(), participant.getRank()));

        int totalParticipants = participantRepository.countByEventId(event.getId());

        return EventSummaryDto.builder()
                .eventId(event.getId())
                .userId(participant.getUserId())
                .score(participant.getScore())
                .rank(participant.getRank())
                .totalParticipants(totalParticipants)
                .correctAnswers(participant.getCorrectAnswers())
                .wrongAnswers(participant.getWrongAnswers())
                .pointsEarned(participant.getScore())
                .newTotalScore(globalScore != null ? globalScore.getTotalScore() : 0L)
                .build();
    }

    // ===== SCORING & LEADERBOARDS =====

    @Override
    @Transactional
    public void updateUserScore(UUID userId, UUID partnerId, Integer points, Integer correctAnswers, Integer wrongAnswers) {
        // Update partner-specific score if applicable
        if (partnerId != null) {
            Optional<UserScore> partnerScoreOpt = userScoreRepository.findByUserIdAndPartnerId(userId, partnerId);
            if (partnerScoreOpt.isPresent()) {
                UserScore ps = partnerScoreOpt.get();
                userScoreRepository.updateScore(ps.getId(), points, correctAnswers, wrongAnswers, LocalDateTime.now());
            } else {
                UserScore newPartnerScore = UserScore.builder()
                        .userId(userId)
                        .partnerId(partnerId)
                        .totalScore(points.longValue())
                        .totalEvents(1)
                        .totalCorrectAnswers(correctAnswers)
                        .totalWrongAnswers(wrongAnswers)
                        .lastUpdatedAt(LocalDateTime.now())
                        .build();
                userScoreRepository.save(newPartnerScore);
            }
        }

        // Update global score
        Optional<UserScore> globalScoreOpt = userScoreRepository.findByUserIdAndPartnerIdIsNull(userId);
        if (globalScoreOpt.isPresent()) {
            UserScore gs = globalScoreOpt.get();
            userScoreRepository.updateScore(gs.getId(), points, correctAnswers, wrongAnswers, LocalDateTime.now());
        } else {
            UserScore newGlobalScore = UserScore.builder()
                    .userId(userId)
                    .partnerId(null)
                    .totalScore(points.longValue())
                    .totalEvents(1)
                    .totalCorrectAnswers(correctAnswers)
                    .totalWrongAnswers(wrongAnswers)
                    .lastUpdatedAt(LocalDateTime.now())
                    .build();
            userScoreRepository.save(newGlobalScore);
        }

        // Publish score updated event
        UserScore updatedGlobal = userScoreRepository.findByUserIdAndPartnerIdIsNull(userId).orElseThrow();
        publishEvent(SCORE_UPDATED, new ScoreUpdatedMessage(userId, partnerId, updatedGlobal.getTotalScore(), points));
    }

    @Override
    @Cacheable(value = "leaderboards", key = "'partner:' + #partnerId + ':page:' + #pageable.pageNumber")
    public Page<UserScoreDto> getPartnerLeaderboard(UUID partnerId, Pageable pageable) {
        Page<UserScore> scores = userScoreRepository.findTopByPartnerIdOrderByTotalScoreDesc(partnerId, pageable);
        return scores.map(this::mapToUserScoreDto);
    }

    @Override
    @Cacheable(value = "leaderboards", key = "'global:page:' + #pageable.pageNumber")
    public Page<UserScoreDto> getGlobalLeaderboard(Pageable pageable) {
        Page<UserScore> scores = userScoreRepository.findTopByOrderByTotalScoreDesc(pageable);
        return scores.map(this::mapToUserScoreDto);
    }

    @Override
    public EventLeaderboardDto getEventLeaderboard(UUID eventId, UUID currentUserId) {
        List<EventParticipant> participants = participantRepository.findByEventIdOrderByScoreDesc(eventId);

        List<LeaderboardEntryDto> entries = participants.stream()
                .map(p -> mapToLeaderboardEntry(p, currentUserId))
                .collect(Collectors.toList());

        return EventLeaderboardDto.builder()
                .eventId(eventId)
                .entries(entries)
                .build();
    }

    @Override
    public UserScoreDto getUserScoreSummary(UUID userId) {
        User globalUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserScore globalScore = userScoreRepository.findByUserIdAndPartnerIdIsNull(userId)
                .orElse(UserScore.builder()
                        .userId(userId)
                        .totalScore(0L)
                        .totalEvents(0)
                        .build());

        return UserScoreDto.builder()
                .userId(userId)
                .username(globalUser.getUsername())
                .totalScore(globalScore.getTotalScore())
                .rank(globalScore.getRank())
                .totalEvents(globalScore.getTotalEvents())
                .build();
    }

    // ===== HELPER METHODS =====

    private int countContentsByCategoryId(UUID categoryId) {
        List<CategoryContent> contents = contentRepository.findAllByCategoryId(categoryId);
        return contents.size();
    }

    private void publishEvent(String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(EVENT_EXCHANGE, routingKey, message);
        } catch (Exception e) {
            log.error("Failed to publish event to {}: {}", routingKey, e.getMessage());
        }
    }

    private EventDto mapToEventDto(Event event, Category category, String partnerName) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .coverImage(event.getCoverImage())
                .eventType(event.getEventType() != null ? event.getEventType().name() : null)
                .status(event.getStatus() != null ? event.getStatus().name() : null)
                .categoryId(event.getCategoryId())
                .categoryName(category != null ? category.getName() : null)
                .partnerId(event.getPartnerId())
                .partnerName(partnerName)
                .createdBy(event.getCreatedBy() != null ? event.getCreatedBy().name() : null)
                .visibility(event.getVisibility() != null ? event.getVisibility().name() : null)
                .scheduledAt(event.getScheduledAt() != null ? event.getScheduledAt().toString() : null)
                .maxParticipants(event.getMaxParticipants())
                .currentParticipants(event.getCurrentParticipants())
                .totalQuestions(event.getTotalQuestions())
                .isActive(event.getIsActive())
                .createdAt(event.getCreatedAt() != null ? event.getCreatedAt().toString() : null)
                .build();
    }

    private CategoryContentResponseDto mapToContentResponseDto(CategoryContent content, Integer index, Integer total) {
        return CategoryContentResponseDto.builder()
                .id(content.getId())
                .categoryId(content.getCategoryId())
                .contentType(content.getContentType())
                .title(content.getTitle())
                .description(content.getDescription())
                .options(content.getOptions())
                .points(content.getPoints())
                .timeLimit(content.getTimeLimit())
                .difficulty(content.getDifficulty())
                .order(content.getOrder())
                .questionIndex(index)
                .totalQuestions(total)
                .build();
    }

    private UserScoreDto mapToUserScoreDto(UserScore score) {
        // FIX: Protection NPE - charger l'utilisateur avec gestion du cas null
        User user = null;
        if (score.getUserId() != null) {
            user = userRepository.findById(score.getUserId()).orElse(null);
        }
        return UserScoreDto.builder()
                .userId(score.getUserId())
                .username(user != null ? user.getUsername() : "Unknown")
                .totalScore(score.getTotalScore() != null ? score.getTotalScore() : 0L)
                .rank(score.getRank())
                .totalEvents(score.getTotalEvents() != null ? score.getTotalEvents() : 0)
                .build();
    }

    private LeaderboardEntryDto mapToLeaderboardEntry(EventParticipant participant, UUID currentUserId) {
        User user = userRepository.findById(participant.getUserId()).orElse(null);

        // Check if friend
        boolean isFriend = false;
        if (currentUserId != null && !currentUserId.equals(participant.getUserId())) {
            Optional<Friendship> friendship = friendshipRepository.findFriendshipBetween(currentUserId, participant.getUserId());
            isFriend = friendship.isPresent() && friendship.get().getStatus() == Friendship.FriendshipStatus.ACCEPTED;
        }

        return LeaderboardEntryDto.builder()
                .rank(participant.getRank())
                .userId(participant.getUserId())
                .username(user != null ? user.getUsername() : "Unknown")
                .avatar(user != null ? user.getAvatarUrl() : null)
                .score(participant.getScore())
                .correctAnswers(participant.getCorrectAnswers())
                .isFriend(isFriend)
                .isCurrentUser(currentUserId != null && currentUserId.equals(participant.getUserId()))
                .build();
    }
}

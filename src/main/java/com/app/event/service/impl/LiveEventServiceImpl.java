package com.app.event.service.impl;

import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.category.entity.CategoryContent;
import com.app.category.repository.CategoryContentRepository;
import com.app.common.exception.BadRequestException;
import com.app.common.exception.ResourceNotFoundException;
import com.app.event.dto.*;
import com.app.event.entity.Event;
import com.app.event.entity.EventAnswer;
import com.app.event.entity.EventParticipant;
import com.app.event.message.*;
import com.app.event.repository.*;
import com.app.event.service.LiveEventService;
import com.app.friendship.entity.Friendship;
import com.app.friendship.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.app.event.config.EventRabbitMQConfig.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LiveEventServiceImpl implements LiveEventService {

    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final EventAnswerRepository answerRepository;
    private final UserScoreRepository userScoreRepository;
    private final CategoryContentRepository contentRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EventServiceImpl eventService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @Override
    @Transactional
    public void startLiveEvent(UUID eventId) {
        Event event = eventRepository.findByIdAndIsActiveTrue(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getEventType() != Event.EventType.LIVE) {
            throw new BadRequestException("Can only start LIVE events");
        }

        event.setStatus(Event.EventStatus.LIVE);
        event.setStartedAt(LocalDateTime.now());
        eventRepository.save(event);

        // Get all waiting participants
        List<EventParticipant> participants = participantRepository.findByEventIdAndStatus(eventId, EventParticipant.ParticipantStatus.WAITING);
        for (EventParticipant p : participants) {
            p.setStatus(EventParticipant.ParticipantStatus.ACTIVE);
            participantRepository.save(p);
        }

        // Broadcast state change
        broadcastEventState(eventId);

        // Publish RabbitMQ event
        publishEvent(EVENT_LIVE_STARTED, new EventLiveStartedMessage(eventId, event.getPartnerId(), participants.size()));

        // Notify participants
        for (EventParticipant p : participants) {
            sendNotification(p.getUserId(), "EVENT_STARTED", "Ton event commence!", "L'event " + event.getTitle() + " vient de démarrer!");
        }

        // Send first question
        sendNextQuestion(eventId);
    }

    @Override
    @Transactional
    public void sendNextQuestion(UUID eventId) {
        Event event = eventRepository.findByIdAndIsActiveTrue(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getStatus() != Event.EventStatus.LIVE) {
            throw new BadRequestException("Event is not LIVE");
        }

        int currentIndex = event.getCurrentQuestionIndex();
        if (currentIndex >= event.getTotalQuestions()) {
            finishLiveEvent(eventId);
            return;
        }

        List<CategoryContent> contents = contentRepository.findAllByCategoryId(event.getCategoryId());
        if (currentIndex >= contents.size()) {
            finishLiveEvent(eventId);
            return;
        }

        CategoryContent content = contents.get(currentIndex);
        CategoryContentResponseDto questionDto = mapToContentResponseDto(content, currentIndex, event.getTotalQuestions());

        // Store question start time in Redis
        String redisKey = "event:" + eventId + ":question:" + currentIndex + ":start";
        redisTemplate.opsForValue().set(redisKey, System.currentTimeMillis(), event.getQuestionTimeLimit(), TimeUnit.SECONDS);

        // Broadcast question to all participants (without correct answer)
        messagingTemplate.convertAndSend("/topic/event/" + eventId + "/question", questionDto);

        // Update event state
        broadcastEventState(eventId);

        // Schedule auto-advance
        scheduler.schedule(() -> {
            try {
                advanceQuestion(eventId);
            } catch (Exception e) {
                log.error("Error auto-advancing question for event {}: {}", eventId, e.getMessage());
            }
        }, event.getQuestionTimeLimit(), TimeUnit.SECONDS);
    }

    @Override
    @Transactional
    public AnswerResultDto submitLiveAnswer(UUID eventId, SubmitAnswerDto dto, UUID userId) {
        Event event = eventRepository.findByIdAndIsActiveTrue(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getStatus() != Event.EventStatus.LIVE) {
            throw new BadRequestException("Event is not LIVE");
        }

        EventParticipant participant = participantRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        if (participant.getStatus() != EventParticipant.ParticipantStatus.ACTIVE) {
            throw new BadRequestException("Participant is not active");
        }

        // Check if already answered this question
        if (answerRepository.existsByParticipantIdAndContentId(participant.getId(), dto.getContentId())) {
            throw new BadRequestException("Already answered this question");
        }

        CategoryContent content = contentRepository.findById(dto.getContentId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // Calculate response time from Redis
        String redisKey = "event:" + eventId + ":question:" + dto.getQuestionIndex() + ":start";
        Long questionStartTime = (Long) redisTemplate.opsForValue().get(redisKey);
        long responseTimeMs = questionStartTime != null ? System.currentTimeMillis() - questionStartTime : 0;

        boolean isCorrect = content.getCorrectAnswer() != null &&
                content.getCorrectAnswer().equalsIgnoreCase(dto.getSelectedAnswer());

        int basePoints = isCorrect ? content.getPoints() : 0;

        // Calculate speed bonus for LIVE events (within 5 seconds = +50%)
        int speedBonus = 0;
        if (isCorrect && responseTimeMs < 5000) {
            speedBonus = basePoints / 2; // +50%
        }

        int totalPoints = basePoints + speedBonus;

        EventAnswer answer = EventAnswer.builder()
                .eventId(eventId)
                .participantId(participant.getId())
                .userId(userId)
                .contentId(dto.getContentId())
                .questionIndex(dto.getQuestionIndex())
                .selectedAnswer(dto.getSelectedAnswer())
                .isCorrect(isCorrect)
                .pointsEarned(totalPoints)
                .responseTimeMs(responseTimeMs)
                .speedBonus(speedBonus)
                .answeredAt(LocalDateTime.now())
                .build();

        answerRepository.save(answer);

        // Update participant score atomically
        participantRepository.updateScore(participant.getId(), totalPoints,
                isCorrect ? 1 : 0, isCorrect ? 0 : 1);

        // Reload participant to get updated score
        participant = participantRepository.findById(participant.getId()).orElseThrow();

        // Send result to user
        AnswerResultDto result = AnswerResultDto.builder()
                .isCorrect(isCorrect)
                .pointsEarned(basePoints)
                .speedBonus(speedBonus)
                .correctAnswer(content.getCorrectAnswer())
                .currentScore(participant.getScore())
                .build();

        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/answer-result", result);

        // Broadcast leaderboard update
        broadcastLeaderboard(eventId);

        return result;
    }

    @Override
    @Transactional
    public void advanceQuestion(UUID eventId) {
        Event event = eventRepository.findByIdAndIsActiveTrue(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        int nextIndex = event.getCurrentQuestionIndex() + 1;
        eventRepository.updateCurrentQuestionIndex(eventId, nextIndex);

        if (nextIndex >= event.getTotalQuestions()) {
            finishLiveEvent(eventId);
        } else {
            sendNextQuestion(eventId);
        }
    }

    @Override
    @Transactional
    public void finishLiveEvent(UUID eventId) {
        Event event = eventRepository.findByIdAndIsActiveTrue(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        event.setStatus(Event.EventStatus.FINISHED);
        event.setFinishedAt(LocalDateTime.now());
        eventRepository.save(event);

        // Calculate final ranks
        participantRepository.updateRanksByEventId(eventId);

        // Update scores for all participants
        List<EventParticipant> participants = participantRepository.findByEventIdOrderByScoreDesc(eventId);
        for (EventParticipant p : participants) {
            eventService.updateUserScore(p.getUserId(), event.getPartnerId(), p.getScore(),
                    p.getCorrectAnswers(), p.getWrongAnswers());
        }

        // Broadcast final results
        EventLeaderboardDto finalLeaderboard = getEventLeaderboard(eventId, null);
        messagingTemplate.convertAndSend("/topic/event/" + eventId + "/finished", finalLeaderboard);

        // Publish RabbitMQ event
        UUID winnerId = participants.isEmpty() ? null : participants.get(0).getUserId();
        publishEvent(EVENT_LIVE_FINISHED, new EventLiveFinishedMessage(eventId, winnerId, finalLeaderboard.getEntries()));

        // Clear Redis keys
        for (int i = 0; i < event.getTotalQuestions(); i++) {
            redisTemplate.delete("event:" + eventId + ":question:" + i + ":start");
        }
    }

    @Override
    public LiveEventStateDto getCurrentState(UUID eventId) {
        Event event = eventRepository.findByIdAndIsActiveTrue(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        CategoryContentResponseDto currentQuestion = null;
        Integer timeRemaining = null;

        if (event.getStatus() == Event.EventStatus.LIVE && event.getCurrentQuestionIndex() < event.getTotalQuestions()) {
            List<CategoryContent> contents = contentRepository.findAllByCategoryId(event.getCategoryId());
            if (event.getCurrentQuestionIndex() < contents.size()) {
                CategoryContent content = contents.get(event.getCurrentQuestionIndex());
                currentQuestion = mapToContentResponseDto(content, event.getCurrentQuestionIndex(), event.getTotalQuestions());

                // Calculate time remaining
                String redisKey = "event:" + eventId + ":question:" + event.getCurrentQuestionIndex() + ":start";
                Long questionStartTime = (Long) redisTemplate.opsForValue().get(redisKey);
                if (questionStartTime != null) {
                    long elapsedMs = System.currentTimeMillis() - questionStartTime;
                    timeRemaining = Math.max(0, event.getQuestionTimeLimit() - (int) (elapsedMs / 1000));
                }
            }
        }

        Integer participantCount = participantRepository.countByEventIdAndStatus(eventId, EventParticipant.ParticipantStatus.ACTIVE);
        List<LeaderboardEntryDto> top5 = getEventLeaderboard(eventId, null).getEntries().stream()
                .limit(5)
                .collect(Collectors.toList());

        return LiveEventStateDto.builder()
                .eventId(eventId)
                .status(event.getStatus())
                .currentQuestionIndex(event.getCurrentQuestionIndex())
                .totalQuestions(event.getTotalQuestions())
                .currentQuestion(currentQuestion)
                .timeRemaining(timeRemaining)
                .participantCount(participantCount)
                .leaderboard(top5)
                .build();
    }

    // ===== HELPER METHODS =====

    private void broadcastEventState(UUID eventId) {
        LiveEventStateDto state = getCurrentState(eventId);
        messagingTemplate.convertAndSend("/topic/event/" + eventId + "/state", state);
    }

    private void broadcastLeaderboard(UUID eventId) {
        EventLeaderboardDto leaderboard = getEventLeaderboard(eventId, null);
        // Send top 10
        List<LeaderboardEntryDto> top10 = leaderboard.getEntries().stream()
                .limit(10)
                .collect(Collectors.toList());
        EventLeaderboardDto broadcast = EventLeaderboardDto.builder()
                .eventId(eventId)
                .entries(top10)
                .build();
        messagingTemplate.convertAndSend("/topic/event/" + eventId + "/leaderboard", broadcast);
    }

    private EventLeaderboardDto getEventLeaderboard(UUID eventId, UUID currentUserId) {
        List<EventParticipant> participants = participantRepository.findByEventIdOrderByScoreDesc(eventId);

        List<LeaderboardEntryDto> entries = participants.stream()
                .map(p -> mapToLeaderboardEntry(p, currentUserId))
                .collect(Collectors.toList());

        return EventLeaderboardDto.builder()
                .eventId(eventId)
                .entries(entries)
                .build();
    }

    private void publishEvent(String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(EVENT_EXCHANGE, routingKey, message);
        } catch (Exception e) {
            log.error("Failed to publish event to {}: {}", routingKey, e.getMessage());
        }
    }

    @Async
    protected void sendNotification(UUID userId, String type, String title, String body) {
        // Implementation would send push notification
        log.info("Sending notification to user {}: {} - {}", userId, title, body);
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

    private LeaderboardEntryDto mapToLeaderboardEntry(EventParticipant participant, UUID currentUserId) {
        User user = userRepository.findById(participant.getUserId()).orElse(null);

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

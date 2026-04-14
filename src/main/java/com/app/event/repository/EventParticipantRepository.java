package com.app.event.repository;

import com.app.event.entity.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, UUID> {

    Optional<EventParticipant> findByEventIdAndUserId(UUID eventId, UUID userId);

    @Query("SELECT ep FROM EventParticipant ep WHERE ep.eventId = :eventId ORDER BY ep.score DESC")
    List<EventParticipant> findByEventIdOrderByScoreDesc(@Param("eventId") UUID eventId);

    List<EventParticipant> findByEventIdAndStatus(UUID eventId, EventParticipant.ParticipantStatus status);

    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    @Query("SELECT COUNT(ep) FROM EventParticipant ep WHERE ep.eventId = :eventId AND ep.status = :status")
    Integer countByEventIdAndStatus(@Param("eventId") UUID eventId, @Param("status") EventParticipant.ParticipantStatus status);

    @Query("SELECT COUNT(ep) FROM EventParticipant ep WHERE ep.eventId = :eventId")
    Integer countByEventId(@Param("eventId") UUID eventId);

    @Modifying
    @Query(value = """
        UPDATE event_participants 
        SET rank = subquery.rank 
        FROM (
            SELECT id, RANK() OVER(PARTITION BY event_id ORDER BY score DESC) as rank 
            FROM event_participants 
            WHERE event_id = :eventId
        ) subquery 
        WHERE event_participants.id = subquery.id
        """, nativeQuery = true)
    void updateRanksByEventId(@Param("eventId") UUID eventId);

    @Modifying
    @Query("UPDATE EventParticipant ep SET ep.isOnline = :isOnline WHERE ep.id = :participantId")
    void updateOnlineStatus(@Param("participantId") UUID participantId, @Param("isOnline") Boolean isOnline);

    @Modifying
    @Query("UPDATE EventParticipant ep SET ep.status = :status, ep.completedAt = :completedAt WHERE ep.id = :participantId")
    void completeParticipant(@Param("participantId") UUID participantId, @Param("status") EventParticipant.ParticipantStatus status, @Param("completedAt") LocalDateTime completedAt);

    @Modifying
    @Query("UPDATE EventParticipant ep SET ep.score = ep.score + :points, ep.correctAnswers = ep.correctAnswers + :correctIncrement, ep.wrongAnswers = ep.wrongAnswers + :wrongIncrement WHERE ep.id = :participantId")
    void updateScore(@Param("participantId") UUID participantId, @Param("points") Integer points, @Param("correctIncrement") Integer correctIncrement, @Param("wrongIncrement") Integer wrongIncrement);
}

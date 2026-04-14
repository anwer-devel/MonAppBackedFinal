package com.app.event.repository;

import com.app.event.entity.EventAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventAnswerRepository extends JpaRepository<EventAnswer, UUID> {

    Optional<EventAnswer> findByParticipantIdAndQuestionIndex(UUID participantId, Integer questionIndex);

    boolean existsByParticipantIdAndContentId(UUID participantId, UUID contentId);

    // FIX: Méthode ajoutée pour éviter les réponses dupliquées par index
    boolean existsByParticipantIdAndQuestionIndex(UUID participantId, Integer questionIndex);

    @Query("SELECT ea FROM EventAnswer ea WHERE ea.eventId = :eventId AND ea.userId = :userId")
    List<EventAnswer> findByEventIdAndUserId(@Param("eventId") UUID eventId, @Param("userId") UUID userId);

    @Query("SELECT ea FROM EventAnswer ea WHERE ea.participantId = :participantId ORDER BY ea.questionIndex")
    List<EventAnswer> findByParticipantIdOrderByQuestionIndex(@Param("participantId") UUID participantId);

    @Query("SELECT COUNT(ea) FROM EventAnswer ea WHERE ea.participantId = :participantId AND ea.isCorrect = true")
    Integer countCorrectAnswersByParticipantId(@Param("participantId") UUID participantId);

    @Query("SELECT SUM(ea.pointsEarned) FROM EventAnswer ea WHERE ea.participantId = :participantId")
    Integer sumPointsByParticipantId(@Param("participantId") UUID participantId);
}

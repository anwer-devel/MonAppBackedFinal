package com.app.event.repository;

import com.app.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findByPartnerId(UUID partnerId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.visibility = 'PUBLIC' AND e.status = 'SCHEDULED' AND e.isActive = true")
    Page<Event> findPublicApprovedEvents(Pageable pageable);

    Page<Event> findByPartnerIdAndStatus(UUID partnerId, Event.EventStatus status, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventType = 'LIVE' AND e.status = 'SCHEDULED' AND e.scheduledAt <= :now AND e.isActive = true")
    List<Event> findLiveEventsNeedingLaunch(@Param("now") LocalDateTime now);

    Optional<Event> findByIdAndIsActiveTrue(UUID id);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.partnerId = :partnerId AND e.status = :status")
    Integer countByPartnerIdAndStatus(@Param("partnerId") UUID partnerId, @Param("status") Event.EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.partnerId = :partnerId AND e.status IN ('SCHEDULED', 'LIVE') AND e.isActive = true")
    List<Event> findActiveEventsByPartnerId(@Param("partnerId") UUID partnerId);

    @Query("SELECT e FROM Event e WHERE e.partnerId = :partnerId AND e.eventType = 'SIMPLE' AND e.status = 'SCHEDULED' AND e.isActive = true")
    List<Event> findActiveSimpleEventsByPartnerId(@Param("partnerId") UUID partnerId);

    @Modifying
    @Query("UPDATE Event e SET e.currentParticipants = e.currentParticipants + 1 WHERE e.id = :eventId")
    void incrementParticipantCount(@Param("eventId") UUID eventId);

    @Modifying
    @Query("UPDATE Event e SET e.currentParticipants = e.currentParticipants - 1 WHERE e.id = :eventId AND e.currentParticipants > 0")
    void decrementParticipantCount(@Param("eventId") UUID eventId);

    @Modifying
    @Query("UPDATE Event e SET e.currentQuestionIndex = :index WHERE e.id = :eventId")
    void updateCurrentQuestionIndex(@Param("eventId") UUID eventId, @Param("index") Integer index);
}

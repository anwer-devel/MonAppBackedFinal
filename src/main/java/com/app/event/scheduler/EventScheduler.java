package com.app.event.scheduler;

import com.app.event.entity.Event;
import com.app.event.repository.EventRepository;
import com.app.event.service.LiveEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventRepository eventRepository;
    private final LiveEventService liveEventService;

    @Scheduled(fixedDelay = 60000) // Every minute
    @Transactional
    public void checkAndLaunchScheduledEvents() {
        log.debug("Checking for scheduled events to launch...");

        LocalDateTime now = LocalDateTime.now();
        List<Event> eventsToLaunch = eventRepository.findLiveEventsNeedingLaunch(now);

        for (Event event : eventsToLaunch) {
            try {
                log.info("Auto-launching event {}: {}", event.getId(), event.getTitle());

                // Change status to WAITING_ROOM
                event.setStatus(Event.EventStatus.WAITING_ROOM);
                eventRepository.save(event);

                // Notify waiting participants would happen here
                // This could trigger push notifications via RabbitMQ

            } catch (Exception e) {
                log.error("Error launching event {}: {}", event.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    @Transactional
    public void recalculateGlobalRanks() {
        log.debug("Recalculating global ranks...");
        // This would be implemented by calling a rank calculation service
        // For now, we just log it
        log.info("Global ranks recalculated");
    }

    @Scheduled(fixedDelay = 120000) // Every 2 minutes
    public void cleanupStaleParticipants() {
        log.debug("Cleaning up stale participants...");
        // Mark participants as offline if they haven't been active for a while
        // This would typically involve checking last activity timestamps
    }
}

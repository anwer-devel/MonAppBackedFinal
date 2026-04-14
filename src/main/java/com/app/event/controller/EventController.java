package com.app.event.controller;

import com.app.auth.repository.UserRepository;
import com.app.event.dto.*;
import com.app.event.entity.Event;
import com.app.event.service.EventService;
import com.app.event.service.LiveEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management endpoints")
public class EventController {

    private final EventService eventService;
    private final LiveEventService liveEventService;
    private final UserRepository userRepository;

    // ===== ADMIN ENDPOINTS =====

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a public event (admin only)")
    public ResponseEntity<EventDto> createPublicEvent(
            @Valid @RequestBody CreateEventDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID adminId = extractUserId(userDetails);
        return ResponseEntity.ok(eventService.createPublicEvent(dto, adminId));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all events (admin only)")
    public ResponseEntity<Page<EventDto>> getAllEvents(
            @RequestParam(required = false) Event.EventStatus status,
            Pageable pageable) {
        FilterEventDto filter = FilterEventDto.builder()
                .status(status)
                .page(pageable.getPageNumber())
                .limit(pageable.getPageSize())
                .build();
        return ResponseEntity.ok(eventService.getAllEvents(filter));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an event (admin only)")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel an event (admin only)")
    public ResponseEntity<Void> cancelEvent(@PathVariable UUID id) {
        eventService.cancelEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ===== PARTNER ENDPOINTS =====

    @PostMapping("/partner")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(summary = "Create a partner event")
    public ResponseEntity<EventDto> createPartnerEvent(
            @Valid @RequestBody CreateEventDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID partnerId = extractUserId(userDetails); // Or get from partner context
        return ResponseEntity.ok(eventService.createPartnerEvent(dto, partnerId));
    }

    @GetMapping("/partner/mine")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(summary = "Get my partner events")
    public ResponseEntity<Page<EventDto>> getMyEvents(
            @RequestParam(required = false) Event.EventStatus status,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID partnerId = extractUserId(userDetails);
        FilterEventDto filter = FilterEventDto.builder()
                .status(status)
                .page(pageable.getPageNumber())
                .limit(pageable.getPageSize())
                .build();
        return ResponseEntity.ok(eventService.getMyEvents(partnerId, filter));
    }

    @PatchMapping("/partner/{id}")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(summary = "Update my event")
    public ResponseEntity<EventDto> updateMyEvent(
            @PathVariable UUID id,
            @RequestBody UpdateEventDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID partnerId = extractUserId(userDetails);
        return ResponseEntity.ok(eventService.updateMyEvent(id, partnerId, dto));
    }

    @DeleteMapping("/partner/{id}")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(summary = "Cancel my event")
    public ResponseEntity<Void> cancelMyEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID partnerId = extractUserId(userDetails);
        eventService.cancelMyEvent(id, partnerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/partner/{id}/launch")
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    @Operation(summary = "Launch a live event")
    public ResponseEntity<EventDto> launchLiveEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID partnerId = extractUserId(userDetails);
        return ResponseEntity.ok(eventService.launchLiveEvent(id, partnerId));
    }

    // ===== USER ENDPOINTS =====

    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get events by partner")
    public ResponseEntity<Page<EventDto>> getEventsByPartner(
            @PathVariable UUID partnerId,
            Pageable pageable) {
        FilterEventDto filter = FilterEventDto.builder()
                .page(pageable.getPageNumber())
                .limit(pageable.getPageSize())
                .build();
        return ResponseEntity.ok(eventService.getEventsByPartner(partnerId, filter));
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER_OWNER')")
    @Operation(summary = "Join an event")
    public ResponseEntity<JoinEventResponseDto> joinEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(eventService.joinEvent(id, userId));
    }

    @PostMapping("/{id}/leave")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER_OWNER')")
    @Operation(summary = "Leave an event")
    public ResponseEntity<Void> leaveEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        eventService.leaveEvent(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/question")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER_OWNER')")
    @Operation(summary = "Get next question (simple events)")
    public ResponseEntity<CategoryContentResponseDto> getNextQuestion(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(eventService.getNextQuestion(id, userId));
    }

    @PostMapping("/{id}/answer")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER_OWNER')")
    @Operation(summary = "Submit answer (simple events)")
    public ResponseEntity<AnswerResultDto> submitAnswer(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitAnswerDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(eventService.submitAnswer(id, dto, userId));
    }

    @GetMapping("/{id}/leaderboard")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER_OWNER')")
    @Operation(summary = "Get event leaderboard")
    public ResponseEntity<EventLeaderboardDto> getEventLeaderboard(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(eventService.getEventLeaderboard(id, userId));
    }

    @GetMapping("/{id}/state")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER_OWNER')")
    @Operation(summary = "Get live event state (for reconnection)")
    public ResponseEntity<LiveEventStateDto> getCurrentState(
            @PathVariable UUID id) {
        return ResponseEntity.ok(liveEventService.getCurrentState(id));
    }

    private UUID extractUserId(UserDetails userDetails) {
        // UserDetails contains email, need to find user ID from repository
        return userRepository.findByEmail(userDetails.getUsername())
                .map(user -> user.getId())
                .orElseThrow(() -> new com.app.common.exception.ResourceNotFoundException(
                        "User not found: " + userDetails.getUsername()));
    }
}

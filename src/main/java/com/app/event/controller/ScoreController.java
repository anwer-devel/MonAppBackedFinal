package com.app.event.controller;

import com.app.auth.repository.UserRepository;
import com.app.common.exception.ResourceNotFoundException;
import com.app.event.dto.EventLeaderboardDto;
import com.app.event.dto.UserScoreDto;
import com.app.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/scores")
@RequiredArgsConstructor
@Tag(name = "Scores", description = "Score and leaderboard endpoints")
public class ScoreController {

    private final EventService eventService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER_OWNER')")
    @Operation(summary = "Get my score summary")
    public ResponseEntity<UserScoreDto> getMyScore(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(eventService.getUserScoreSummary(userId));
    }

    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER_OWNER')")
    @Operation(summary = "Get partner leaderboard")
    public ResponseEntity<Page<UserScoreDto>> getPartnerLeaderboard(
            @PathVariable UUID partnerId,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.getPartnerLeaderboard(partnerId, pageable));
    }

    @GetMapping("/global")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER_OWNER')")
    @Operation(summary = "Get global leaderboard")
    public ResponseEntity<Page<UserScoreDto>> getGlobalLeaderboard(Pageable pageable) {
        return ResponseEntity.ok(eventService.getGlobalLeaderboard(pageable));
    }

    private UUID extractUserId(UserDetails userDetails) {
        // FIX: userDetails.getUsername() retourne l'email, pas l'UUID
        // Il faut chercher l'utilisateur dans la base pour obtenir son ID
        return userRepository.findByEmail(userDetails.getUsername())
                .map(user -> user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userDetails.getUsername()));
    }
}

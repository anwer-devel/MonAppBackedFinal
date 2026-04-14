package com.app.event.controller;

import com.app.common.response.ApiResponse;
import com.app.event.dto.UserScoreDto;
import com.app.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Leaderboard and ranking endpoints")
public class LeaderboardController {

    private final EventService eventService;

    @GetMapping("/top")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get top users from global leaderboard")
    public ResponseEntity<ApiResponse<Page<UserScoreDto>>> getTopLeaderboard(
            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 100, message = "Limit cannot exceed 100")
            Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<UserScoreDto> topUsers = eventService.getGlobalLeaderboard(pageable);
        return ResponseEntity.ok(ApiResponse.success(topUsers, "Top leaderboard retrieved"));
    }
}

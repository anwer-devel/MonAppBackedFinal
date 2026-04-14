package com.app.auth.controller;

import com.app.auth.dto.UpdateProfileRequest;
import com.app.auth.dto.UserDto;
import com.app.auth.service.UserProfileService;
import com.app.common.response.ApiResponse;
import com.app.common.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management endpoints")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get current user profile", description = "Returns full profile of authenticated user")
    public ResponseEntity<ApiResponse<UserDto>> getMyProfile() {
        UUID userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }
        UserDto profile = userProfileService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved"));
    }

    @PutMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update current user profile", description = "Update username, avatar, or title")
    public ResponseEntity<ApiResponse<UserDto>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }
        UserDto profile = userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile updated"));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get public user profile", description = "Returns public profile info")
    public ResponseEntity<ApiResponse<UserDto>> getPublicProfile(@PathVariable UUID userId) {
        UserDto profile = userProfileService.getPublicProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Public profile retrieved"));
    }
}

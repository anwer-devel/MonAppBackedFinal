package com.app.friendship.controller;

import com.app.common.response.ApiResponse;
import com.app.common.security.SecurityUtil;
import com.app.friendship.dto.FriendDTO;
import com.app.friendship.dto.InvitableFriendDTO;
import com.app.friendship.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Friends", description = "Friendship management endpoints")
public class FriendController {

    private final FriendshipService friendshipService;

    @GetMapping
    @Operation(summary = "List friends", description = "Get all accepted friends")
    public ResponseEntity<ApiResponse<List<FriendDTO>>> getFriends() {
        UUID userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(friendshipService.getFriends(userId), "Friends retrieved"));
    }

    @GetMapping("/pending")
    @Operation(summary = "Pending requests", description = "Get pending friend requests")
    public ResponseEntity<ApiResponse<List<FriendDTO>>> getPendingRequests() {
        UUID userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(friendshipService.getPendingRequests(userId), "Pending requests retrieved"));
    }

    @PostMapping("/request/{friendId}")
    @Operation(summary = "Send friend request")
    public ResponseEntity<ApiResponse<FriendDTO>> sendRequest(@PathVariable UUID friendId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        FriendDTO result = friendshipService.sendFriendRequest(userId, friendId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Friend request sent"));
    }

    @PostMapping("/accept/{friendshipId}")
    @Operation(summary = "Accept friend request")
    public ResponseEntity<ApiResponse<FriendDTO>> acceptRequest(@PathVariable UUID friendshipId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        FriendDTO result = friendshipService.acceptFriendRequest(userId, friendshipId);
        return ResponseEntity.ok(ApiResponse.success(result, "Friend request accepted"));
    }

    @DeleteMapping("/{friendshipId}")
    @Operation(summary = "Remove friend")
    public ResponseEntity<ApiResponse<Void>> removeFriend(@PathVariable UUID friendshipId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        friendshipService.removeFriend(userId, friendshipId);
        return ResponseEntity.ok(ApiResponse.success(null, "Friend removed"));
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Recommended friends")
    public ResponseEntity<ApiResponse<List<FriendDTO>>> getRecommendations() {
        UUID userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(friendshipService.getRecommendations(userId), "Recommendations retrieved"));
    }

    @GetMapping("/invitable")
    @Operation(summary = "Invitable friends for game rooms")
    public ResponseEntity<ApiResponse<List<InvitableFriendDTO>>> getInvitableFriends() {
        UUID userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(friendshipService.getInvitableFriends(userId), "Invitable friends retrieved"));
    }
}

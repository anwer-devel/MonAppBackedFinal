package com.app.friendship.service;

import com.app.friendship.dto.FriendDTO;
import com.app.friendship.dto.InvitableFriendDTO;

import java.util.List;
import java.util.UUID;

public interface FriendshipService {
    List<FriendDTO> getFriends(UUID userId);
    List<FriendDTO> getPendingRequests(UUID userId);
    FriendDTO sendFriendRequest(UUID userId, UUID friendId);
    FriendDTO acceptFriendRequest(UUID userId, UUID friendshipId);
    void removeFriend(UUID userId, UUID friendshipId);
    List<FriendDTO> getRecommendations(UUID userId);
    List<InvitableFriendDTO> getInvitableFriends(UUID userId);
}

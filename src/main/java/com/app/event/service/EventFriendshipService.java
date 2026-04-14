package com.app.event.service;

import com.app.event.dto.FriendshipDto;
import com.app.event.dto.SendFriendRequestDto;

import java.util.List;
import java.util.UUID;

public interface EventFriendshipService {

    FriendshipDto sendFriendRequest(UUID requesterId, SendFriendRequestDto dto);

    FriendshipDto respondToRequest(UUID friendshipId, UUID userId, boolean accept);

    List<FriendshipDto> getFriends(UUID userId);

    List<FriendshipDto> getPendingRequests(UUID userId);

    boolean isFriend(UUID userId1, UUID userId2);

    void blockUser(UUID requesterId, UUID targetId);
}

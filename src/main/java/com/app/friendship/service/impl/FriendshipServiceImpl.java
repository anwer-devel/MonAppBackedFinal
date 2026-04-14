package com.app.friendship.service.impl;

import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.common.exception.BadRequestException;
import com.app.common.exception.ResourceNotFoundException;
import com.app.friendship.dto.FriendDTO;
import com.app.friendship.dto.InvitableFriendDTO;
import com.app.friendship.entity.Friendship;
import com.app.friendship.repository.FriendshipRepository;
import com.app.friendship.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FriendDTO> getFriends(UUID userId) {
        return friendshipRepository.findAcceptedFriendships(userId).stream()
                .map(f -> toFriendDTO(f, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendDTO> getPendingRequests(UUID userId) {
        return friendshipRepository.findPendingRequests(userId).stream()
                .map(f -> toFriendDTO(f, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FriendDTO sendFriendRequest(UUID userId, UUID friendId) {
        if (userId.equals(friendId)) {
            throw new BadRequestException("Cannot send friend request to yourself");
        }

        friendshipRepository.findFriendshipBetween(userId, friendId)
                .ifPresent(f -> {
                    throw new BadRequestException("Friendship already exists");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found"));

        Friendship friendship = Friendship.builder()
                .user(user)
                .friend(friend)
                .status(Friendship.FriendshipStatus.PENDING)
                .build();
        friendship.setIsActive(true);

        friendship = friendshipRepository.save(friendship);
        log.info("Friend request sent from {} to {}", userId, friendId);

        return toFriendDTO(friendship, userId);
    }

    @Override
    @Transactional
    public FriendDTO acceptFriendRequest(UUID userId, UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship not found"));

        if (!friendship.getFriend().getId().equals(userId)) {
            throw new BadRequestException("Only the recipient can accept the friend request");
        }

        friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        friendship = friendshipRepository.save(friendship);
        log.info("Friend request {} accepted by {}", friendshipId, userId);

        return toFriendDTO(friendship, userId);
    }

    @Override
    @Transactional
    public void removeFriend(UUID userId, UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship not found"));

        if (!friendship.getUser().getId().equals(userId) && !friendship.getFriend().getId().equals(userId)) {
            throw new BadRequestException("Not authorized to remove this friendship");
        }

        friendship.setIsActive(false);
        friendshipRepository.save(friendship);
        log.info("Friendship {} removed by {}", friendshipId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendDTO> getRecommendations(UUID userId) {
        // Simple recommendation: users with similar level, not already friends
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<UUID> friendIds = friendshipRepository.findAcceptedFriendships(userId).stream()
                .map(f -> f.getUser().getId().equals(userId) ? f.getFriend().getId() : f.getUser().getId())
                .collect(Collectors.toList());
        friendIds.add(userId);

        return userRepository.findAll().stream()
                .filter(u -> !friendIds.contains(u.getId()))
                .filter(u -> u.getIsActive())
                .limit(10)
                .map(u -> FriendDTO.builder()
                        .userId(u.getId())
                        .username(u.getUsername())
                        .avatarUrl(u.getAvatarUrl())
                        .xp(u.getXp())
                        .level(u.getLevel())
                        .isOnline(u.getIsOnline())
                        .isRecommended(true)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitableFriendDTO> getInvitableFriends(UUID userId) {
        return friendshipRepository.findAcceptedFriendships(userId).stream()
                .map(f -> {
                    User friend = f.getUser().getId().equals(userId) ? f.getFriend() : f.getUser();
                    return InvitableFriendDTO.builder()
                            .id(friend.getId())
                            .firstName(friend.getUsername())
                            .avatarUrl(friend.getAvatarUrl())
                            .isSelected(false)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private FriendDTO toFriendDTO(Friendship friendship, UUID currentUserId) {
        User friendUser = friendship.getUser().getId().equals(currentUserId)
                ? friendship.getFriend() : friendship.getUser();

        return FriendDTO.builder()
                .id(friendship.getId())
                .userId(friendUser.getId())
                .username(friendUser.getUsername())
                .avatarUrl(friendUser.getAvatarUrl())
                .xp(friendUser.getXp())
                .level(friendUser.getLevel())
                .isOnline(friendUser.getIsOnline())
                .status(friendship.getStatus().name())
                .isRecommended(false)
                .build();
    }
}

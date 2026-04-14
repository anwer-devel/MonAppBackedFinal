package com.app.event.service.impl;

import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.common.exception.BadRequestException;
import com.app.common.exception.ForbiddenException;
import com.app.common.exception.ResourceNotFoundException;
import com.app.event.dto.FriendshipDto;
import com.app.event.dto.SendFriendRequestDto;
import com.app.event.message.FriendshipAcceptedMessage;
import com.app.event.message.FriendshipRequestedMessage;
import com.app.event.service.EventFriendshipService;
import com.app.friendship.entity.Friendship;
import com.app.friendship.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.app.event.config.EventRabbitMQConfig.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventFriendshipServiceImpl implements EventFriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public FriendshipDto sendFriendRequest(UUID requesterId, SendFriendRequestDto dto) {
        UUID addresseeId = dto.getAddresseeId();

        if (requesterId.equals(addresseeId)) {
            throw new BadRequestException("Cannot send friend request to yourself");
        }

        // Check if users exist
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new ResourceNotFoundException("Addressee not found"));

        // Check if already friends or request exists
        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requesterId, addresseeId);
        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == Friendship.FriendshipStatus.ACCEPTED) {
                throw new BadRequestException("Already friends with this user");
            }
            if (f.getStatus() == Friendship.FriendshipStatus.PENDING) {
                throw new BadRequestException("Friend request already pending");
            }
            if (f.getStatus() == Friendship.FriendshipStatus.BLOCKED) {
                throw new BadRequestException("Cannot send request to blocked user");
            }
        }

        // Create new friendship request
        Friendship friendship = Friendship.builder()
                .user(requester)
                .friend(addressee)
                .status(Friendship.FriendshipStatus.PENDING)
                .build();

        Friendship saved = friendshipRepository.save(friendship);

        // Publish RabbitMQ event
        publishEvent(FRIENDSHIP_REQUESTED, new FriendshipRequestedMessage(requesterId, addresseeId, dto.getEventId()));

        // Notify addressee via WebSocket
        messagingTemplate.convertAndSendToUser(addresseeId.toString(), "/queue/friendRequests", mapToDto(saved));

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public FriendshipDto respondToRequest(UUID friendshipId, UUID userId, boolean accept) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        // Verify user is the addressee
        if (!friendship.getFriend().getId().equals(userId)) {
            throw new ForbiddenException("Only the addressee can respond to this request");
        }

        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            throw new BadRequestException("Friend request is not pending");
        }

        if (accept) {
            friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        } else {
            friendship.setStatus(Friendship.FriendshipStatus.BLOCKED); // Or we could delete it
            friendship.setIsActive(false);
        }

        Friendship saved = friendshipRepository.save(friendship);

        if (accept) {
            // Publish RabbitMQ event
            publishEvent(FRIENDSHIP_ACCEPTED, new FriendshipAcceptedMessage(
                    friendship.getUser().getId(), friendship.getFriend().getId()));

            // Notify requester via WebSocket
            messagingTemplate.convertAndSendToUser(
                    friendship.getUser().getId().toString(),
                    "/queue/notifications",
                    "Friend request accepted by " + friendship.getFriend().getUsername());
        }

        return mapToDto(saved);
    }

    @Override
    public List<FriendshipDto> getFriends(UUID userId) {
        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(userId);
        return friendships.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendshipDto> getPendingRequests(UUID userId) {
        List<Friendship> requests = friendshipRepository.findPendingRequests(userId);
        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFriend(UUID userId1, UUID userId2) {
        Optional<Friendship> friendship = friendshipRepository.findFriendshipBetween(userId1, userId2);
        return friendship.isPresent() && friendship.get().getStatus() == Friendship.FriendshipStatus.ACCEPTED;
    }

    @Override
    @Transactional
    public void blockUser(UUID requesterId, UUID targetId) {
        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requesterId, targetId);

        if (existing.isPresent()) {
            Friendship f = existing.get();
            f.setStatus(Friendship.FriendshipStatus.BLOCKED);
            f.setIsActive(false);
            friendshipRepository.save(f);
        } else {
            // Create a blocked entry
            User requester = userRepository.findById(requesterId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            User target = userRepository.findById(targetId)
                    .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

            Friendship blocked = Friendship.builder()
                    .user(requester)
                    .friend(target)
                    .status(Friendship.FriendshipStatus.BLOCKED)
                    .build();
            blocked.setIsActive(false);
            friendshipRepository.save(blocked);
        }
    }

    // ===== HELPER METHODS =====

    private FriendshipDto mapToDto(Friendship friendship) {
        return FriendshipDto.builder()
                .id(friendship.getId())
                .requesterId(friendship.getUser().getId())
                .addresseeId(friendship.getFriend().getId())
                .status(friendship.getStatus())
                .requesterUsername(friendship.getUser().getUsername())
                .addresseeUsername(friendship.getFriend().getUsername())
                .createdAt(friendship.getCreatedAt())
                .build();
    }

    private void publishEvent(String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(EVENT_EXCHANGE, routingKey, message);
        } catch (Exception e) {
            log.error("Failed to publish event to {}: {}", routingKey, e.getMessage());
        }
    }
}

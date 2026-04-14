package com.app.friendship.repository;

import com.app.friendship.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("SELECT f FROM Friendship f WHERE (f.user.id = :userId OR f.friend.id = :userId) AND f.status = 'ACCEPTED' AND f.isActive = true")
    List<Friendship> findAcceptedFriendships(@Param("userId") UUID userId);

    @Query("SELECT f FROM Friendship f WHERE f.friend.id = :userId AND f.status = 'PENDING' AND f.isActive = true")
    List<Friendship> findPendingRequests(@Param("userId") UUID userId);

    @Query("SELECT f FROM Friendship f WHERE f.user.id = :userId AND f.friend.id = :friendId AND f.isActive = true")
    Optional<Friendship> findByUserAndFriend(@Param("userId") UUID userId, @Param("friendId") UUID friendId);

    @Query("SELECT f FROM Friendship f WHERE ((f.user.id = :userId AND f.friend.id = :friendId) OR (f.user.id = :friendId AND f.friend.id = :userId)) AND f.isActive = true")
    Optional<Friendship> findFriendshipBetween(@Param("userId") UUID userId, @Param("friendId") UUID friendId);

    @Query("SELECT COUNT(f) FROM Friendship f WHERE (f.user.id = :userId OR f.friend.id = :userId) AND f.status = 'ACCEPTED' AND f.isActive = true")
    long countFriends(@Param("userId") UUID userId);
}

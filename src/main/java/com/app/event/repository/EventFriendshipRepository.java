package com.app.event.repository;

import com.app.friendship.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventFriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("SELECT f FROM Friendship f WHERE f.user.id = :requesterId AND f.friend.id = :addresseeId AND f.isActive = true")
    Optional<Friendship> findByRequesterIdAndAddresseeId(@Param("requesterId") UUID requesterId, @Param("addresseeId") UUID addresseeId);

    @Query("SELECT f FROM Friendship f WHERE ((f.user.id = :userId1 AND f.friend.id = :userId2) OR (f.user.id = :userId2 AND f.friend.id = :userId1)) AND f.isActive = true")
    Optional<Friendship> findFriendshipBetween(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);

    @Query("SELECT f FROM Friendship f WHERE (f.user.id = :userId OR f.friend.id = :userId) AND f.status = 'ACCEPTED' AND f.isActive = true")
    List<Friendship> findAcceptedFriendsByUserId(@Param("userId") UUID userId);

    @Query("SELECT f FROM Friendship f WHERE f.friend.id = :addresseeId AND f.status = 'PENDING' AND f.isActive = true")
    List<Friendship> findPendingRequestsByAddresseeId(@Param("addresseeId") UUID addresseeId);

    @Modifying
    @Query("UPDATE Friendship f SET f.status = :status WHERE f.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") Friendship.FriendshipStatus status);

    @Modifying
    @Query("UPDATE Friendship f SET f.isActive = false WHERE f.id = :id")
    void softDelete(@Param("id") UUID id);
}

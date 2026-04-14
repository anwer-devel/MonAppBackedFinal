package com.app.partner.repository;

import com.app.partner.entity.UserFavoriteVenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFavoriteVenueRepository extends JpaRepository<UserFavoriteVenue, UUID> {
    Optional<UserFavoriteVenue> findByUserIdAndPartnerId(UUID userId, UUID partnerId);
    boolean existsByUserIdAndPartnerIdAndIsActiveTrue(UUID userId, UUID partnerId);
}

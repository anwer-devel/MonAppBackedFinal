package com.app.event.repository;

import com.app.event.entity.UserScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserScoreRepository extends JpaRepository<UserScore, UUID> {

    Optional<UserScore> findByUserIdAndPartnerId(UUID userId, UUID partnerId);

    @Query("SELECT us FROM UserScore us WHERE us.userId = :userId AND us.partnerId IS NULL")
    Optional<UserScore> findByUserIdAndPartnerIdIsNull(@Param("userId") UUID userId);

    Page<UserScore> findTopByOrderByTotalScoreDesc(Pageable pageable);

    @Query("SELECT us FROM UserScore us WHERE us.partnerId = :partnerId ORDER BY us.totalScore DESC")
    Page<UserScore> findTopByPartnerIdOrderByTotalScoreDesc(@Param("partnerId") UUID partnerId, Pageable pageable);

    @Modifying
    @Query(value = """
        UPDATE user_scores 
        SET rank = subquery.rank, last_updated_at = :now
        FROM (
            SELECT id, RANK() OVER(ORDER BY total_score DESC) as rank 
            FROM user_scores 
            WHERE partner_id IS NULL
        ) subquery 
        WHERE user_scores.id = subquery.id
        """, nativeQuery = true)
    void updateGlobalRanks(@Param("now") LocalDateTime now);

    @Modifying
    @Query(value = """
        UPDATE user_scores 
        SET rank = subquery.rank, last_updated_at = :now
        FROM (
            SELECT id, RANK() OVER(PARTITION BY partner_id ORDER BY total_score DESC) as rank 
            FROM user_scores 
            WHERE partner_id = :partnerId
        ) subquery 
        WHERE user_scores.id = subquery.id
        """, nativeQuery = true)
    void updatePartnerRanks(@Param("partnerId") UUID partnerId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserScore us SET us.totalScore = us.totalScore + :points, us.totalEvents = us.totalEvents + 1, us.totalCorrectAnswers = us.totalCorrectAnswers + :correctAnswers, us.totalWrongAnswers = us.totalWrongAnswers + :wrongAnswers, us.lastUpdatedAt = :now WHERE us.id = :id")
    void updateScore(@Param("id") UUID id, @Param("points") Integer points, @Param("correctAnswers") Integer correctAnswers, @Param("wrongAnswers") Integer wrongAnswers, @Param("now") LocalDateTime now);
}

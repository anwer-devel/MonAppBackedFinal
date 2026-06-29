package com.erp.platform.iam.repository;

import com.erp.platform.iam.entity.Collaborator;
import com.erp.platform.iam.enums.CollaboratorRole;
import com.erp.platform.iam.enums.CollaboratorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, UUID> {

    Optional<Collaborator> findByEmailAndIsDeletedFalse(String email);

    Optional<Collaborator> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
        SELECT c FROM Collaborator c
        LEFT JOIN FETCH c.partner p
        LEFT JOIN FETCH c.defaultLocal l
        WHERE c.id = :id
          AND c.isDeleted = false
        """)
    Optional<Collaborator> findByIdWithRelations(@Param("id") UUID id);

    Optional<Collaborator> findByRefreshTokenHashAndIsDeletedFalse(String refreshTokenHash);

    @Query("""
        SELECT c FROM Collaborator c
        LEFT JOIN FETCH c.partner p
        LEFT JOIN FETCH c.defaultLocal l
        WHERE c.email = :email
          AND c.isDeleted = false
        """)
    Optional<Collaborator> findByEmailWithRelations(@Param("email") String email);

    @Query("""
        SELECT c FROM Collaborator c
        LEFT JOIN FETCH c.partner p
        LEFT JOIN FETCH c.defaultLocal l
        WHERE c.refreshTokenHash = :hash
          AND c.isDeleted = false
        """)
    Optional<Collaborator> findByRefreshTokenHashWithRelations(@Param("hash") String hash);

    @Query("""
        SELECT COUNT(c) FROM Collaborator c
        WHERE c.partner.id = :partnerId
          AND c.role != com.erp.platform.iam.enums.CollaboratorRole.PARTNER_ADMIN
          AND c.isDeleted = false
        """)
    int countNonAdminByPartnerId(@Param("partnerId") UUID partnerId);

    int countByPartner_IdAndStatusAndIsDeletedFalse(UUID partnerId, CollaboratorStatus status);

    int countByPartner_IdAndIsDeletedFalse(UUID partnerId);

    @Query("""
        SELECT c FROM Collaborator c WHERE c.isDeleted = false
          AND (:partnerId IS NULL OR c.partner.id = :partnerId)
          AND (:role IS NULL OR c.role = :role)
          AND (:localId IS NULL OR c.defaultLocal.id = :localId)
          AND (
            :q IS NULL
            OR LOWER(c.firstName) LIKE :qPattern
            OR LOWER(c.lastName) LIKE :qPattern
            OR LOWER(c.email) LIKE :qPattern
          )
        """)
    Page<Collaborator> findWithFilters(@Param("partnerId") UUID partnerId,
                                       @Param("role") CollaboratorRole role,
                                       @Param("localId") UUID localId,
                                       @Param("q") String q,
                                       @Param("qPattern") String qPattern,
                                       Pageable pageable);

    Optional<Collaborator> findByPartner_IdAndRoleAndIsDeletedFalse(
            UUID partnerId, CollaboratorRole role);
}

package com.erp.platform.iam.repository;

import com.erp.platform.iam.entity.LocalUnit;
import com.erp.platform.iam.enums.LocalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocalUnitRepository extends JpaRepository<LocalUnit, UUID> {

    List<LocalUnit> findByPartner_IdAndIsDeletedFalse(UUID partnerId);

    List<LocalUnit> findByPartner_IdAndTypeAndIsDeletedFalse(UUID partnerId, LocalType type);

    Optional<LocalUnit> findByPartner_IdAndIsMainTrueAndIsDeletedFalse(UUID partnerId);

    Optional<LocalUnit> findByIdAndIsDeletedFalse(UUID id);

    boolean existsByPartner_IdAndCodeAndIsDeletedFalse(UUID partnerId, String code);

    int countByPartner_IdAndIsDeletedFalse(UUID partnerId);
}

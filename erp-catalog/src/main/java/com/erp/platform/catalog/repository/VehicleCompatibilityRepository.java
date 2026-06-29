package com.erp.platform.catalog.repository;

import com.erp.platform.catalog.entity.Product;
import com.erp.platform.catalog.entity.VehicleCompatibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VehicleCompatibilityRepository extends JpaRepository<VehicleCompatibility, UUID> {
    List<VehicleCompatibility> findByAutoPart_IdAndIsDeletedFalse(UUID autoPartId);

    @Query("SELECT DISTINCT v.vehicleMake FROM VehicleCompatibility v " +
           "WHERE v.isDeleted=false ORDER BY v.vehicleMake")
    List<String> findDistinctMakes();

    @Query("SELECT DISTINCT v.vehicleModel FROM VehicleCompatibility v " +
           "WHERE v.vehicleMake = :make AND v.isDeleted=false ORDER BY v.vehicleModel")
    List<String> findModelsByMake(@Param("make") String make);

    @Query("""
      SELECT DISTINCT v.autoPart.product FROM VehicleCompatibility v
      LEFT JOIN FETCH v.autoPart.product.category c
      LEFT JOIN FETCH v.autoPart.product.unit u
      WHERE v.isDeleted = false
        AND LOWER(v.vehicleMake) LIKE LOWER(CONCAT('%',:make,'%'))
        AND LOWER(v.vehicleModel) LIKE LOWER(CONCAT('%',:model,'%'))
        AND (:year IS NULL
          OR (v.yearFrom IS NULL OR v.yearFrom <= :year)
             AND (v.yearTo IS NULL OR v.yearTo >= :year))
      """)
    List<Product> findCompatibleProducts(
        @Param("make") String make,
        @Param("model") String model,
        @Param("year") Integer year
    );
}

package com.erp.platform.catalog.repository;

import com.erp.platform.catalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByRefIgnoreCaseAndIsDeletedFalse(String ref);
    Optional<Product> findByBarcodeAndIsDeletedFalse(String barcode);

    @Query("""
      SELECT p FROM Product p
      LEFT JOIN FETCH p.category c
      LEFT JOIN FETCH p.unit u
      WHERE (UPPER(p.barcode) = UPPER(:code)
        OR UPPER(p.ref) = UPPER(:code))
        AND p.isDeleted = false
        AND p.isActive = true
      """)
    Optional<Product> findByBarcodeOrRef(@Param("code") String code);

    @Query(value = """
      SELECT p FROM Product p
      LEFT JOIN FETCH p.category c
      LEFT JOIN FETCH p.unit u
      WHERE p.isDeleted = false
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:sectorType IS NULL OR p.sectorType = :sectorType)
        AND (:isActive IS NULL OR p.isActive = :isActive)
        AND (:isFavorite IS NULL OR p.isFavorite = :isFavorite)
        AND (:q IS NULL
          OR LOWER(p.name) LIKE :qPattern
          OR LOWER(p.ref) LIKE :qPattern
          OR LOWER(p.shortDescription) LIKE :qPattern)
      """,
      countQuery = """
      SELECT COUNT(p) FROM Product p
      WHERE p.isDeleted = false
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:sectorType IS NULL OR p.sectorType = :sectorType)
        AND (:isActive IS NULL OR p.isActive = :isActive)
        AND (:isFavorite IS NULL OR p.isFavorite = :isFavorite)
        AND (:q IS NULL
          OR LOWER(p.name) LIKE :qPattern
          OR LOWER(p.ref) LIKE :qPattern
          OR LOWER(p.shortDescription) LIKE :qPattern)
      """)
    Page<Product> searchProducts(
        @Param("categoryId") UUID categoryId,
        @Param("sectorType") String sectorType,
        @Param("isActive") Boolean isActive,
        @Param("isFavorite") Boolean isFavorite,
        @Param("q") String q,
        @Param("qPattern") String qPattern,
        Pageable pageable
    );

    @Query("""
      SELECT p FROM Product p
      LEFT JOIN FETCH p.category c
      LEFT JOIN FETCH p.unit u
      WHERE p.isFavorite = true
        AND p.isActive = true
        AND p.isDeleted = false
      ORDER BY p.name ASC
      """)
    List<Product> findFavorites();

    @Query("""
      SELECT p FROM Product p
      LEFT JOIN FETCH p.category c
      LEFT JOIN FETCH p.unit u
      LEFT JOIN FETCH p.autoPartExt ape
      LEFT JOIN FETCH p.pharmaExt pe
      LEFT JOIN FETCH p.hardwareExt he
      WHERE p.id = :id AND p.isDeleted = false
      """)
    Optional<Product> findByIdWithExtensions(@Param("id") UUID id);
}

package com.erp.platform.catalog.entity;

import com.erp.platform.core.common.BaseEntity;
import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "products")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Product extends BaseEntity {

    @Column(name = "ref", nullable = false, unique = true, length = 100)
    private String ref;

    @Column(name = "barcode", unique = true, length = 50)
    private String barcode;

    @Column(name = "name", nullable = false, length = 300)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "sector_type", length = 20)
    private String sectorType;

    @Column(name = "purchase_price_ht", nullable = false, precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal purchasePriceHT = BigDecimal.ZERO;

    @Column(name = "sale_price_ht", nullable = false, precision = 12, scale = 3)
    private BigDecimal salePriceHT;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = new BigDecimal("19.0");

    @Column(name = "margin_rate", precision = 6, scale = 2)
    private BigDecimal marginRate;

    @Column(name = "min_stock_level", nullable = false)
    @Builder.Default
    private int minStockLevel = 0;

    @Column(name = "safety_stock_level", nullable = false)
    @Builder.Default
    private int safetyStockLevel = 0;

    @Column(name = "max_stock_level", nullable = false)
    @Builder.Default
    private int maxStockLevel = 9999;

    @Column(name = "track_stock", nullable = false)
    @Builder.Default
    private boolean trackStock = true;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private boolean isFavorite = false;

    @Type(JsonBinaryType.class)
    @Column(name = "attributes", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, String> attributes = new HashMap<>();

    @Type(StringArrayType.class)
    @Column(name = "image_urls", columnDefinition = "text[]")
    private String[] imageUrls;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private AutoPartExtension autoPartExt;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PharmaExtension pharmaExt;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private HardwareExtension hardwareExt;
}

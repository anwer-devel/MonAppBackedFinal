package com.erp.platform.catalog.entity;

import com.erp.platform.core.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "units")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Unit extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;
}

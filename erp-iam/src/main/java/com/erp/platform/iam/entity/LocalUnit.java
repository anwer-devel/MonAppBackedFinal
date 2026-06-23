package com.erp.platform.iam.entity;

import com.erp.platform.core.common.BaseEntity;
import com.erp.platform.iam.enums.LocalStatus;
import com.erp.platform.iam.enums.LocalType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(schema = "public", name = "local_units",
        uniqueConstraints = @UniqueConstraint(columnNames = {"partner_id", "code"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalUnit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String code;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LocalType type;

    private String address;

    private String phone;

    private String email;

    @Column(name = "is_main", nullable = false)
    private boolean isMain = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LocalStatus status = LocalStatus.ACTIVE;
}

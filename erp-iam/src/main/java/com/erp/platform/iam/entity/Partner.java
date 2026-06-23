package com.erp.platform.iam.entity;

import com.erp.platform.core.common.BaseEntity;
import com.erp.platform.iam.enums.PartnerStatus;
import com.erp.platform.iam.enums.PlanType;
import com.erp.platform.iam.enums.SectorType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;

@Entity
@Table(schema = "public", name = "partners")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Partner extends BaseEntity {

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "[A-Z0-9\\-]+")
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "sector_type", nullable = false, length = 20)
    private SectorType sectorType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanType plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartnerStatus status;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String address;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "subscription_end")
    private LocalDate subscriptionEnd;

    @Column(length = 10)
    private String currency = "TND";

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private PartnerConfig config;
}

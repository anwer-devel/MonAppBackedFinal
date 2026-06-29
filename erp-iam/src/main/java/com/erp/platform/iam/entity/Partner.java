package com.erp.platform.iam.entity;

import com.erp.platform.core.common.BaseEntity;
import com.erp.platform.iam.enums.PartnerStatus;
import com.erp.platform.iam.enums.PlanType;
import com.erp.platform.iam.enums.SectorType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(schema = "public", name = "partners",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_partner_code", columnNames = "code"),
    @UniqueConstraint(name = "uk_partner_email", columnNames = "email")
  })
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Partner extends BaseEntity {

  @Column(name = "code", nullable = false, length = 20)
  private String code;

  @Column(name = "name", nullable = false, length = 200)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "sector_type", nullable = false, length = 20)
  private SectorType sectorType;

  @Enumerated(EnumType.STRING)
  @Column(name = "plan", nullable = false, length = 20)
  private PlanType plan;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private PartnerStatus status = PartnerStatus.TRIAL;

  @Column(name = "email", nullable = false, length = 255)
  private String email;

  @Column(name = "phone", length = 30)
  private String phone;

  @Column(name = "address", columnDefinition = "TEXT")
  private String address;

  @Column(name = "tax_number", length = 100)
  private String taxNumber;

  @Column(name = "subscription_end")
  private LocalDate subscriptionEnd;

  @Builder.Default
  @Column(name = "currency", length = 10)
  private String currency = "TND";

  @Builder.Default
  @Type(JsonBinaryType.class)
  @Column(name = "config", columnDefinition = "jsonb")
  private PartnerConfig config = new PartnerConfig();
}


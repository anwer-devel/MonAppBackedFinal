package com.erp.platform.iam.entity;

import com.erp.platform.core.common.BaseEntity;
import com.erp.platform.iam.enums.CollaboratorRole;
import com.erp.platform.iam.enums.CollaboratorStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "public", name = "collaborators")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Collaborator extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_local_id")
    private LocalUnit defaultLocal;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CollaboratorRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CollaboratorStatus status = CollaboratorStatus.ACTIVE;

    @Column(name = "multi_local")
    private boolean multiLocal = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "collaborator_local_access",
            schema = "public",
            joinColumns = @JoinColumn(name = "collaborator_id")
    )
    @Column(name = "local_id")
    @Builder.Default
    private List<UUID> localAccess = new ArrayList<>();

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip")
    private String lastLoginIp;

    @Column(name = "refresh_token_hash")
    private String refreshTokenHash;

    @Column(name = "refresh_token_expiry")
    private LocalDateTime refreshTokenExpiry;
}

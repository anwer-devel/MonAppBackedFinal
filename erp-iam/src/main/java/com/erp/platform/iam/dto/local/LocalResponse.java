package com.erp.platform.iam.dto.local;

import com.erp.platform.iam.enums.LocalStatus;
import com.erp.platform.iam.enums.LocalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalResponse {

    private UUID id;
    private UUID partnerId;
    private String code;
    private String name;
    private LocalType type;
    private String address;
    private String phone;
    private String email;
    private boolean isMain;
    private LocalStatus status;
    private int collaboratorsCount;
    private LocalDateTime createdAt;
}

package com.erp.platform.iam.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {

    @NotBlank(message = "Le refresh token est requis")
    private String refreshToken;
}

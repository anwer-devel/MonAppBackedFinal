package com.erp.platform.iam.dto.local;

import lombok.Data;

@Data
public class UpdateLocalRequest {

    private String name;
    private String address;
    private String phone;
    private String email;
}

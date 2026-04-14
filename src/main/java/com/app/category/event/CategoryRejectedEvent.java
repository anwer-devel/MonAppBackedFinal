package com.app.category.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRejectedEvent implements Serializable {

    private UUID categoryId;
    private UUID partnerId;
    private String categoryName;
    private String rejectionReason;
    private Long timestamp;
}


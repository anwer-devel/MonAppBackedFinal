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
public class CategoryCreatedEvent implements Serializable {

    private UUID categoryId;
    private UUID partnerId;
    private String categoryType;
    private String categoryName;
    private Long timestamp;
}


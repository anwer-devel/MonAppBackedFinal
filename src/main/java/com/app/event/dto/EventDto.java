package com.app.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {

    private UUID id;

    private String title;

    private String description;

    private String coverImage;

    private String eventType;

    private String status;

    private UUID categoryId;

    private String categoryName;

    private UUID partnerId;

    private String partnerName;

    private String createdBy;

    private String visibility;

    private String scheduledAt;

    private Integer maxParticipants;

    private Integer currentParticipants;

    private Integer totalQuestions;

    private Boolean isActive;

    private String createdAt;
}

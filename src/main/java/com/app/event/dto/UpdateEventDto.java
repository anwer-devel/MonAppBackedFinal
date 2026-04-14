package com.app.event.dto;

import com.app.event.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventDto {

    private String title;

    private String description;

    private String coverImage;

    private Event.EventVisibility visibility;

    private LocalDateTime scheduledAt;

    private Integer maxParticipants;

    private Integer questionTimeLimit;

    private Map<String, Object> metadata;

    private Boolean isActive;
}

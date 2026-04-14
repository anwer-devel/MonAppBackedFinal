package com.app.event.dto;

import com.app.event.entity.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String coverImage;

    @NotNull(message = "Event type is required")
    private Event.EventType eventType;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Visibility is required")
    private Event.EventVisibility visibility;

    private LocalDateTime scheduledAt;

    private Integer maxParticipants;

    private Integer questionTimeLimit;

    private Map<String, Object> metadata;
}

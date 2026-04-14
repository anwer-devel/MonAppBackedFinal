package com.app.event.dto;

import com.app.event.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterEventDto {

    private Event.EventType eventType;

    private Event.EventStatus status;

    private UUID partnerId;

    private UUID categoryId;

    private String search;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer limit = 20;
}

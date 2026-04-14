package com.app.event.message;

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
public class EventCreatedMessage {
    private UUID eventId;
    private UUID partnerId;
    private Event.EventType eventType;
    private UUID categoryId;
}

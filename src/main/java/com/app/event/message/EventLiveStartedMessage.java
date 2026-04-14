package com.app.event.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLiveStartedMessage {
    private UUID eventId;
    private UUID partnerId;
    private Integer participantCount;
}

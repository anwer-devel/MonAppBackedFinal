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
public class FriendshipRequestedMessage {
    private UUID requesterId;
    private UUID addresseeId;
    private UUID eventId;
}

package com.app.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendFriendRequestDto {

    @NotNull(message = "Addressee ID is required")
    private UUID addresseeId;

    private UUID eventId;
}

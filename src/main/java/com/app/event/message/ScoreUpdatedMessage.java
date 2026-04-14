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
public class ScoreUpdatedMessage {
    private UUID userId;
    private UUID partnerId;
    private Long newScore;
    private Integer delta;
}

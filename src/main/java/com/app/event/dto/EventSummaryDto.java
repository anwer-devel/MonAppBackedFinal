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
public class EventSummaryDto {

    private UUID eventId;

    private UUID userId;

    private Integer score;

    private Integer rank;

    private Integer totalParticipants;

    private Integer correctAnswers;

    private Integer wrongAnswers;

    private Integer pointsEarned;

    private Long newTotalScore;
}

package com.app.event.dto;

import com.app.event.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveEventStateDto {

    private UUID eventId;

    private Event.EventStatus status;

    private Integer currentQuestionIndex;

    private Integer totalQuestions;

    private CategoryContentResponseDto currentQuestion;

    private Integer timeRemaining;

    private Integer participantCount;

    private List<LeaderboardEntryDto> leaderboard;
}

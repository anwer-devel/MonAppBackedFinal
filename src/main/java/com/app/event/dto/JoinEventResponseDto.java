package com.app.event.dto;

import com.app.category.dto.CategoryContentDto;
import com.app.event.entity.EventParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinEventResponseDto {

    private UUID eventId;

    private UUID participantId;

    private EventParticipant.ParticipantStatus status;

    private String message;

    private CategoryContentDto currentQuestion;
}

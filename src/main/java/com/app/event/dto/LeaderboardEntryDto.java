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
public class LeaderboardEntryDto {

    private Integer rank;

    private UUID userId;

    private String username;

    private String avatar;

    private Integer score;

    private Integer correctAnswers;

    private Boolean isFriend;

    private Boolean isCurrentUser;
}

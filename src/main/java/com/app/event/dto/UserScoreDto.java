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
public class UserScoreDto {

    private UUID userId;

    private String username;

    private Long totalScore;

    private Integer rank;

    private Integer totalEvents;

    private Long partnerScore;

    private Integer globalRank;
}

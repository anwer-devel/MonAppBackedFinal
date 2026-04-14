package com.app.friendship.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private String avatarUrl;
    private Integer xp;
    private Integer level;
    private Boolean isOnline;
    private String status;
    private Boolean isRecommended;
}

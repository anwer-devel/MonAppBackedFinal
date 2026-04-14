package com.app.event.message;

import com.app.event.dto.LeaderboardEntryDto;
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
public class EventLiveFinishedMessage {
    private UUID eventId;
    private UUID winnerId;
    private List<LeaderboardEntryDto> leaderboard;
}

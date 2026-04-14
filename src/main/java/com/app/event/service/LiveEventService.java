package com.app.event.service;

import com.app.event.dto.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface LiveEventService {

    // ===== Gestion du flow LIVE en temps réel =====
    @Transactional
    void startLiveEvent(UUID eventId);

    @Transactional
    void sendNextQuestion(UUID eventId);

    @Transactional
    AnswerResultDto submitLiveAnswer(UUID eventId, SubmitAnswerDto dto, UUID userId);

    @Transactional
    void advanceQuestion(UUID eventId);

    @Transactional
    void finishLiveEvent(UUID eventId);

    LiveEventStateDto getCurrentState(UUID eventId);
}

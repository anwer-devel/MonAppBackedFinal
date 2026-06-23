package com.erp.platform.core.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    public void log(UUID userId, String userEmail, String action,
                    String entityType, UUID entityId,
                    Object oldValue, Object newValue,
                    String partnerCode, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .userId(userId)
                    .userEmail(userEmail)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(serialize(oldValue))
                    .newValue(serialize(newValue))
                    .partnerCode(partnerCode)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} - {} - {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    private String serialize(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit value: {}", e.getMessage());
            return value.toString();
        }
    }
}

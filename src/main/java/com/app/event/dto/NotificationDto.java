package com.app.event.dto;

import com.app.event.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private UUID id;

    private Notification.NotificationType type;

    private String title;

    private String body;

    private Map<String, Object> data;

    private Boolean isRead;

    private LocalDateTime createdAt;
}

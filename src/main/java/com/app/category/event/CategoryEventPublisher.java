package com.app.category.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes category events to RabbitMQ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    // ===== Exchange and routing keys =====
    private static final String CATEGORY_EXCHANGE = "category-exchange";
    private static final String CATEGORY_CREATED_KEY = "category.created";
    private static final String CATEGORY_APPROVED_KEY = "category.approved";
    private static final String CATEGORY_REJECTED_KEY = "category.rejected";
    private static final String CATEGORY_DELETED_KEY = "category.deleted";

    /**
     * Publish category created event
     */
    public void publishCategoryCreated(CategoryCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(CATEGORY_EXCHANGE, CATEGORY_CREATED_KEY, event);
            log.debug("Published CategoryCreatedEvent for categoryId={}, partnerId={}",
                    event.getCategoryId(), event.getPartnerId());
        } catch (Exception e) {
            log.error("Failed to publish CategoryCreatedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish category approved event
     */
    public void publishCategoryApproved(CategoryApprovedEvent event) {
        try {
            rabbitTemplate.convertAndSend(CATEGORY_EXCHANGE, CATEGORY_APPROVED_KEY, event);
            log.debug("Published CategoryApprovedEvent for categoryId={}", event.getCategoryId());
        } catch (Exception e) {
            log.error("Failed to publish CategoryApprovedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish category rejected event
     */
    public void publishCategoryRejected(CategoryRejectedEvent event) {
        try {
            rabbitTemplate.convertAndSend(CATEGORY_EXCHANGE, CATEGORY_REJECTED_KEY, event);
            log.debug("Published CategoryRejectedEvent for categoryId={}, reason={}",
                    event.getCategoryId(), event.getRejectionReason());
        } catch (Exception e) {
            log.error("Failed to publish CategoryRejectedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish category deleted event
     */
    public void publishCategoryDeleted(CategoryDeletedEvent event) {
        try {
            rabbitTemplate.convertAndSend(CATEGORY_EXCHANGE, CATEGORY_DELETED_KEY, event);
            log.debug("Published CategoryDeletedEvent for categoryId={}", event.getCategoryId());
        } catch (Exception e) {
            log.error("Failed to publish CategoryDeletedEvent: {}", e.getMessage(), e);
        }
    }
}


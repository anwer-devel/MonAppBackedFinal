package com.app.category.consumer;

import com.app.category.event.CategoryCreatedEvent;
import com.app.category.event.CategoryApprovedEvent;
import com.app.category.event.CategoryRejectedEvent;
import com.app.category.event.CategoryDeletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for category events from RabbitMQ.
 * These listeners process category lifecycle events for analytics, notifications, etc.
 */
@Component
@Slf4j
public class CategoryEventConsumer {

    /**
     * Handle category created event
     */
    @RabbitListener(queues = "category-created-queue")
    public void handleCategoryCreated(CategoryCreatedEvent event) {
        try {
            log.info("Received CategoryCreatedEvent: categoryId={}, partnerId={}, type={}",
                    event.getCategoryId(), event.getPartnerId(), event.getCategoryType());

            // TODO: Implement analytics tracking, notifications, etc.
            // - Send notification to partner if category created by partner
            // - Track analytics
            // - Update partner statistics

        } catch (Exception e) {
            log.error("Error processing CategoryCreatedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle category approved event
     */
    @RabbitListener(queues = "category-approved-queue")
    public void handleCategoryApproved(CategoryApprovedEvent event) {
        try {
            log.info("Received CategoryApprovedEvent: categoryId={}, partnerId={}",
                    event.getCategoryId(), event.getPartnerId());

            // TODO: Implement approval handling
            // - Send notification to partner
            // - Update partner dashboard
            // - Trigger related events (e.g., make categories visible)

        } catch (Exception e) {
            log.error("Error processing CategoryApprovedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle category rejected event
     */
    @RabbitListener(queues = "category-rejected-queue")
    public void handleCategoryRejected(CategoryRejectedEvent event) {
        try {
            log.info("Received CategoryRejectedEvent: categoryId={}, reason={}",
                    event.getCategoryId(), event.getRejectionReason());

            // TODO: Implement rejection handling
            // - Send rejection notification to partner
            // - Store rejection reason in audit log
            // - Allow partner to resubmit

        } catch (Exception e) {
            log.error("Error processing CategoryRejectedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle category deleted event
     */
    @RabbitListener(queues = "category-deleted-queue")
    public void handleCategoryDeleted(CategoryDeletedEvent event) {
        try {
            log.info("Received CategoryDeletedEvent: categoryId={}, partnerId={}",
                    event.getCategoryId(), event.getPartnerId());

            // TODO: Implement deletion handling
            // - Cascade delete related game rooms, events, etc.
            // - Send notification to affected users
            // - Archive category data

        } catch (Exception e) {
            log.error("Error processing CategoryDeletedEvent: {}", e.getMessage(), e);
        }
    }
}


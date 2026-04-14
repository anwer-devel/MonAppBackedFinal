package com.app.category.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for category events.
 */
@Configuration
public class CategoryEventConfig {

    // ===== Exchange =====
    public static final String CATEGORY_EXCHANGE = "category-exchange";

    // ===== Queues =====
    public static final String CATEGORY_CREATED_QUEUE = "category-created-queue";
    public static final String CATEGORY_APPROVED_QUEUE = "category-approved-queue";
    public static final String CATEGORY_REJECTED_QUEUE = "category-rejected-queue";
    public static final String CATEGORY_DELETED_QUEUE = "category-deleted-queue";

    // ===== Routing Keys =====
    public static final String CATEGORY_CREATED_KEY = "category.created";
    public static final String CATEGORY_APPROVED_KEY = "category.approved";
    public static final String CATEGORY_REJECTED_KEY = "category.rejected";
    public static final String CATEGORY_DELETED_KEY = "category.deleted";

    @Bean
    public TopicExchange categoryExchange() {
        return new TopicExchange(CATEGORY_EXCHANGE, true, false);
    }

    @Bean
    public Queue categoryCreatedQueue() {
        return new Queue(CATEGORY_CREATED_QUEUE, true);
    }

    @Bean
    public Queue categoryApprovedQueue() {
        return new Queue(CATEGORY_APPROVED_QUEUE, true);
    }

    @Bean
    public Queue categoryRejectedQueue() {
        return new Queue(CATEGORY_REJECTED_QUEUE, true);
    }

    @Bean
    public Queue categoryDeletedQueue() {
        return new Queue(CATEGORY_DELETED_QUEUE, true);
    }

    @Bean
    public Binding categoryCreatedBinding(Queue categoryCreatedQueue, TopicExchange categoryExchange) {
        return BindingBuilder.bind(categoryCreatedQueue).to(categoryExchange).with(CATEGORY_CREATED_KEY);
    }

    @Bean
    public Binding categoryApprovedBinding(Queue categoryApprovedQueue, TopicExchange categoryExchange) {
        return BindingBuilder.bind(categoryApprovedQueue).to(categoryExchange).with(CATEGORY_APPROVED_KEY);
    }

    @Bean
    public Binding categoryRejectedBinding(Queue categoryRejectedQueue, TopicExchange categoryExchange) {
        return BindingBuilder.bind(categoryRejectedQueue).to(categoryExchange).with(CATEGORY_REJECTED_KEY);
    }

    @Bean
    public Binding categoryDeletedBinding(Queue categoryDeletedQueue, TopicExchange categoryExchange) {
        return BindingBuilder.bind(categoryDeletedQueue).to(categoryExchange).with(CATEGORY_DELETED_KEY);
    }
}


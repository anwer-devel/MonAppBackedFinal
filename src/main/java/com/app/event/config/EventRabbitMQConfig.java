package com.app.event.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventRabbitMQConfig {

    // ===== Exchange names =====
    public static final String EVENT_EXCHANGE = "event-exchange";

    // ===== Queue names =====
    public static final String EVENT_NOTIFICATIONS_QUEUE = "event-notifications-queue";
    public static final String EVENT_ANALYTICS_QUEUE = "event-analytics-queue";
    public static final String EVENT_LEADERBOARD_QUEUE = "event-leaderboard-queue";

    // ===== Routing keys =====
    public static final String EVENT_CREATED = "event.created";
    public static final String EVENT_LIVE_STARTED = "event.live.started";
    public static final String EVENT_LIVE_FINISHED = "event.live.finished";
    public static final String EVENT_SIMPLE_COMPLETED = "event.simple.completed";
    public static final String EVENT_CANCELLED = "event.cancelled";
    public static final String SCORE_UPDATED = "score.updated";
    public static final String FRIENDSHIP_REQUESTED = "friendship.requested";
    public static final String FRIENDSHIP_ACCEPTED = "friendship.accepted";

    // ===== Exchanges =====

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(EVENT_EXCHANGE);
    }

    // ===== Queues =====

    @Bean
    public Queue eventNotificationsQueue() {
        return new Queue(EVENT_NOTIFICATIONS_QUEUE, true);
    }

    @Bean
    public Queue eventAnalyticsQueue() {
        return new Queue(EVENT_ANALYTICS_QUEUE, true);
    }

    @Bean
    public Queue eventLeaderboardQueue() {
        return new Queue(EVENT_LEADERBOARD_QUEUE, true);
    }

    // ===== Bindings =====

    @Bean
    public Binding eventNotificationsBinding() {
        return BindingBuilder.bind(eventNotificationsQueue())
                .to(eventExchange())
                .with("event.#");
    }

    @Bean
    public Binding eventAnalyticsBinding() {
        return BindingBuilder.bind(eventAnalyticsQueue())
                .to(eventExchange())
                .with("event.#");
    }

    @Bean
    public Binding scoreUpdatedBinding() {
        return BindingBuilder.bind(eventLeaderboardQueue())
                .to(eventExchange())
                .with("score.#");
    }
}

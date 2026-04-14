package com.app.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration: exchanges, queues, bindings.
 */
@Configuration
public class RabbitMQConfig {

    // ===== Exchange names =====
    public static final String ROOM_EXCHANGE = "room-exchange";

    // ===== Queue names =====
    public static final String QUIZ_NOTIFICATIONS_QUEUE = "quiz-notifications-queue";
    public static final String ANALYTICS_EVENTS_QUEUE = "analytics-events-queue";
    public static final String LEADERBOARD_UPDATE_QUEUE = "leaderboard-update-queue";

    // ===== Routing keys =====
    public static final String ROOM_EVENT_KEY = "room.event.#";

    // ===== Exchanges =====

    @Bean
    public TopicExchange roomExchange() {
        return new TopicExchange(ROOM_EXCHANGE);
    }


    // ===== Message converter (JSON) =====

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}

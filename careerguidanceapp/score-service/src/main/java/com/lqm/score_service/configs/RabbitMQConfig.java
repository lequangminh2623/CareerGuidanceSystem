package com.lqm.score_service.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ACADEMIC_EVENTS_EXCHANGE = "academic.events";
    public static final String QUEUE_SCORE_SYNC = "q.score.sync";
    public static final String RK_SCORE_SYNC = "score.sync";

    @Bean
    public TopicExchange academicEventsExchange() {
        return new TopicExchange(ACADEMIC_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue scoreSyncQueue() {
        return QueueBuilder.durable(QUEUE_SCORE_SYNC).build();
    }

    @Bean
    public Binding scoreSyncBinding(Queue scoreSyncQueue, TopicExchange academicEventsExchange) {
        return BindingBuilder.bind(scoreSyncQueue).to(academicEventsExchange).with(RK_SCORE_SYNC);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

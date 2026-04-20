package com.lqm.chat_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ACADEMIC_EVENTS_EXCHANGE = "academic.events";

    public static final String QUEUE_GROUP_CREATE = "q.chat.group-create";
    public static final String QUEUE_GROUP_UPDATE_TEACHER = "q.chat.group-update-teacher";
    public static final String QUEUE_GROUP_DELETE = "q.chat.group-delete";

    public static final String RK_GROUP_CREATE = "chat.group.create";
    public static final String RK_GROUP_UPDATE_TEACHER = "chat.group.update-teacher";
    public static final String RK_GROUP_DELETE = "chat.group.delete";

    @Bean
    public TopicExchange academicEventsExchange() {
        return new TopicExchange(ACADEMIC_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue groupCreateQueue() {
        return QueueBuilder.durable(QUEUE_GROUP_CREATE).build();
    }

    @Bean
    public Queue groupUpdateTeacherQueue() {
        return QueueBuilder.durable(QUEUE_GROUP_UPDATE_TEACHER).build();
    }

    @Bean
    public Queue groupDeleteQueue() {
        return QueueBuilder.durable(QUEUE_GROUP_DELETE).build();
    }

    @Bean
    public Binding groupCreateBinding(Queue groupCreateQueue, TopicExchange academicEventsExchange) {
        return BindingBuilder.bind(groupCreateQueue).to(academicEventsExchange).with(RK_GROUP_CREATE);
    }

    @Bean
    public Binding groupUpdateTeacherBinding(Queue groupUpdateTeacherQueue, TopicExchange academicEventsExchange) {
        return BindingBuilder.bind(groupUpdateTeacherQueue).to(academicEventsExchange).with(RK_GROUP_UPDATE_TEACHER);
    }

    @Bean
    public Binding groupDeleteBinding(Queue groupDeleteQueue, TopicExchange academicEventsExchange) {
        return BindingBuilder.bind(groupDeleteQueue).to(academicEventsExchange).with(RK_GROUP_DELETE);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

package com.lqm.attendance_service.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ===== Exchange =====
    public static final String ACADEMIC_EVENTS_EXCHANGE = "academic.events";
    public static final String ATTENDANCE_EXCHANGE = "attendance.exchange";

    // ===== Queues =====
    public static final String QUEUE_STUDENTS_REMOVED = "q.attendance.students-removed";
    public static final String QUEUE_CLASSROOM_DELETED = "q.attendance.classroom-deleted";
    public static final String QUEUE_ABSENT = "q.attendance.absent";

    // ===== Routing Keys =====
    public static final String RK_STUDENTS_REMOVED = "attendance.students-removed";
    public static final String RK_CLASSROOM_DELETED = "attendance.classroom-deleted";

    // ===== Exchanges =====

    @Bean
    public TopicExchange academicEventsExchange() {
        return new TopicExchange(ACADEMIC_EVENTS_EXCHANGE);
    }

    @Bean
    public DirectExchange attendanceExchange() {
        return new DirectExchange(ATTENDANCE_EXCHANGE);
    }

    // ===== Queues =====

    @Bean
    public Queue studentsRemovedQueue() {
        return QueueBuilder.durable(QUEUE_STUDENTS_REMOVED).build();
    }

    @Bean
    public Queue classroomDeletedQueue() {
        return QueueBuilder.durable(QUEUE_CLASSROOM_DELETED).build();
    }

    @Bean
    public Queue absentQueue() {
        return QueueBuilder.durable(QUEUE_ABSENT).build();
    }

    // ===== Bindings =====

    @Bean
    public Binding studentsRemovedBinding(Queue studentsRemovedQueue, TopicExchange academicEventsExchange) {
        return BindingBuilder.bind(studentsRemovedQueue).to(academicEventsExchange).with(RK_STUDENTS_REMOVED);
    }

    @Bean
    public Binding classroomDeletedBinding(Queue classroomDeletedQueue, TopicExchange academicEventsExchange) {
        return BindingBuilder.bind(classroomDeletedQueue).to(academicEventsExchange).with(RK_CLASSROOM_DELETED);
    }

    @Bean
    public Binding absentBinding(Queue absentQueue, DirectExchange attendanceExchange) {
        return BindingBuilder.bind(absentQueue).to(attendanceExchange).with("");
    }

    // ===== Message Converter =====

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

package com.lqm.academic_service.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ===== Exchange =====
    public static final String ACADEMIC_EVENTS_EXCHANGE = "academic.events";
    public static final String EMAIL_EXCHANGE = "email.exchange";

    // ===== Routing Keys =====
    public static final String RK_SCORE_SYNC = "score.sync";
    public static final String RK_STUDENTS_REMOVED = "attendance.students-removed";
    public static final String RK_CLASSROOM_DELETED = "attendance.classroom-deleted";
    public static final String RK_CHAT_GROUP_CREATE = "chat.group.create";
    public static final String RK_CHAT_GROUP_UPDATE_TEACHER = "chat.group.update-teacher";
    public static final String RK_CHAT_GROUP_DELETE = "chat.group.delete";

    // ===== Queues =====
    public static final String QUEUE_SCORE_SYNC = "q.score.sync";
    public static final String QUEUE_ATTENDANCE_STUDENTS_REMOVED = "q.attendance.students-removed";
    public static final String QUEUE_ATTENDANCE_CLASSROOM_DELETED = "q.attendance.classroom-deleted";
    public static final String QUEUE_CHAT_GROUP_CREATE = "q.chat.group-create";
    public static final String QUEUE_CHAT_GROUP_UPDATE_TEACHER = "q.chat.group-update-teacher";
    public static final String QUEUE_CHAT_GROUP_DELETE = "q.chat.group-delete";
    public static final String QUEUE_EMAIL = "q.email";

    // ===== Exchanges =====

    @Bean
    public TopicExchange academicEventsExchange() {
        return new TopicExchange(ACADEMIC_EVENTS_EXCHANGE);
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    // ===== Queues =====

    @Bean
    public Queue scoreSyncQueue() {
        return QueueBuilder.durable(QUEUE_SCORE_SYNC).build();
    }

    @Bean
    public Queue attendanceStudentsRemovedQueue() {
        return QueueBuilder.durable(QUEUE_ATTENDANCE_STUDENTS_REMOVED).build();
    }

    @Bean
    public Queue attendanceClassroomDeletedQueue() {
        return QueueBuilder.durable(QUEUE_ATTENDANCE_CLASSROOM_DELETED).build();
    }

    @Bean
    public Queue chatGroupCreateQueue() {
        return QueueBuilder.durable(QUEUE_CHAT_GROUP_CREATE).build();
    }

    @Bean
    public Queue chatGroupUpdateTeacherQueue() {
        return QueueBuilder.durable(QUEUE_CHAT_GROUP_UPDATE_TEACHER).build();
    }

    @Bean
    public Queue chatGroupDeleteQueue() {
        return QueueBuilder.durable(QUEUE_CHAT_GROUP_DELETE).build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL).build();
    }

    // ===== Bindings: academic.events exchange =====

    @Bean
    public Binding scoreSyncBinding(Queue scoreSyncQueue, TopicExchange academicEventsExchange) {
        return BindingBuilder.bind(scoreSyncQueue).to(academicEventsExchange).with(RK_SCORE_SYNC);
    }

    @Bean
    public Binding attendanceStudentsRemovedBinding(Queue attendanceStudentsRemovedQueue, TopicExchange academicEventsExchange) {
        return BindingBuilder.bind(attendanceStudentsRemovedQueue).to(academicEventsExchange).with(RK_STUDENTS_REMOVED);
    }

    @Bean
    public Binding attendanceClassroomDeletedBinding(Queue attendanceClassroomDeletedQueue, TopicExchange academicEventsExchange) {
        return BindingBuilder.bind(attendanceClassroomDeletedQueue).to(academicEventsExchange).with(RK_CLASSROOM_DELETED);
    }

    @Bean
    public Binding chatGroupCreateBinding(Queue chatGroupCreateQueue, TopicExchange academicEventsExchange) {
        return BindingBuilder.bind(chatGroupCreateQueue).to(academicEventsExchange).with(RK_CHAT_GROUP_CREATE);
    }

    @Bean
    public Binding chatGroupUpdateTeacherBinding(Queue chatGroupUpdateTeacherQueue, TopicExchange academicEventsExchange) {
        return BindingBuilder.bind(chatGroupUpdateTeacherQueue).to(academicEventsExchange).with(RK_CHAT_GROUP_UPDATE_TEACHER);
    }

    @Bean
    public Binding chatGroupDeleteBinding(Queue chatGroupDeleteQueue, TopicExchange academicEventsExchange) {
        return BindingBuilder.bind(chatGroupDeleteQueue).to(academicEventsExchange).with(RK_CHAT_GROUP_DELETE);
    }

    // ===== Binding: email exchange =====

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with("");
    }

    // ===== Message Converter =====

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

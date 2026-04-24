package com.lqm.attendance_service.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình STOMP WebSocket cho real-time notifications.
 * Endpoint: /ws (SockJS fallback)
 * Subscribe prefix: /topic
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Value("${ws.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Client subscribe prefix: /topic/devices, /topic/attendances
        config.enableSimpleBroker("/topic");
        // Client send prefix (không dùng trong trường hợp này nhưng cần set)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        // STOMP endpoint - SockJS fallback cho browser cũ
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(origins)
                .withSockJS()
                .setSuppressCors(true);
        // STOMP endpoint không SockJS - cho native WebSocket client
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(origins);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Interceptor xác thực JWT khi STOMP CONNECT
        registration.interceptors(webSocketAuthInterceptor);
    }
}

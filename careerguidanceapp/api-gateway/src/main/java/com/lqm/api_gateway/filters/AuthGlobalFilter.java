package com.lqm.api_gateway.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.api_gateway.utils.JwtUtil;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;
    private final List<String> ignoredPaths;

    // Tự viết Constructor như thế này:
    public AuthGlobalFilter(
            ObjectMapper objectMapper,
            @Value("${auth.gateway.security.ignored-paths:}") List<String> ignoredPaths) {
        this.objectMapper = objectMapper;
        this.ignoredPaths = ignoredPaths;
    }
    @Override
    @Nonnull
    public Mono<Void> filter(ServerWebExchange exchange, @Nonnull GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (ignoredPaths.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Auth header");
        }

        String token = authHeader.substring(7);

        Map<String, String> data;
        try {
            data = JwtUtil.validateToken(token);
        } catch (Exception e) {
            return onError(exchange, "Invalid Token");
        }

        if (data == null) {
            return onError(exchange, "Invalid Token or Expired");
        }

        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Email", data.get("email"))
                .header("X-User-Role", data.get("role"))
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errDetail) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorBody = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "message", errDetail
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorBody);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return response.setComplete();
        }
    }
}
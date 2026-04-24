package com.lqm.admin_service.configs;

import io.github.resilience4j.core.ContextPropagator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class SecurityContextPropagator implements ContextPropagator<SecurityContext> {

    @Override
    public Supplier<Optional<SecurityContext>> retrieve() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext());
    }

    @Override
    public Consumer<Optional<SecurityContext>> copy() {
        return securityContext -> {
            if (securityContext.isPresent()) {
                SecurityContextHolder.setContext(securityContext.get());
            } else {
                SecurityContextHolder.clearContext();
            }
        };
    }

    @Override
    public Consumer<Optional<SecurityContext>> clear() {
        return securityContext -> SecurityContextHolder.clearContext();
    }
}

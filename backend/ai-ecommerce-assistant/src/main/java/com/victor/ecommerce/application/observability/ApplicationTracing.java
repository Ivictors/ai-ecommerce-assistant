package com.victor.ecommerce.application.observability;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.function.Supplier;

@ApplicationScoped
public class ApplicationTracing {

    private final Tracer tracer;

    @Inject
    public ApplicationTracing(Instance<Tracer> tracer) {
        this.tracer = tracer.isResolvable() ? tracer.get() : OpenTelemetry.noop().getTracer("ai-ecommerce-assistant");
    }

    public ApplicationTracing(Tracer tracer) {
        this.tracer = tracer;
    }

    public <T> T trace(String spanName, String operation, Supplier<T> action) {
        Span span = tracer.spanBuilder(spanName)
                .setAttribute("ecommerce.operation", operation)
                .startSpan();
        try {
            T result = action.get();
            span.setAttribute("ecommerce.result", "success");
            return result;
        } catch (RuntimeException exception) {
            span.setAttribute("ecommerce.result", "failure");
            span.recordException(exception);
            span.setStatus(StatusCode.ERROR);
            throw exception;
        } finally {
            span.end();
        }
    }
}

package com.victor.ecommerce.application.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationTracingTest {

    private final Tracer tracer = OpenTelemetry.noop().getTracer("test");
    private final ApplicationTracing tracing = new ApplicationTracing(tracer);

    @Test
    void executesOperationWithNoopTracerWithoutNetworkDependency() {
        assertEquals("result", tracing.trace("test.operation", "unit-test", () -> "result"));
    }

    @Test
    void preservesFailuresWithNoopTracer() {
        assertThrows(IllegalStateException.class,
                () -> tracing.trace("test.operation", "unit-test", () -> {
                    throw new IllegalStateException("expected");
                }));
    }
}

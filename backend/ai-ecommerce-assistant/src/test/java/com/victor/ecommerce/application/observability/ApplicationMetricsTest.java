package com.victor.ecommerce.application.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationMetricsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final ApplicationMetrics metrics = new ApplicationMetrics(registry);

    @Test
    void recordsSuccessfulNvidiaCallAndLatencyWithoutDynamicSensitiveTags() {
        assertEquals("answer", metrics.recordNvidiaCall("chat", () -> "answer"));

        assertEquals(1.0, registry.get("ecommerce.nvidia.calls")
                .tag("operation", "chat").tag("result", "success").counter().count());
        assertEquals(1, registry.get("ecommerce.nvidia.latency")
                .tag("operation", "chat").timer().count());
        assertEquals(2, registry.get("ecommerce.nvidia.calls").counter().getId().getTags().size());
    }

    @Test
    void recordsFailedNvidiaCallAndLatency() {
        assertThrows(IllegalStateException.class,
                () -> metrics.recordNvidiaCall("classification", () -> {
                    throw new IllegalStateException("provider failure");
                }));

        assertEquals(1.0, registry.get("ecommerce.nvidia.calls")
                .tag("operation", "classification").tag("result", "failure").counter().count());
        assertEquals(1, registry.get("ecommerce.nvidia.latency")
                .tag("operation", "classification").timer().count());
    }

    @Test
    void recordsApplicationSignalsWithBoundedValues() {
        metrics.recordChatRequest("TEXT");
        metrics.recordRagProcessing("success");
        metrics.recordAuthenticationFailure();
        metrics.recordAuthorizationDenial();

        assertEquals(1.0, registry.get("ecommerce.chat.requests").tag("result", "TEXT").counter().count());
        assertEquals(1.0, registry.get("ecommerce.rag.documents.processed")
                .tag("result", "success").counter().count());
        assertEquals(1.0, registry.get("ecommerce.authentication.failures").counter().count());
        assertEquals(1.0, registry.get("ecommerce.authorization.denials").counter().count());
    }
}

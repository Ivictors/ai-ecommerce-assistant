package com.victor.ecommerce.application.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.function.Supplier;

@ApplicationScoped
public class ApplicationMetrics {

    private final MeterRegistry registry;

    @Inject
    public ApplicationMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordChatRequest(String result) {
        counter("ecommerce.chat.requests", "Chat requests", "result", result).increment();
    }

    public <T> T recordNvidiaCall(String operation, Supplier<T> action) {
        Timer.Sample sample = Timer.start(registry);
        try {
            T result = action.get();
            counter("ecommerce.nvidia.calls", "NVIDIA model calls", "operation", operation, "result", "success")
                    .increment();
            return result;
        } catch (RuntimeException exception) {
            counter("ecommerce.nvidia.calls", "NVIDIA model calls", "operation", operation, "result", "failure")
                    .increment();
            throw exception;
        } finally {
            sample.stop(timer("ecommerce.nvidia.latency", "NVIDIA model call latency", "operation", operation));
        }
    }

    public void recordRagProcessing(String result) {
        counter("ecommerce.rag.documents.processed", "RAG document processing", "result", result).increment();
    }

    public void recordAuthenticationFailure() {
        counter("ecommerce.authentication.failures", "Authentication failures").increment();
    }

    public void recordAuthorizationDenial() {
        counter("ecommerce.authorization.denials", "Authorization denials").increment();
    }

    private Counter counter(String name, String description, String... tags) {
        return Counter.builder(name).description(description).tags(tags).register(registry);
    }

    private Timer timer(String name, String description, String... tags) {
        return Timer.builder(name).description(description).tags(tags).register(registry);
    }
}

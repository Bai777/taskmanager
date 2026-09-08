package com.javarush.baymakov.taskmanager.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskMetrics {
    private final MeterRegistry meterRegistry;
    private Counter taskCreatedCounter;

    @PostConstruct
    public void init() {
        taskCreatedCounter = Counter.builder("tasks.created")
                .description("Number of tasks created")
                .register(meterRegistry);
    }

    public void incrementTaskCreated() {
        taskCreatedCounter.increment();
    }
}

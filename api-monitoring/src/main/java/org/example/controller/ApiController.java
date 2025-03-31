package org.example.controller;

import org.example.model.ApiLog;
import org.example.service.kafkaProducerService;
import org.example.repository.ApiLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.instrument.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final kafkaProducerService kafkaProducerService;
    private final ApiLogRepository apiLogRepository;
    private final MeterRegistry meterRegistry;
    private final Counter apiErrors;

    public ApiController(kafkaProducerService kafkaProducerService,
                         ApiLogRepository apiLogRepository,
                         MeterRegistry meterRegistry) {
        this.kafkaProducerService = kafkaProducerService;
        this.apiLogRepository = apiLogRepository;
        this.meterRegistry = meterRegistry;
        this.apiErrors = Counter.builder("api_errors")
                .tag("service", "api-monitoring")
                .register(meterRegistry);
    }

    @GetMapping("/process")
    public ResponseEntity<Map<String, String>> processApi() {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            long startTime = System.currentTimeMillis();

            // Original processing logic
            try {
                Thread.sleep((long) (Math.random() * 2000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long endTime = System.currentTimeMillis();
            String responseTime = String.valueOf(endTime - startTime);

            ApiLog log = new ApiLog("/api/process", Instant.now().toString(), responseTime);
            apiLogRepository.save(log);
            kafkaProducerService.sendLog(log);

            Map<String, String> response = new HashMap<>();
            response.put("message", "API processed successfully");
            response.put("apiEndpoint", log.getApiEndpoint());
            response.put("timestamp", log.getTimestamp());
            response.put("responseTime", log.getResponseTime() + " ms");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            apiErrors.increment();
            throw e;
        } finally {
            sample.stop(Timer.builder("api_response_time")
                    .tag("endpoint", "/api/process")
                    .register(meterRegistry));
        }
    }
}
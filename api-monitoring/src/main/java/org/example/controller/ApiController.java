package org.example.controller;

import org.example.model.ApiLog;
import org.example.service.kafkaProducerService;
import org.example.repository.ApiLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final kafkaProducerService kafkaProducerService;
    private final ApiLogRepository apiLogRepository;

    public ApiController(kafkaProducerService kafkaProducerService, ApiLogRepository apiLogRepository) {
        this.kafkaProducerService = kafkaProducerService;
        this.apiLogRepository = apiLogRepository;
    }

    @GetMapping("/process")
    public ResponseEntity<Map<String, String>> processApi() {

        long startTime = System.currentTimeMillis();

        // Simulate API processing (replace with actual logic)
        try {
            Thread.sleep((long) (Math.random() * 2000)); // Simulate random API delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        String responseTime = String.valueOf(endTime - startTime);

        // Create API log entry
        ApiLog log = new ApiLog("/api/process", Instant.now().toString(), responseTime);

        // Save to MongoDB
        apiLogRepository.save(log);

        // Send to Kafka
        kafkaProducerService.sendLog(log);

        // Prepare response body
        Map<String, String> response = new HashMap<>();
        response.put("message", "API processed successfully");
        response.put("apiEndpoint", log.getApiEndpoint());
        response.put("timestamp", log.getTimestamp());
        response.put("responseTime", log.getResponseTime() + " ms");

        return ResponseEntity.ok(response);
    }
}

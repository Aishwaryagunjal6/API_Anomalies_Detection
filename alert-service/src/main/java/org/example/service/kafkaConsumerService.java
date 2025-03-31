package org.example.service;



import org.example.model.ApiLog;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class kafkaConsumerService {

    @KafkaListener(topics = "api-logs", groupId = "alert-service-group")
    public void consumeLog(ApiLog log) {
        System.out.println("Received Log: " + log.getApiEndpoint() + " | Response Time: " + log.getResponseTime() + "ms");

        // Run Python anomaly detection script
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "detect_anomaly.py",
                    String.valueOf(log.getResponseTime()));
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();

            if ("anomaly".equals(result)) {
                System.out.println("⚠ ALERT: Anomalous API Behavior Detected!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



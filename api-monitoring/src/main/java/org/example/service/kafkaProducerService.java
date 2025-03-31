package org.example.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.example.model.ApiLog;

@Service
public class kafkaProducerService {

    @Autowired
    private KafkaTemplate<String, ApiLog> kafkaTemplate;

    private static final String TOPIC = "api-logs";

    public void sendLog(ApiLog log) {
        kafkaTemplate.send("api-logs", log);
        System.out.println("Sent API Log to Kafka: " + log.getApiEndpoint() + " | Response Time: " + log.getResponseTime() + "ms");
    }
}


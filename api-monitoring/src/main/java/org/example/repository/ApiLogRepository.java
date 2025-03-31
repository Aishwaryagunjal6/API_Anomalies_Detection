package org.example.repository;



import org.example.model.ApiLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiLogRepository extends MongoRepository<ApiLog, String> {
}


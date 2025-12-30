package com.analytics.platform.controller;

import com.analytics.platform.model.AnalyticsEvent;
import com.analytics.platform.model.AnalyticsResult;
import com.analytics.platform.kafka.KafkaProducer;
import com.analytics.platform.service.ResultStorageService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@Slf4j
public class AnalyticsController {
    
    private final KafkaProducer kafkaProducer;
    private final ResultStorageService resultStorageService;
    
    public AnalyticsController(KafkaProducer kafkaProducer, 
                              ResultStorageService resultStorageService) {
        this.kafkaProducer = kafkaProducer;
        this.resultStorageService = resultStorageService;
    }
    
    @PostMapping("/events")
    public ResponseEntity<String> submitEvent(@Valid @RequestBody AnalyticsEvent event) {
        try {
            // Generate event ID if not provided
            if (event.getEventId() == null || event.getEventId().isEmpty()) {
                event.setEventId(UUID.randomUUID().toString());
            }
            
            // Send to Kafka
            kafkaProducer.sendEvent(event);
            
            log.info("Event submitted: {}", event.getEventId());
            return ResponseEntity.accepted()
                .body("Event submitted successfully. Event ID: " + event.getEventId());
        } catch (Exception e) {
            log.error("Error submitting event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error submitting event: " + e.getMessage());
        }
    }
    
    @GetMapping("/results/{eventId}")
    public ResponseEntity<AnalyticsResult> getResult(@PathVariable String eventId) {
        Optional<AnalyticsResult> result = resultStorageService.getResult(eventId);
        return result.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/results")
    public ResponseEntity<List<AnalyticsResult>> getAllResults() {
        List<AnalyticsResult> results = resultStorageService.getAllResults();
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/results/my")
    public ResponseEntity<List<AnalyticsResult>> getMyResults() {
        List<AnalyticsResult> results = resultStorageService.getUserResults();
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/results/prediction/{prediction}")
    public ResponseEntity<List<AnalyticsResult>> getResultsByPrediction(
            @PathVariable String prediction) {
        List<AnalyticsResult> results = resultStorageService.getResultsByPrediction(prediction);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics Platform is running");
    }
}


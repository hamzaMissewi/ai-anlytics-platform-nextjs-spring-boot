package com.analytics.platform.kafka;

import com.analytics.platform.model.AnalyticsEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class KafkaProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${kafka.topic.events:analytics-events}")
    private String eventsTopic;
    
    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    public void sendEvent(AnalyticsEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(eventsTopic, event.getEventId(), eventJson);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Sent event [{}] to topic [{}] with offset [{}]",
                        event.getEventId(), eventsTopic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send event [{}] to topic [{}]",
                        event.getEventId(), eventsTopic, exception);
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Error serializing event to JSON", e);
        }
    }
}


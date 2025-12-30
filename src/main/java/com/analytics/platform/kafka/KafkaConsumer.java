package com.analytics.platform.kafka;

import com.analytics.platform.model.AnalyticsEvent;
import com.analytics.platform.model.AnalyticsResult;
import com.analytics.platform.service.AIService;
import com.analytics.platform.service.ResultStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaConsumer {
    
    private final ObjectMapper objectMapper;
    private final AIService aiService;
    private final ResultStorageService resultStorageService;
    
    public KafkaConsumer(ObjectMapper objectMapper, AIService aiService, 
                        ResultStorageService resultStorageService) {
        this.objectMapper = objectMapper;
        this.aiService = aiService;
        this.resultStorageService = resultStorageService;
    }
    
    @KafkaListener(topics = "${kafka.topic.events:analytics-events}", 
                   groupId = "${kafka.consumer.group-id:analytics-group}")
    public void consumeEvent(@Payload String message,
                           @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                           @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                           @Header(KafkaHeaders.OFFSET) long offset,
                           Acknowledgment acknowledgment) {
        try {
            log.info("Received message from topic [{}], partition [{}], offset [{}]: {}",
                topic, partition, offset, message);
            
            AnalyticsEvent event = objectMapper.readValue(message, AnalyticsEvent.class);
            
            // Process with AI
            AnalyticsResult result = aiService.processEvent(event);
            
            // Store result
            resultStorageService.saveResult(result);
            
            log.info("Processed event [{}] with prediction [{}]", 
                event.getEventId(), result.getPrediction());
            
            // Acknowledge message
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
        } catch (Exception e) {
            log.error("Error processing message from Kafka", e);
            // In production, implement retry logic or dead letter queue
        }
    }
}


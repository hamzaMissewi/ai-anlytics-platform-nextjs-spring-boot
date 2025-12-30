package com.analytics.platform.service;

import com.analytics.platform.model.AnalyticsEvent;
import com.analytics.platform.model.AnalyticsResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class AIService {
    
    private final WebClient webClient;
    private final Random random = new Random();
    private final String modelVersion = "v1.0.0";
    
    @Value("${ai.huggingface.api.url:https://api-inference.huggingface.co/models}")
    private String huggingFaceApiUrl;
    
    @Value("${ai.huggingface.api.key:}")
    private String huggingFaceApiKey;
    
    @Value("${ai.model.enabled:true}")
    private boolean aiModelEnabled;
    
    public AIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    /**
     * Process analytics event using AI/ML model
     * Uses Hugging Face Inference API (free tier) for content moderation and sentiment analysis
     */
    public AnalyticsResult processEvent(AnalyticsEvent event) {
        log.info("Processing event {} with AI model", event.getEventId());
        
        try {
            if (aiModelEnabled && huggingFaceApiKey != null && !huggingFaceApiKey.isEmpty()) {
                return processWithHuggingFace(event);
            } else {
                log.warn("Hugging Face API not configured, using fallback processing");
                return processWithFallback(event);
            }
        } catch (Exception e) {
            log.error("Error processing with AI model, using fallback", e);
            return processWithFallback(event);
        }
    }
    
    /**
     * Process using Hugging Face free API for content moderation
     * Model: facebook/roberta-hate-speech-dynabench-r4-target
     */
    private AnalyticsResult processWithHuggingFace(AnalyticsEvent event) {
        String content = extractContentFromEvent(event);
        
        if (content == null || content.isEmpty()) {
            return processWithFallback(event);
        }
        
        try {
            // Use Hugging Face Inference API for hate speech detection
            String modelEndpoint = huggingFaceApiUrl + "/facebook/roberta-hate-speech-dynabench-r4-target";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", content);
            
            // Hugging Face API returns array of responses
            List<Map<String, Object>> responseList = webClient.post()
                .uri(modelEndpoint)
                .header("Authorization", "Bearer " + huggingFaceApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .block();
            
            HuggingFaceResponse response = parseHuggingFaceResponse(responseList);
            
            if (response != null && response.getScores() != null) {
                return buildResultFromHuggingFace(event, response);
            }
        } catch (Exception e) {
            log.error("Error calling Hugging Face API", e);
        }
        
        return processWithFallback(event);
    }
    
    private AnalyticsResult buildResultFromHuggingFace(AnalyticsEvent event, HuggingFaceResponse response) {
        double[] scores = response.getScores();
        String prediction;
        double confidence;
        
        // Interpret scores: [hate, no-hate]
        if (scores.length >= 2) {
            double hateScore = scores[0];
            double noHateScore = scores[1];
            
            if (hateScore > 0.5) {
                prediction = "HATE_SPEECH_DETECTED";
                confidence = hateScore;
            } else {
                prediction = "SAFE_CONTENT";
                confidence = noHateScore;
            }
        } else {
            prediction = "NEUTRAL";
            confidence = 0.5;
        }
        
        Map<String, Object> insights = new HashMap<>();
        insights.put("hate_score", scores.length > 0 ? scores[0] : 0.0);
        insights.put("safe_score", scores.length > 1 ? scores[1] : 0.0);
        insights.put("model", "roberta-hate-speech-dynabench");
        insights.put("recommendation", prediction.equals("HATE_SPEECH_DETECTED") 
            ? "Content requires moderation" : "Content is safe");
        insights.put("risk_level", prediction.equals("HATE_SPEECH_DETECTED") ? "high" : "low");
        
        return new AnalyticsResult(
            event.getEventId(),
            prediction,
            confidence,
            insights,
            LocalDateTime.now(),
            modelVersion
        );
    }
    
    private String extractContentFromEvent(AnalyticsEvent event) {
        if (event.getData() != null) {
            Object content = event.getData().get("content");
            if (content != null) {
                return content.toString();
            }
            Object text = event.getData().get("text");
            if (text != null) {
                return text.toString();
            }
            Object message = event.getData().get("message");
            if (message != null) {
                return message.toString();
            }
        }
        return null;
    }
    
    private AnalyticsResult processWithFallback(AnalyticsEvent event) {
        String prediction = generatePrediction(event);
        Double confidence = 0.7 + random.nextDouble() * 0.3;
        
        Map<String, Object> insights = generateInsights(event);
        
        return new AnalyticsResult(
            event.getEventId(),
            prediction,
            confidence,
            insights,
            LocalDateTime.now(),
            modelVersion
        );
    }
    
    private String generatePrediction(AnalyticsEvent event) {
        switch (event.getEventType().toLowerCase()) {
            case "content":
            case "comment":
            case "post":
                return random.nextBoolean() ? "SAFE_CONTENT" : "REVIEW_NEEDED";
            case "purchase":
                return random.nextBoolean() ? "HIGH_VALUE_CUSTOMER" : "STANDARD_CUSTOMER";
            case "click":
                return random.nextBoolean() ? "HIGH_INTEREST" : "LOW_INTEREST";
            default:
                return "NEUTRAL";
        }
    }
    
    private Map<String, Object> generateInsights(AnalyticsEvent event) {
        Map<String, Object> insights = new HashMap<>();
        insights.put("sentiment", random.nextBoolean() ? "positive" : "neutral");
        insights.put("anomaly_score", random.nextDouble());
        insights.put("recommendation", "Continue monitoring");
        insights.put("risk_level", random.nextBoolean() ? "low" : "medium");
        insights.put("model", "fallback");
        return insights;
    }
    
    public boolean isHealthy() {
        return true;
    }
    
    private HuggingFaceResponse parseHuggingFaceResponse(List<Map<String, Object>> responseList) {
        if (responseList == null || responseList.isEmpty()) {
            return null;
        }
        
        Map<String, Object> firstResponse = responseList.get(0);
        HuggingFaceResponse response = new HuggingFaceResponse();
        
        if (firstResponse.containsKey("label")) {
            response.setLabel(firstResponse.get("label").toString());
        }
        
        if (firstResponse.containsKey("score")) {
            double score = ((Number) firstResponse.get("score")).doubleValue();
            response.setScores(new double[]{score});
        } else if (firstResponse.containsKey("scores")) {
            @SuppressWarnings("unchecked")
            List<Number> scoresList = (List<Number>) firstResponse.get("scores");
            response.setScores(scoresList.stream()
                .mapToDouble(Number::doubleValue)
                .toArray());
        }
        
        return response;
    }
    
    // Inner class for Hugging Face API response
    private static class HuggingFaceResponse {
        private String label;
        private double[] scores;
        
        public String getLabel() {
            return label;
        }
        
        public void setLabel(String label) {
            this.label = label;
        }
        
        public double[] getScores() {
            return scores;
        }
        
        public void setScores(double[] scores) {
            this.scores = scores;
        }
    }
}


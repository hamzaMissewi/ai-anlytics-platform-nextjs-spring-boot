package com.analytics.platform.service;

import com.analytics.platform.entity.AnalyticsResultEntity;
import com.analytics.platform.model.AnalyticsResult;
import com.analytics.platform.repository.AnalyticsResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultStorageService {
    
    private final AnalyticsResultRepository repository;
    private final UserService userService;
    
    @Transactional
    public void saveResult(AnalyticsResult result) {
        AnalyticsResultEntity entity = convertToEntity(result);
        
        // Associate with current user if authenticated
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                entity.setUser(userService.findByUsername(auth.getName()));
            }
        } catch (Exception e) {
            log.warn("Could not associate result with user", e);
        }
        
        repository.save(entity);
        log.info("Saved analytics result for event [{}]", result.getEventId());
    }
    
    public Optional<AnalyticsResult> getResult(String eventId) {
        return repository.findByEventId(eventId)
            .map(this::convertToModel);
    }
    
    public List<AnalyticsResult> getAllResults() {
        return repository.findAll().stream()
            .map(this::convertToModel)
            .collect(Collectors.toList());
    }
    
    public List<AnalyticsResult> getResultsByPrediction(String prediction) {
        return repository.findByPrediction(prediction).stream()
            .map(this::convertToModel)
            .collect(Collectors.toList());
    }
    
    public List<AnalyticsResult> getUserResults() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                var user = userService.findByUsername(auth.getName());
                return repository.findByUserId(user.getId()).stream()
                    .map(this::convertToModel)
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Could not get user results", e);
        }
        return List.of();
    }
    
    private AnalyticsResultEntity convertToEntity(AnalyticsResult result) {
        AnalyticsResultEntity entity = new AnalyticsResultEntity();
        entity.setEventId(result.getEventId());
        entity.setPrediction(result.getPrediction());
        entity.setConfidence(result.getConfidence());
        entity.setProcessedAt(result.getProcessedAt());
        entity.setModelVersion(result.getModelVersion());
        
        // Convert insights Map<String, Object> to Map<String, String>
        if (result.getInsights() != null) {
            Map<String, String> insights = result.getInsights().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue() != null ? e.getValue().toString() : ""
                ));
            entity.setInsights(insights);
        }
        
        return entity;
    }
    
    private AnalyticsResult convertToModel(AnalyticsResultEntity entity) {
        Map<String, Object> insights = entity.getInsights() != null 
            ? entity.getInsights().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            : Map.of();
        
        return new AnalyticsResult(
            entity.getEventId(),
            entity.getPrediction(),
            entity.getConfidence(),
            insights,
            entity.getProcessedAt(),
            entity.getModelVersion()
        );
    }
}


package com.analytics.platform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResult {
    private String eventId;
    private String prediction;
    private Double confidence;
    private Map<String, Object> insights;
    private LocalDateTime processedAt;
    private String modelVersion;
}


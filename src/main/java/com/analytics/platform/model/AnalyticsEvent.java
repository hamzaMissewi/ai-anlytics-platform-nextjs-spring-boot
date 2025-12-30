package com.analytics.platform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
    private String userId;
    private String source;
}


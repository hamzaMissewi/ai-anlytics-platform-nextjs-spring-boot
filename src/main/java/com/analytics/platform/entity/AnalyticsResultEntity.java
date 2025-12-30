package com.analytics.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "analytics_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResultEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String eventId;
    
    @Column(nullable = false)
    private String prediction;
    
    @Column(nullable = false)
    private Double confidence;
    
    @ElementCollection
    @CollectionTable(name = "analytics_insights", joinColumns = @JoinColumn(name = "result_id"))
    @MapKeyColumn(name = "insight_key")
    @Column(name = "insight_value")
    private Map<String, String> insights = new HashMap<>();
    
    @Column(nullable = false)
    private LocalDateTime processedAt;
    
    @Column(nullable = false)
    private String modelVersion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}

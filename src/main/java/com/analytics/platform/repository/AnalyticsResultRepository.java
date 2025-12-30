package com.analytics.platform.repository;

import com.analytics.platform.entity.AnalyticsResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsResultRepository extends JpaRepository<AnalyticsResultEntity, Long> {
    Optional<AnalyticsResultEntity> findByEventId(String eventId);
    List<AnalyticsResultEntity> findByPrediction(String prediction);
    List<AnalyticsResultEntity> findByUserId(Long userId);
}


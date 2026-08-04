package com.bp.decline.persistence.repository;

import com.bp.decline.persistence.entity.DeclineForecastEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeclineForecastRepository extends JpaRepository<DeclineForecastEntity, Long> {

    List<DeclineForecastEntity> findByWellIdOrderByComputedAtDesc(Long wellId);
}

package com.bp.decline.core.dto.production;

import com.bp.decline.persistence.entity.ProductionRecordEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductionRecordResponse(
        Long id,
        Long wellId,
        LocalDate recordDate,
        BigDecimal productionRate,
        LocalDateTime createdAt) {

    public static ProductionRecordResponse from(ProductionRecordEntity entity) {
        return new ProductionRecordResponse(
                entity.getId(),
                entity.getWell().getId(),
                entity.getRecordDate(),
                entity.getProductionRate(),
                entity.getCreatedAt());
    }
}

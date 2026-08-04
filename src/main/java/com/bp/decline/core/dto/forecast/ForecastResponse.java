package com.bp.decline.core.dto.forecast;

import com.bp.decline.core.enums.DeclineType;
import com.bp.decline.persistence.entity.DeclineForecastEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ForecastResponse(
        Long forecastId,
        Long wellId,
        String wellName,
        DeclineType declineType,
        BigDecimal initialRate,
        BigDecimal initialDeclineRate,
        BigDecimal bFactor,
        int forecastMonths,
        BigDecimal economicLimit,
        BigDecimal eur,
        LocalDateTime computedAt,
        List<ForecastDataPoint> timeSeries) {

    public static ForecastResponse from(DeclineForecastEntity entity, List<ForecastDataPoint> timeSeries) {
        return new ForecastResponse(
                entity.getId(),
                entity.getWell().getId(),
                entity.getWell().getName(),
                entity.getDeclineType(),
                entity.getInitialRate(),
                entity.getInitialDeclineRate(),
                entity.getBFactor(),
                entity.getForecastMonths(),
                entity.getEconomicLimit(),
                entity.getEur(),
                entity.getComputedAt(),
                timeSeries);
    }
}

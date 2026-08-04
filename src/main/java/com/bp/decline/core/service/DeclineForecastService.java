package com.bp.decline.core.service;

import com.bp.decline.common.config.ClockConfig;
import com.bp.decline.common.exception.ResourceNotFoundException;
import com.bp.decline.core.dto.forecast.ForecastDataPoint;
import com.bp.decline.core.dto.forecast.ForecastRequest;
import com.bp.decline.core.dto.forecast.ForecastResponse;
import com.bp.decline.core.logic.ArpsDeclineEngine;
import com.bp.decline.persistence.entity.DeclineForecastEntity;
import com.bp.decline.persistence.entity.WellEntity;
import com.bp.decline.persistence.repository.DeclineForecastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DeclineForecastService {

    private final DeclineForecastRepository declineForecastRepository;
    private final WellService wellService;
    private final ArpsDeclineEngine arpsDeclineEngine;

    /**
     * Computes an Arps decline forecast, persists the model parameters and EUR,
     * and returns the full time-series.
     */
    @Transactional
    public ForecastResponse computeAndSave(Long wellId, ForecastRequest request) {
        WellEntity well = wellService.findEntity(wellId);

        double qi = request.initialRate().doubleValue();
        double di = request.initialDeclineRate().doubleValue();
        double b  = request.bFactor().doubleValue();
        Double economicLimit = request.economicLimit() != null
                ? request.economicLimit().doubleValue() : null;

        // Validate b-factor against the requested decline type
        validateBFactor(request);

        // Compute EUR
        double eur = arpsDeclineEngine.computeEur(
                request.declineType(), qi, di, b, economicLimit, request.forecastMonths());

        // Compute monthly time-series starting from today
        LocalDate startDate = LocalDate.now(ClockConfig.APPLICATION_ZONE_ID);
        List<ForecastDataPoint> timeSeries = arpsDeclineEngine.generateTimeSeries(
                request.declineType(), qi, di, b,
                request.forecastMonths(), economicLimit, startDate);

        // Persist forecast parameters
        DeclineForecastEntity entity = new DeclineForecastEntity(
                well,
                request.declineType(),
                request.initialRate(),
                request.initialDeclineRate(),
                request.bFactor(),
                request.forecastMonths(),
                request.economicLimit(),
                BigDecimal.valueOf(eur).setScale(4, RoundingMode.HALF_UP),
                LocalDateTime.now(ClockConfig.applicationClock()));

        DeclineForecastEntity saved = declineForecastRepository.save(entity);
        return ForecastResponse.from(saved, timeSeries);
    }

    /**
     * Returns all previously saved forecasts for a well (summary only, no time-series re-computation).
     */
    @Transactional(readOnly = true)
    public List<ForecastResponse> listByWell(Long wellId) {
        wellService.findEntity(wellId); // validate well exists
        return declineForecastRepository.findByWellIdOrderByComputedAtDesc(wellId).stream()
                .map(entity -> ForecastResponse.from(entity, List.of()))
                .toList();
    }

    /**
     * Re-computes the time-series for a previously saved forecast.
     */
    @Transactional(readOnly = true)
    public ForecastResponse get(Long wellId, Long forecastId) {
        DeclineForecastEntity entity = declineForecastRepository.findById(forecastId)
                .orElseThrow(() -> new ResourceNotFoundException("Forecast not found: " + forecastId));

        if (!entity.getWell().getId().equals(wellId)) {
            throw new ResourceNotFoundException(
                    "Forecast " + forecastId + " does not belong to well " + wellId);
        }

        double qi = entity.getInitialRate().doubleValue();
        double di = entity.getInitialDeclineRate().doubleValue();
        double b  = entity.getBFactor().doubleValue();
        Double economicLimit = entity.getEconomicLimit() != null
                ? entity.getEconomicLimit().doubleValue() : null;

        LocalDate startDate = entity.getCreatedAt().toLocalDate();
        List<ForecastDataPoint> timeSeries = arpsDeclineEngine.generateTimeSeries(
                entity.getDeclineType(), qi, di, b,
                entity.getForecastMonths(), economicLimit, startDate);

        return ForecastResponse.from(entity, timeSeries);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private void validateBFactor(ForecastRequest request) {
        double b = request.bFactor().doubleValue();
        switch (request.declineType()) {
            case EXPONENTIAL -> {
                if (b != 0.0) {
                    throw new IllegalArgumentException(
                            "b-factor must be 0 for EXPONENTIAL decline. Provided: " + b);
                }
            }
            case HARMONIC -> {
                if (b != 1.0) {
                    throw new IllegalArgumentException(
                            "b-factor must be 1 for HARMONIC decline. Provided: " + b);
                }
            }
            case HYPERBOLIC -> {
                if (b <= 0.0 || b >= 1.0) {
                    throw new IllegalArgumentException(
                            "b-factor must be strictly between 0 and 1 for HYPERBOLIC decline. Provided: " + b);
                }
            }
        }
    }
}

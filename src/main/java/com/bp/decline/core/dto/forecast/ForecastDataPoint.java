package com.bp.decline.core.dto.forecast;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single point on the forecast time-series.
 *
 * @param month          Month number from start (1, 2, 3 … forecastMonths)
 * @param date           Calendar date of this forecast point
 * @param productionRate Forecast rate q(t) in bbl/day or Mscf/day
 * @param cumulativeProduction Cumulative production Np from t=0 to this point
 * @param belowEconomicLimit True when q(t) has fallen below the economic limit
 */
public record ForecastDataPoint(
        int month,
        LocalDate date,
        BigDecimal productionRate,
        BigDecimal cumulativeProduction,
        boolean belowEconomicLimit) {
}

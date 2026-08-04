package com.bp.decline.core.logic;

import com.bp.decline.core.dto.forecast.ForecastDataPoint;
import com.bp.decline.core.enums.DeclineType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Arps Decline Curve Engine — implements all three Arps (1945) decline models.
 *
 * <p><b>Exponential (b=0):</b><br>
 * q(t) = qi * exp(-Di * t)<br>
 * Np(t) = (qi - q(t)) / Di
 *
 * <p><b>Hyperbolic (0 &lt; b &lt; 1):</b><br>
 * q(t) = qi / (1 + b * Di * t)^(1/b)<br>
 * Np(t) = (qi^b / ((1-b) * Di)) * (qi^(1-b) - q(t)^(1-b))
 *
 * <p><b>Harmonic (b=1):</b><br>
 * q(t) = qi / (1 + Di * t)<br>
 * Np(t) = (qi / Di) * ln(qi / q(t))
 *
 * <p>Time unit: <b>days</b> (t is total elapsed days from first production).
 * Monthly timesteps are used (t increments by 30.44 days per month).
 */
@Component
public class ArpsDeclineEngine {

    private static final double DAYS_PER_MONTH = 30.4375;
    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_UP);
    private static final int SCALE = 4;

    /**
     * Generates a monthly production forecast time-series using the specified Arps model.
     *
     * @param declineType       EXPONENTIAL, HYPERBOLIC, or HARMONIC
     * @param qi                initial production rate (bbl/day or Mscf/day)
     * @param di                initial nominal decline rate (fraction/day)
     * @param b                 Arps curvature factor (0, 0&lt;b&lt;1, or 1)
     * @param forecastMonths    number of months to forecast
     * @param economicLimit     abandonment rate; null means no limit applied
     * @param startDate         calendar start date for the series
     * @return list of monthly {@link ForecastDataPoint}
     */
    public List<ForecastDataPoint> generateTimeSeries(
            DeclineType declineType,
            double qi, double di, double b,
            int forecastMonths,
            Double economicLimit,
            LocalDate startDate) {

        List<ForecastDataPoint> series = new ArrayList<>(forecastMonths);
        double cumulativeProduction = 0.0;

        for (int month = 1; month <= forecastMonths; month++) {
            double t = month * DAYS_PER_MONTH;           // elapsed days
            double q = computeRate(declineType, qi, di, b, t);
            double np = computeCumulative(declineType, qi, di, b, q);

            cumulativeProduction += np - (month == 1 ? 0 : computeCumulative(declineType, qi, di, b,
                    computeRate(declineType, qi, di, b, (month - 1) * DAYS_PER_MONTH)));

            // Clamp negative drift from floating point
            if (q < 0) q = 0;

            boolean belowLimit = economicLimit != null && q < economicLimit;
            LocalDate pointDate = startDate.plusMonths(month - 1L);

            series.add(new ForecastDataPoint(
                    month,
                    pointDate,
                    round(q),
                    round(Math.max(np, 0)),
                    belowLimit));

            if (belowLimit) break;  // stop forecasting past economic limit
        }

        return series;
    }

    /**
     * Computes Estimated Ultimate Recovery (EUR) — cumulative production from t=0 to economic limit.
     *
     * @param declineType decline model
     * @param qi          initial rate
     * @param di          initial decline rate (fraction/day)
     * @param b           b-factor
     * @param economicLimit abandonment rate; if null, EUR is computed at the end of forecastMonths
     * @param forecastMonths number of months to run if no economic limit
     * @return EUR in bbl or Mscf
     */
    public double computeEur(DeclineType declineType, double qi, double di, double b,
                             Double economicLimit, int forecastMonths) {
        double qLimit = (economicLimit != null && economicLimit > 0) ? economicLimit : 0;

        return switch (declineType) {
            case EXPONENTIAL -> {
                if (qLimit > 0) {
                    // EUR = (qi - q_limit) / Di
                    yield (qi - qLimit) / di;
                } else {
                    double tEnd = forecastMonths * DAYS_PER_MONTH;
                    double qEnd = computeRate(DeclineType.EXPONENTIAL, qi, di, b, tEnd);
                    yield (qi - qEnd) / di;
                }
            }
            case HYPERBOLIC -> {
                if (qLimit > 0) {
                    // EUR = (qi^b / ((1-b)*Di)) * (qi^(1-b) - qLimit^(1-b))
                    yield (Math.pow(qi, b) / ((1 - b) * di))
                            * (Math.pow(qi, 1 - b) - Math.pow(qLimit, 1 - b));
                } else {
                    double tEnd = forecastMonths * DAYS_PER_MONTH;
                    double qEnd = computeRate(DeclineType.HYPERBOLIC, qi, di, b, tEnd);
                    yield (Math.pow(qi, b) / ((1 - b) * di))
                            * (Math.pow(qi, 1 - b) - Math.pow(qEnd, 1 - b));
                }
            }
            case HARMONIC -> {
                if (qLimit > 0) {
                    // EUR = (qi / Di) * ln(qi / qLimit)
                    yield (qi / di) * Math.log(qi / qLimit);
                } else {
                    double tEnd = forecastMonths * DAYS_PER_MONTH;
                    double qEnd = computeRate(DeclineType.HARMONIC, qi, di, b, tEnd);
                    yield (qi / di) * Math.log(qi / qEnd);
                }
            }
        };
    }

    // -------------------------------------------------------------------------
    // Private: core Arps rate equations
    // -------------------------------------------------------------------------

    private double computeRate(DeclineType type, double qi, double di, double b, double t) {
        return switch (type) {
            case EXPONENTIAL -> qi * Math.exp(-di * t);
            case HYPERBOLIC  -> qi / Math.pow(1 + b * di * t, 1.0 / b);
            case HARMONIC    -> qi / (1 + di * t);
        };
    }

    private double computeCumulative(DeclineType type, double qi, double di, double b, double q) {
        return switch (type) {
            case EXPONENTIAL -> (qi - q) / di;
            case HYPERBOLIC  -> (Math.pow(qi, b) / ((1 - b) * di))
                    * (Math.pow(qi, 1 - b) - Math.pow(q, 1 - b));
            case HARMONIC    -> (qi / di) * Math.log(qi / Math.max(q, 0.001));
        };
    }

    private BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(SCALE, RoundingMode.HALF_UP);
    }
}

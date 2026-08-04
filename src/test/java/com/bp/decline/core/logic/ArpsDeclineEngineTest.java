package com.bp.decline.core.logic;

import com.bp.decline.core.dto.forecast.ForecastDataPoint;
import com.bp.decline.core.enums.DeclineType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ArpsDeclineEngineTest {

    private ArpsDeclineEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ArpsDeclineEngine();
    }

    // -------------------------------------------------------------------------
    // Exponential Decline Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Exponential: rate at t=0 equals initial rate")
    void exponential_rateAtTimeZero_equalsInitialRate() {
        List<ForecastDataPoint> series = engine.generateTimeSeries(
                DeclineType.EXPONENTIAL, 1000.0, 0.001, 0.0, 1, null, LocalDate.now());

        // Month 1 rate should be close to 1000 (slight decay over 30 days)
        double month1Rate = series.get(0).productionRate().doubleValue();
        assertThat(month1Rate).isLessThan(1000.0);
        assertThat(month1Rate).isGreaterThan(900.0);
    }

    @Test
    @DisplayName("Exponential: rates are strictly monotonically decreasing")
    void exponential_rates_areMonotonicallyDecreasing() {
        List<ForecastDataPoint> series = engine.generateTimeSeries(
                DeclineType.EXPONENTIAL, 3200.0, 0.002, 0.0, 24, null, LocalDate.now());

        for (int i = 1; i < series.size(); i++) {
            double prev = series.get(i - 1).productionRate().doubleValue();
            double curr = series.get(i).productionRate().doubleValue();
            assertThat(curr).isLessThan(prev);
        }
    }

    @Test
    @DisplayName("Exponential: forecast stops when rate drops below economic limit")
    void exponential_stopsAtEconomicLimit() {
        List<ForecastDataPoint> series = engine.generateTimeSeries(
                DeclineType.EXPONENTIAL, 1000.0, 0.005, 0.0, 120, 100.0, LocalDate.now());

        // Series should be shorter than 120 months (economic limit cuts it short)
        assertThat(series.size()).isLessThan(120);

        // Last point should be flagged as below economic limit
        ForecastDataPoint lastPoint = series.get(series.size() - 1);
        assertThat(lastPoint.belowEconomicLimit()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Hyperbolic Decline Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Hyperbolic: declines slower than exponential for same Di")
    void hyperbolic_declinesSlowerThanExponential() {
        double qi = 3200.0;
        double di = 0.002;

        List<ForecastDataPoint> expSeries = engine.generateTimeSeries(
                DeclineType.EXPONENTIAL, qi, di, 0.0, 24, null, LocalDate.now());
        List<ForecastDataPoint> hypSeries = engine.generateTimeSeries(
                DeclineType.HYPERBOLIC, qi, di, 0.5, 24, null, LocalDate.now());

        // Hyperbolic should have higher rates than exponential at every month
        for (int i = 0; i < 24; i++) {
            double expRate = expSeries.get(i).productionRate().doubleValue();
            double hypRate = hypSeries.get(i).productionRate().doubleValue();
            assertThat(hypRate).isGreaterThan(expRate);
        }
    }

    @Test
    @DisplayName("Hyperbolic: 24-month series has exactly 24 data points when no economic limit")
    void hyperbolic_seriesLength_equalsRequestedMonths() {
        List<ForecastDataPoint> series = engine.generateTimeSeries(
                DeclineType.HYPERBOLIC, 800.0, 0.001, 0.5, 24, null, LocalDate.now());

        assertThat(series).hasSize(24);
    }

    // -------------------------------------------------------------------------
    // Harmonic Decline Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Harmonic: declines slowest of all three models")
    void harmonic_isSlowerThanHyperbolicAndExponential() {
        double qi = 3200.0;
        double di = 0.002;

        List<ForecastDataPoint> expSeries = engine.generateTimeSeries(
                DeclineType.EXPONENTIAL, qi, di, 0.0, 36, null, LocalDate.now());
        List<ForecastDataPoint> hypSeries = engine.generateTimeSeries(
                DeclineType.HYPERBOLIC, qi, di, 0.5, 36, null, LocalDate.now());
        List<ForecastDataPoint> harSeries = engine.generateTimeSeries(
                DeclineType.HARMONIC, qi, di, 1.0, 36, null, LocalDate.now());

        // At month 36: harmonic > hyperbolic > exponential
        double expRate36 = expSeries.get(35).productionRate().doubleValue();
        double hypRate36 = hypSeries.get(35).productionRate().doubleValue();
        double harRate36 = harSeries.get(35).productionRate().doubleValue();

        assertThat(harRate36).isGreaterThan(hypRate36);
        assertThat(hypRate36).isGreaterThan(expRate36);
    }

    // -------------------------------------------------------------------------
    // EUR Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("EUR: exponential EUR is positive and proportional to qi/Di")
    void eur_exponential_isPositive() {
        double qi = 3200.0;
        double di = 0.002;

        double eur = engine.computeEur(DeclineType.EXPONENTIAL, qi, di, 0.0, 50.0, 120);

        // EUR = (qi - qLimit) / Di = (3200 - 50) / 0.002 = 1,575,000 bbl
        assertThat(eur).isCloseTo(1_575_000.0, within(1000.0));
    }

    @Test
    @DisplayName("EUR: higher economic limit results in lower EUR")
    void eur_higherEconomicLimit_givesLowerEur() {
        double qi = 3200.0;
        double di = 0.002;

        double eurLowLimit  = engine.computeEur(DeclineType.EXPONENTIAL, qi, di, 0.0, 10.0, 120);
        double eurHighLimit = engine.computeEur(DeclineType.EXPONENTIAL, qi, di, 0.0, 500.0, 120);

        assertThat(eurLowLimit).isGreaterThan(eurHighLimit);
    }

    @Test
    @DisplayName("Time-series: month numbers are sequential starting at 1")
    void timeSeries_monthNumbers_areSequential() {
        List<ForecastDataPoint> series = engine.generateTimeSeries(
                DeclineType.HYPERBOLIC, 1000.0, 0.001, 0.5, 12, null, LocalDate.of(2024, 1, 1));

        for (int i = 0; i < series.size(); i++) {
            assertThat(series.get(i).month()).isEqualTo(i + 1);
        }
    }

    @Test
    @DisplayName("Time-series: dates are sequential months from start date")
    void timeSeries_dates_areSequentialMonths() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        List<ForecastDataPoint> series = engine.generateTimeSeries(
                DeclineType.EXPONENTIAL, 1000.0, 0.001, 0.0, 6, null, start);

        assertThat(series.get(0).date()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(series.get(1).date()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(series.get(5).date()).isEqualTo(LocalDate.of(2024, 6, 1));
    }
}

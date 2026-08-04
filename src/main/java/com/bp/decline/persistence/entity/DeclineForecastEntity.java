package com.bp.decline.persistence.entity;

import com.bp.decline.core.enums.DeclineType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "decline_forecasts")
public class DeclineForecastEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "well_id", nullable = false)
    private WellEntity well;

    @Enumerated(EnumType.STRING)
    @Column(name = "decline_type", nullable = false, length = 20)
    private DeclineType declineType;

    /** qi — initial production rate (bbl/day or Mscf/day) */
    @Column(name = "initial_rate", nullable = false, precision = 12, scale = 4)
    private BigDecimal initialRate;

    /** Di — initial decline rate (fraction/day) */
    @Column(name = "initial_decline_rate", nullable = false, precision = 10, scale = 6)
    private BigDecimal initialDeclineRate;

    /** b — Arps curvature factor (0 = exponential, 1 = harmonic, 0 < b < 1 = hyperbolic) */
    @Column(name = "b_factor", nullable = false, precision = 6, scale = 4)
    private BigDecimal bFactor;

    @Column(name = "forecast_months", nullable = false)
    private int forecastMonths;

    /** Economic limit rate — below this the well is uneconomic */
    @Column(name = "economic_limit", precision = 12, scale = 4)
    private BigDecimal economicLimit;

    /** Estimated Ultimate Recovery — cumulative production to economic limit */
    @Column(name = "eur", precision = 18, scale = 4)
    private BigDecimal eur;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    public DeclineForecastEntity(WellEntity well, DeclineType declineType, BigDecimal initialRate,
                                 BigDecimal initialDeclineRate, BigDecimal bFactor, int forecastMonths,
                                 BigDecimal economicLimit, BigDecimal eur, LocalDateTime computedAt) {
        this.well = well;
        this.declineType = declineType;
        this.initialRate = initialRate;
        this.initialDeclineRate = initialDeclineRate;
        this.bFactor = bFactor;
        this.forecastMonths = forecastMonths;
        this.economicLimit = economicLimit;
        this.eur = eur;
        this.computedAt = computedAt;
    }
}

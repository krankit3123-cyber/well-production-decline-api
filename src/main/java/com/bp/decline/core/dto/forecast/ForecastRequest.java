package com.bp.decline.core.dto.forecast;

import com.bp.decline.core.enums.DeclineType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request to compute an Arps decline curve forecast for a well")
public record ForecastRequest(

        @NotNull
        @Schema(description = "Decline model type", example = "HYPERBOLIC")
        DeclineType declineType,

        @NotNull
        @DecimalMin("0.0001")
        @Schema(description = "Initial production rate qi in bbl/day (oil) or Mscf/day (gas)", example = "3200.0")
        BigDecimal initialRate,

        @NotNull
        @DecimalMin("0.000001")
        @DecimalMax("0.999999")
        @Schema(description = "Initial nominal decline rate Di in fraction/day. E.g. 0.003 = 0.3%/day", example = "0.003")
        BigDecimal initialDeclineRate,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        @Schema(description = "Arps b-factor: 0 = exponential, 1 = harmonic, 0<b<1 = hyperbolic", example = "0.5")
        BigDecimal bFactor,

        @Min(1) @Max(600)
        @Schema(description = "Number of months to forecast", example = "120")
        int forecastMonths,

        @DecimalMin("0.0")
        @Schema(description = "Economic limit rate — below this the well is uneconomic (optional)", example = "50.0")
        BigDecimal economicLimit) {
}

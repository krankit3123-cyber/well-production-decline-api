package com.bp.decline.core.dto.production;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductionRecordRequest(
        @NotNull @PastOrPresent LocalDate recordDate,
        @NotNull @DecimalMin("0.0001") BigDecimal productionRate) {
}

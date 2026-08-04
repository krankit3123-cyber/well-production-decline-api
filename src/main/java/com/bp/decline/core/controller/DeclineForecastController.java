package com.bp.decline.core.controller;

import com.bp.decline.core.dto.forecast.ForecastRequest;
import com.bp.decline.core.dto.forecast.ForecastResponse;
import com.bp.decline.core.service.DeclineForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/wells/{wellId}/forecasts")
@Tag(name = "Decline Curve Forecasts", description = "Arps decline curve analysis and EUR computation")
public class DeclineForecastController {

    private final DeclineForecastService declineForecastService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Compute an Arps decline forecast",
            description = """
                    Computes a production forecast using the selected Arps decline model:
                    - EXPONENTIAL (b=0): fastest decline, suitable for solution-gas-drive reservoirs
                    - HYPERBOLIC (0<b<1): most common; fractured reservoirs often use b≈0.5
                    - HARMONIC (b=1): slowest decline; gravity-drainage or water-drive reservoirs
                    
                    Returns the full monthly time-series and the Estimated Ultimate Recovery (EUR).
                    The forecast parameters are persisted and can be retrieved later via GET.
                    """)
    public ForecastResponse computeForecast(
            @PathVariable Long wellId,
            @Valid @RequestBody ForecastRequest request) {
        return declineForecastService.computeAndSave(wellId, request);
    }

    @GetMapping
    @Operation(summary = "List all forecasts for a well (no time-series, summary only)")
    public List<ForecastResponse> list(@PathVariable Long wellId) {
        return declineForecastService.listByWell(wellId);
    }

    @GetMapping("/{forecastId}")
    @Operation(summary = "Get a specific forecast with full time-series re-computed")
    public ForecastResponse get(@PathVariable Long wellId, @PathVariable Long forecastId) {
        return declineForecastService.get(wellId, forecastId);
    }
}

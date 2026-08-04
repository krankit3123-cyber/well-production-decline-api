package com.bp.decline.core.controller;

import com.bp.decline.core.dto.production.ProductionRecordRequest;
import com.bp.decline.core.dto.production.ProductionRecordResponse;
import com.bp.decline.core.service.ProductionRecordService;
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
@RequestMapping("/api/v1/wells/{wellId}/production")
@Tag(name = "Production Records", description = "Historical production data per well")
public class ProductionRecordController {

    private final ProductionRecordService productionRecordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a production record for a well")
    public ProductionRecordResponse addRecord(
            @PathVariable Long wellId,
            @Valid @RequestBody ProductionRecordRequest request) {
        return productionRecordService.addRecord(wellId, request);
    }

    @GetMapping
    @Operation(summary = "List all production records for a well, ordered by date ascending")
    public List<ProductionRecordResponse> list(@PathVariable Long wellId) {
        return productionRecordService.listByWell(wellId);
    }

    @GetMapping("/{recordId}")
    @Operation(summary = "Get a specific production record")
    public ProductionRecordResponse get(@PathVariable Long wellId, @PathVariable Long recordId) {
        return productionRecordService.get(wellId, recordId);
    }
}

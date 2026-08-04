package com.bp.decline.core.controller;

import com.bp.decline.core.dto.well.WellRequest;
import com.bp.decline.core.dto.well.WellResponse;
import com.bp.decline.core.enums.WellStatus;
import com.bp.decline.core.service.WellService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/wells")
@Tag(name = "Wells", description = "Well registry management")
public class WellController {

    private final WellService wellService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new well")
    public WellResponse create(@Valid @RequestBody WellRequest request) {
        return wellService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get well by ID")
    public WellResponse get(@PathVariable Long id) {
        return wellService.get(id);
    }

    @GetMapping
    @Operation(summary = "List all wells, optionally filtered by status")
    public List<WellResponse> list(@RequestParam(required = false) WellStatus status) {
        if (status != null) {
            return wellService.listByStatus(status);
        }
        return wellService.list();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update well details")
    public WellResponse update(@PathVariable Long id, @Valid @RequestBody WellRequest request) {
        return wellService.update(id, request);
    }
}

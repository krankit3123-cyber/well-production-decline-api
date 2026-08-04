package com.bp.decline.core.dto.well;

import com.bp.decline.core.enums.FluidType;
import com.bp.decline.core.enums.WellStatus;
import com.bp.decline.persistence.entity.WellEntity;

import java.time.LocalDateTime;

public record WellResponse(
        Long id,
        String name,
        String fieldName,
        String basin,
        String country,
        FluidType fluidType,
        WellStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static WellResponse from(WellEntity entity) {
        return new WellResponse(
                entity.getId(),
                entity.getName(),
                entity.getFieldName(),
                entity.getBasin(),
                entity.getCountry(),
                entity.getFluidType(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}

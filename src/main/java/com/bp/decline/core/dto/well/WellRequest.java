package com.bp.decline.core.dto.well;

import com.bp.decline.core.enums.FluidType;
import com.bp.decline.core.enums.WellStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WellRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 200) String fieldName,
        @Size(max = 200) String basin,
        @NotBlank @Size(max = 100) String country,
        @NotNull FluidType fluidType,
        WellStatus status) {
}

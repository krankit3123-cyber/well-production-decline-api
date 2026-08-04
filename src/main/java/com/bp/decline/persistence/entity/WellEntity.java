package com.bp.decline.persistence.entity;

import com.bp.decline.core.enums.FluidType;
import com.bp.decline.core.enums.WellStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wells")
public class WellEntity extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "field_name", nullable = false, length = 200)
    private String fieldName;

    @Column(length = 200)
    private String basin;

    @Column(nullable = false, length = 100)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "fluid_type", nullable = false, length = 20)
    private FluidType fluidType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WellStatus status;

    public WellEntity(String name, String fieldName, String basin,
                      String country, FluidType fluidType, WellStatus status) {
        this.name = name;
        this.fieldName = fieldName;
        this.basin = basin;
        this.country = country;
        this.fluidType = fluidType;
        this.status = status;
    }

    public void update(String name, String fieldName, String basin,
                       String country, FluidType fluidType, WellStatus status) {
        this.name = name;
        this.fieldName = fieldName;
        this.basin = basin;
        this.country = country;
        this.fluidType = fluidType;
        this.status = status;
    }
}

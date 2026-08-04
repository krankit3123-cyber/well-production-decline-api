package com.bp.decline.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "production_records")
public class ProductionRecordEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "well_id", nullable = false)
    private WellEntity well;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    /**
     * Production rate in bbl/day (oil) or Mscf/day (gas).
     */
    @Column(name = "production_rate", nullable = false, precision = 12, scale = 4)
    private BigDecimal productionRate;

    public ProductionRecordEntity(WellEntity well, LocalDate recordDate, BigDecimal productionRate) {
        this.well = well;
        this.recordDate = recordDate;
        this.productionRate = productionRate;
    }
}

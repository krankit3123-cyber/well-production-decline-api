package com.bp.decline.persistence.repository;

import com.bp.decline.persistence.entity.ProductionRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProductionRecordRepository extends JpaRepository<ProductionRecordEntity, Long> {

    List<ProductionRecordEntity> findByWellIdOrderByRecordDateAsc(Long wellId);

    List<ProductionRecordEntity> findByWellIdAndRecordDateBetweenOrderByRecordDateAsc(
            Long wellId, LocalDate from, LocalDate to);

    boolean existsByWellIdAndRecordDate(Long wellId, LocalDate recordDate);
}

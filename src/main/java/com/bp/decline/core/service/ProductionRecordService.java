package com.bp.decline.core.service;

import com.bp.decline.common.exception.ResourceNotFoundException;
import com.bp.decline.core.dto.production.ProductionRecordRequest;
import com.bp.decline.core.dto.production.ProductionRecordResponse;
import com.bp.decline.persistence.entity.ProductionRecordEntity;
import com.bp.decline.persistence.entity.WellEntity;
import com.bp.decline.persistence.repository.ProductionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductionRecordService {

    private final ProductionRecordRepository productionRecordRepository;
    private final WellService wellService;

    @Transactional
    public ProductionRecordResponse addRecord(Long wellId, ProductionRecordRequest request) {
        WellEntity well = wellService.findEntity(wellId);

        if (productionRecordRepository.existsByWellIdAndRecordDate(wellId, request.recordDate())) {
            throw new IllegalArgumentException(
                    "Production record already exists for well " + wellId + " on " + request.recordDate());
        }

        ProductionRecordEntity entity = new ProductionRecordEntity(
                well, request.recordDate(), request.productionRate());
        return ProductionRecordResponse.from(productionRecordRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ProductionRecordResponse> listByWell(Long wellId) {
        wellService.findEntity(wellId); // validate well exists
        return productionRecordRepository.findByWellIdOrderByRecordDateAsc(wellId).stream()
                .map(ProductionRecordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductionRecordResponse get(Long wellId, Long recordId) {
        ProductionRecordEntity entity = productionRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Production record not found: " + recordId));
        if (!entity.getWell().getId().equals(wellId)) {
            throw new ResourceNotFoundException(
                    "Production record " + recordId + " does not belong to well " + wellId);
        }
        return ProductionRecordResponse.from(entity);
    }
}

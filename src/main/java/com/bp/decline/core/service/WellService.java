package com.bp.decline.core.service;

import com.bp.decline.common.exception.ResourceNotFoundException;
import com.bp.decline.core.dto.well.WellRequest;
import com.bp.decline.core.dto.well.WellResponse;
import com.bp.decline.core.enums.WellStatus;
import com.bp.decline.persistence.entity.WellEntity;
import com.bp.decline.persistence.repository.WellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class WellService {

    private final WellRepository wellRepository;

    @Transactional
    public WellResponse create(WellRequest request) {
        WellStatus status = request.status() != null ? request.status() : WellStatus.ACTIVE;
        WellEntity entity = new WellEntity(
                request.name(),
                request.fieldName(),
                request.basin(),
                request.country(),
                request.fluidType(),
                status);
        return WellResponse.from(wellRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public WellResponse get(Long id) {
        return WellResponse.from(findById(id));
    }

    @Transactional(readOnly = true)
    public List<WellResponse> list() {
        return wellRepository.findAll().stream()
                .map(WellResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WellResponse> listByStatus(WellStatus status) {
        return wellRepository.findByStatus(status).stream()
                .map(WellResponse::from)
                .toList();
    }

    @Transactional
    public WellResponse update(Long id, WellRequest request) {
        WellEntity entity = findById(id);
        WellStatus status = request.status() != null ? request.status() : entity.getStatus();
        entity.update(request.name(), request.fieldName(), request.basin(),
                request.country(), request.fluidType(), status);
        return WellResponse.from(entity);
    }

    /** Package-private: used by other services to load the entity directly */
    @Transactional(readOnly = true)
    public WellEntity findEntity(Long id) {
        return findById(id);
    }

    private WellEntity findById(Long id) {
        return wellRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Well not found: " + id));
    }
}

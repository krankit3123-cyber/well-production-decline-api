package com.bp.decline.persistence.repository;

import com.bp.decline.core.enums.WellStatus;
import com.bp.decline.persistence.entity.WellEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WellRepository extends JpaRepository<WellEntity, Long> {

    List<WellEntity> findByStatus(WellStatus status);

    List<WellEntity> findByFieldNameIgnoreCase(String fieldName);

    boolean existsByNameAndFieldName(String name, String fieldName);
}

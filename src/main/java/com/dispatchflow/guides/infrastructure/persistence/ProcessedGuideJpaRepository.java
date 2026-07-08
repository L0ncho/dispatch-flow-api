package com.dispatchflow.guides.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedGuideJpaRepository extends JpaRepository<ProcessedGuideEntity, Long> {
}
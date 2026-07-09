package com.dispatchflow.guides.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataGuideSequenceRepository extends JpaRepository<GuideSequenceEntity, Long> {
}

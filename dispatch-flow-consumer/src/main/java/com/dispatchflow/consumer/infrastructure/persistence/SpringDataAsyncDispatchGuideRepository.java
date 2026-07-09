package com.dispatchflow.consumer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAsyncDispatchGuideRepository extends JpaRepository<AsyncDispatchGuideEntity, Long> {
}

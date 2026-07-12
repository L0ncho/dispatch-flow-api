package com.dispatchflow.consumer.infrastructure.persistence;

import org.springframework.stereotype.Repository;

@Repository
public class AsyncDispatchGuideRepository {

    private final SpringDataAsyncDispatchGuideRepository repository;

    public AsyncDispatchGuideRepository(SpringDataAsyncDispatchGuideRepository repository) {
        this.repository = repository;
    }

    public AsyncDispatchGuideEntity save(AsyncDispatchGuideEntity entity) {
        return repository.save(entity);
    }
}

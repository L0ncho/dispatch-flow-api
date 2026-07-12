package com.dispatchflow.consumer.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AsyncGuideSequenceRepository {

    private final SpringDataAsyncGuideSequenceRepository repository;

    public AsyncGuideSequenceRepository(SpringDataAsyncGuideSequenceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public long nextSequence() {
        AsyncGuideSequenceEntity sequence = new AsyncGuideSequenceEntity();
        repository.save(sequence);
        return sequence.getId();
    }
}

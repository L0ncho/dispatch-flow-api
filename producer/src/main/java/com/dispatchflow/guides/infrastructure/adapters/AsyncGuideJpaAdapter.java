package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.infrastructure.persistence.AsyncDispatchGuideJpaEntity;
import com.dispatchflow.guides.infrastructure.persistence.AsyncDispatchGuideJpaMapper;
import com.dispatchflow.guides.infrastructure.persistence.SpringDataAsyncDispatchGuideRepository;
import com.dispatchflow.shared.domain.DomainError;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AsyncGuideJpaAdapter implements AsyncGuideStore {

    private final SpringDataAsyncDispatchGuideRepository repository;

    public AsyncGuideJpaAdapter(SpringDataAsyncDispatchGuideRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<DispatchGuide> findByGuideId(GuideId id) {
        return repository.findByGuideId(id.value())
                .map(AsyncDispatchGuideJpaMapper::toDomain)
                .filter(Objects::nonNull);
    }

    @Override
    public boolean existsByGuideId(GuideId id) {
        return repository.existsByGuideId(id.value());
    }

    @Override
    public List<DispatchGuide> findAllActive() {
        return repository.findByStatus(AsyncDispatchGuideJpaEntity.ProcessingStatus.PROCESSED).stream()
                .map(AsyncDispatchGuideJpaMapper::toDomain)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<DispatchGuide> findActiveByCarrierAndDispatchDate(String carrierName, LocalDate dispatchDate) {
        return repository
                .findByCarrierNameAndDispatchDateAndStatus(
                        carrierName,
                        dispatchDate,
                        AsyncDispatchGuideJpaEntity.ProcessingStatus.PROCESSED)
                .stream()
                .map(AsyncDispatchGuideJpaMapper::toDomain)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional
    public void save(DispatchGuide guide) {
        AsyncDispatchGuideJpaEntity entity = repository.findByGuideId(guide.getId().value())
                .orElseThrow(() -> DomainError.notFound("Async guide " + guide.getId().value() + " not found"));
        AsyncDispatchGuideJpaMapper.applyDomainToEntity(guide, entity);
        repository.save(entity);
    }
}

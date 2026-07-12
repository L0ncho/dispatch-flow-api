package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.repositories.GuideRepository;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.infrastructure.persistence.GuideJpaMapper;
import com.dispatchflow.guides.infrastructure.persistence.GuideJpaEntity;
import com.dispatchflow.guides.infrastructure.persistence.GuideSequenceEntity;
import com.dispatchflow.guides.infrastructure.persistence.GuideStatusJpa;
import com.dispatchflow.guides.infrastructure.persistence.SpringDataGuideRepository;
import com.dispatchflow.guides.infrastructure.persistence.SpringDataGuideSequenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaGuideRepository implements GuideRepository {

    private final SpringDataGuideRepository guideRepository;
    private final SpringDataGuideSequenceRepository sequenceRepository;

    public JpaGuideRepository(
            SpringDataGuideRepository guideRepository,
            SpringDataGuideSequenceRepository sequenceRepository) {
        this.guideRepository = guideRepository;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    @Transactional
    public void save(DispatchGuide guide) {
        GuideJpaEntity entity = GuideJpaMapper.toEntity(guide);
        guideRepository.save(entity);
    }

    @Override
    public Optional<DispatchGuide> findById(GuideId id) {
        return guideRepository.findById(id.value()).map(GuideJpaMapper::toDomain);
    }

    @Override
    public List<DispatchGuide> findAllActive() {
        return guideRepository.findByStatusNot(GuideStatusJpa.DELETED).stream()
                .map(GuideJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<DispatchGuide> findByCarrierAndDispatchDate(String carrierName, LocalDate dispatchDate) {
        return guideRepository
                .findByCarrierNameAndDispatchDateAndStatusNot(
                        carrierName, dispatchDate, GuideStatusJpa.DELETED)
                .stream()
                .map(GuideJpaMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public long nextSequence() {
        GuideSequenceEntity sequence = new GuideSequenceEntity();
        sequenceRepository.save(sequence);
        return sequence.getId();
    }
}

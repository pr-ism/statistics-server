package com.prism.statistics.infrastructure.label.persistence;

import static com.prism.statistics.domain.label.QPrLabel.prLabel;

import com.prism.statistics.domain.label.PrLabel;
import com.prism.statistics.domain.label.repository.PrLabelRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PrLabelRepositoryAdapter implements PrLabelRepository {

    private final JpaPrLabelRepository jpaPrLabelRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public PrLabel save(PrLabel label) {
        return jpaPrLabelRepository.save(label);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PrLabel> findLabel(Long pullRequestId, String labelName) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(prLabel)
                        .where(
                                prLabel.pullRequestId.eq(pullRequestId),
                                prLabel.labelName.eq(labelName)
                        )
                        .fetchOne()
        );
    }

    @Override
    @Transactional
    public void delete(PrLabel label) {
        jpaPrLabelRepository.delete(label);
    }
}

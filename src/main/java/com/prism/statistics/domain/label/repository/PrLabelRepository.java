package com.prism.statistics.domain.label.repository;

import com.prism.statistics.domain.label.PrLabel;

import java.util.Optional;

public interface PrLabelRepository {

    PrLabel save(PrLabel prLabel);

    Optional<PrLabel> findLabel(Long pullRequestId, String labelName);

    void delete(PrLabel prLabel);
}

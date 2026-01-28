package com.prism.statistics.domain.label.repository;

import com.prism.statistics.domain.label.PrLabel;

import java.util.Optional;

public interface PrLabelRepository {

    PrLabel save(PrLabel prLabel);

    boolean exists(Long pullRequestId, String labelName);

    void deleteLabel(Long pullRequestId, String labelName);
}

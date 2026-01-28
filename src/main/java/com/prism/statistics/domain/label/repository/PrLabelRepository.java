package com.prism.statistics.domain.label.repository;

import com.prism.statistics.domain.label.PrLabel;

public interface PrLabelRepository {

    PrLabel save(PrLabel prLabel);

    boolean exists(Long pullRequestId, String labelName);

    void deleteLabel(Long pullRequestId, String labelName);
}

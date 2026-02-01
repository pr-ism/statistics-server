package com.prism.statistics.domain.review.repository;

import com.prism.statistics.domain.review.Review;

public interface ReviewRepository {

    Review save(Review review);
}

package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dao.ReviewRepository;
import com.luv2code.springbootlibrary.entity.Review;
import com.luv2code.springbootlibrary.requestmodels.ReviewRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public void postReview(String userEmail, ReviewRequest reviewRequest) {
        if (userReviewListed(userEmail, reviewRequest.getBookId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review already created");
        }
        Review review = new Review();
        review.setRating(reviewRequest.getRating());
        review.setUserEmail(userEmail);
        review.setBookId(reviewRequest.getBookId());
        review.setReviewDescription(reviewRequest.getReviewDescription());
        reviewRepository.save(review);
    }

    public boolean userReviewListed(String userEmail, Long bookId) {
        return reviewRepository.findByUserEmailAndBookId(userEmail, bookId) != null;
    }
}

package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.requestmodels.ReviewRequest;
import com.luv2code.springbootlibrary.service.ReviewService;
import com.luv2code.springbootlibrary.utils.ExtractJWT;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin("https://localhost:3000")
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final String EMAIL = "\"email\"";
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/secure/user/book")
    public boolean reviewBookByUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                    @RequestParam Long bookId) {
        String userEmail = extractRequiredEmail(token);
        return reviewService.userReviewListed(userEmail, bookId);
    }

    @PostMapping("/secure")
    public void postReview(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                           @RequestBody ReviewRequest reviewRequest) {
        String userEmail = extractRequiredEmail(token);
        reviewService.postReview(userEmail, reviewRequest);
    }

    private static String extractRequiredEmail(String token) {
        String userEmail = ExtractJWT.payloadJWTExtraction(token, EMAIL);
        if (userEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User email is missing");
        }
        return userEmail;
    }
}

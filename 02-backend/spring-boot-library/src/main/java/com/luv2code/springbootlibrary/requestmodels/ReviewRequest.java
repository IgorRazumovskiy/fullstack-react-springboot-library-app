package com.luv2code.springbootlibrary.requestmodels;

import lombok.Data;

@Data
public class ReviewRequest {

    private double rating;

    private Long bookId;

    private String reviewDescription;
}

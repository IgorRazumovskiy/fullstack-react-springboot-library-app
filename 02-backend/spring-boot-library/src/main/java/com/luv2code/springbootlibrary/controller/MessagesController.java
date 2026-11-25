package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.requestmodels.AdminQuestionRequest;
import com.luv2code.springbootlibrary.requestmodels.MessageRequest;
import com.luv2code.springbootlibrary.service.MessagesService;
import com.luv2code.springbootlibrary.utils.ExtractJWT;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin("https://localhost:3000")
@RestController
@RequestMapping("/api/messages")
public class MessagesController {

    private static final String EMAIL = "\"email\"";
    private static final String USER_TYPE = "\"userType\"";
    private static final String ADMIN = "admin";

    private final MessagesService messagesService;

    public MessagesController(MessagesService messagesService) {
        this.messagesService = messagesService;
    }

    @PostMapping("/secure/add/message")
    public void postMessage(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                            @RequestBody MessageRequest messageRequest) {
        String userEmail = ExtractJWT.payloadJWTExtraction(token, EMAIL);
        if (userEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User email is missing");
        }
        messagesService.postMessage(messageRequest, userEmail);
    }

    @PutMapping("/secure/admin/message")
    public void putMessage(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                           @RequestBody AdminQuestionRequest adminQuestionRequest) {
        String userEmail = ExtractJWT.payloadJWTExtraction(token, EMAIL);
        String userType = ExtractJWT.payloadJWTExtraction(token, USER_TYPE);

        if (!ADMIN.equals(userType)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administration page only.");
        }

        messagesService.putMessage(adminQuestionRequest, userEmail);
    }
}

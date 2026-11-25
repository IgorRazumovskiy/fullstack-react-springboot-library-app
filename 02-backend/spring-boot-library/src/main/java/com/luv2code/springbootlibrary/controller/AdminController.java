package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.requestmodels.AddBookRequest;
import com.luv2code.springbootlibrary.service.AdminService;
import com.luv2code.springbootlibrary.utils.ExtractJWT;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin("https://localhost:3000")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final String USER_TYPE = "\"userType\"";
    private static final String ADMIN = "admin";

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PutMapping("/secure/increase/book/quantity")
    public void increaseBookQuantity(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                     @RequestParam Long bookId) {
        validateAdmin(token);
        adminService.increaseBookQuantity(bookId);
    }

    @PutMapping("/secure/decrease/book/quantity")
    public void decreaseBookQuantity(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                     @RequestParam Long bookId) {
        validateAdmin(token);
        adminService.decreaseBookQuantity(bookId);
    }

    @PostMapping("/secure/add/book")
    public void postBook(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                         @RequestBody AddBookRequest addBookRequest) {
        validateAdmin(token);
        adminService.postBook(addBookRequest);
    }

    @DeleteMapping("/secure/delete/book")
    public void deleteBook(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                           @RequestParam Long bookId) {
        validateAdmin(token);
        adminService.deleteBook(bookId);
    }

    private static void validateAdmin(String token) {
        String userType = ExtractJWT.payloadJWTExtraction(token, USER_TYPE);
        if (!ADMIN.equals(userType)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administration page only.");
        }
    }
}

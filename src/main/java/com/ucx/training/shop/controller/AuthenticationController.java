package com.ucx.training.shop.controller;

import com.ucx.training.shop.dto.ContactFormDTO;
import com.ucx.training.shop.dto.CustomerDTO;
import com.ucx.training.shop.dto.DTOEntity;
import com.ucx.training.shop.entity.Customer;
import com.ucx.training.shop.exception.ResponseException;
import com.ucx.training.shop.service.AuthenticationService;
import com.ucx.training.shop.service.CustomerService;
import com.ucx.training.shop.util.uimapper.DTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("v1/auth")

public class AuthenticationController {

    private AuthenticationService authenticationService;
    private CustomerService customerService;

    public AuthenticationController(AuthenticationService authenticationService, CustomerService customerService) {
        this.authenticationService = authenticationService;
        this.customerService = customerService;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credentials) throws ResponseException {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            return authenticationService.login(email, password);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseException(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public DTOEntity register(@RequestBody Customer customer) throws ResponseException {
        try {
            Customer createdCustomer = customerService.save(customer);
            return DTOMapper.convertToDto(createdCustomer, CustomerDTO.class);
        } catch (IllegalArgumentException e) {
            throw new ResponseException(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new ResponseException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/contact-us")
    public Map<String, String> contactUs(@RequestBody ContactFormDTO contactFormDTO) throws ResponseException {
        Map<String, String> response = new HashMap<>();
        response.put("SUCCESS", "Email was successfully sent");
        response.put("ERROR", "Something went wrong, please try again");
        try {
            authenticationService.sendMail(contactFormDTO);
            return Map.of("Message", response.get("SUCCESS"));
        } catch (MessagingException e) {
            throw new ResponseException(response.get("ERROR"), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            throw new ResponseException(response.get("ERROR"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new ResponseException(response.get("ERROR"), HttpStatus.BAD_REQUEST);
        }

    }
}

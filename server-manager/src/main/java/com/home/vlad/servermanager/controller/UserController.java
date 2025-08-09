package com.home.vlad.servermanager.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.vlad.servermanager.dto.user.LoginRequest;
import com.home.vlad.servermanager.service.user.MyUserService;

@RestController
@RequestMapping("/manage/api/v1/login")
public class UserController {
    private MyUserService service;

    public UserController(MyUserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String token = service.verifyLogin(loginRequest);
            return ResponseEntity.ok(Map.of("accessToken", token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        // Requires valid token; otherwise Security returns 401
        return Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities());
    }
}

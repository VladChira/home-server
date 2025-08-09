package com.home.vlad.servermanager.service.user;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.home.vlad.servermanager.dto.user.LoginRequest;

@Service
public class MyUserService {
    private final AuthenticationManager authManager;

    private JWTService jwtService;

    public MyUserService(AuthenticationManager authManager, JWTService jwtService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public String verifyLogin(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        if (!auth.isAuthenticated()) {
            throw new BadCredentialsException("Bad creds");
        }
        return jwtService.generateToken(req.getUsername());
    }
}

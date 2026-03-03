package com.unipi.PlayerHive.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Gamer Registration
    @PostMapping("/register")
    public ResponseEntity<> registerGamer(@Valid @RequestBody bla bla) {
        return null;
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<> loginUser(@Valid @RequestBody bla bla){
        return null;
    }
}

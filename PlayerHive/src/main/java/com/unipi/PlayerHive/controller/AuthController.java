package com.unipi.PlayerHive.controller;

import com.unipi.PlayerHive.DTO.users.login.AccessTokenDTO;
import com.unipi.PlayerHive.DTO.users.login.UserLoginDTO;
import com.unipi.PlayerHive.DTO.users.login.UserRegistrationDTO;
import com.unipi.PlayerHive.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO dto){
        authService.registerUser(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginDTO dto){
        try{
            String token = authService.loginUser(dto);
            if(token != null)
                return ResponseEntity.ok(new AccessTokenDTO(token));
            else
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch(AuthenticationException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}

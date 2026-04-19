package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.users.UserLoginDTO;
import com.unipi.PlayerHive.DTO.users.UserRegistrationDTO;
import com.unipi.PlayerHive.config.JwtUtils;
import com.unipi.PlayerHive.model.User;
import com.unipi.PlayerHive.model.UserNeo4j;
import com.unipi.PlayerHive.model.UserPrincipal;
import com.unipi.PlayerHive.repository.users.UserNeo4jRepository;
import com.unipi.PlayerHive.repository.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final UserNeo4jRepository userNeo4jRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public AuthService(AuthenticationManager authManager, UserRepository userRepository, UserNeo4jRepository userNeo4jRepository, PasswordEncoder encoder){
        this.authManager = authManager;
        this.userRepository = userRepository;
        this.userNeo4jRepository = userNeo4jRepository;
        this.encoder = encoder;
    }

    public void registerUser(UserRegistrationDTO dto){
        if(userRepository.existsByUsername(dto.username()))
            throw new IllegalArgumentException("Username already exists");
        if(userRepository.existsByEmail(dto.email()))
            throw new IllegalArgumentException("Email already exists");

        // generate the id once so we can reuse it for the Neo4j node below
        String userId = UUID.randomUUID().toString();

        User newUser = new User();
        newUser.setId(userId);
        newUser.setUsername(dto.username());
        newUser.setPassword(encoder.encode(dto.password()));
        newUser.setEmail(dto.email());
        newUser.setBirthDate(dto.birthDate());
        newUser.setRole("USER");
        newUser.setNumGames(0);
        newUser.setHoursPlayed(0);
        newUser.setFriends(0);
        userRepository.save(newUser); // add the id reutrned from here to neo4j not custom id

        // Neo4j needs its own node for the user without this, friend and library operations would silently break
        UserNeo4j neo4jUser = new UserNeo4j();
        neo4jUser.setId(userId);
        neo4jUser.setUsername(dto.username());
        userNeo4jRepository.save(neo4jUser);
    }

    public String loginUser(UserLoginDTO dto){
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        if(auth.isAuthenticated()){
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            return JwtUtils.generateToken(principal.getUser().getId());
        }
        return null;
    }
}

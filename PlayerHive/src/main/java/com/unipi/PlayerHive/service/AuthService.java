package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.users.UserLoginDTO;
import com.unipi.PlayerHive.DTO.users.UserRegistrationDTO;
import com.unipi.PlayerHive.config.JwtUtils;
import com.unipi.PlayerHive.model.User;
import com.unipi.PlayerHive.model.UserNeo4j;
import com.unipi.PlayerHive.model.UserPrincipal;
import com.unipi.PlayerHive.repository.users.UserNeo4jRepository;
import com.unipi.PlayerHive.repository.users.UserRepository;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
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

    public void registerUser(@Nonnull @Valid @RequestBody UserRegistrationDTO dto){
        // A single query is performed to look for both username and email matches, instead of two separate ones, idk if the other version is better
        // todo if we are very fat, we can use access tables to check which operation is better, or add indexes maybe

        userRepository.findLightByUsernameOrEmail(dto.username(), dto.email()).ifPresent(user -> {
            if (user.username().equals(dto.username())) {
                throw new IllegalArgumentException("Username already exists");
            }
            if (user.email().equals(dto.email())) {
                throw new IllegalArgumentException("Email already exists");
            }
        });

        // the userId will be obtained by mongoDB
        User newUser = new User();

        newUser.setUsername(dto.username());
        newUser.setPassword(encoder.encode(dto.password()));
        newUser.setEmail(dto.email());
        newUser.setBirthDate(dto.birthDate());
        newUser.setRole("USER");
        newUser.setNumGames(0);
        newUser.setHoursPlayed(0);
        newUser.setFriends(0);
        // TODO: DO WE HAVE TO ADD THE EMPTY ARRAYS, OR DO WE ADD THE NULL CHECK IN THE QUERIES?
        newUser.setFriendRequests(new ArrayList<>());
        newUser.setReviewIds(new ArrayList<>());

        User savedUser = userRepository.save(newUser);

        UserNeo4j neo4jUser = new UserNeo4j();
        neo4jUser.setId(savedUser.getId());
        neo4jUser.setUsername(dto.username());

        userNeo4jRepository.save(neo4jUser);
    }

    public String loginUser(@Nonnull @Valid @RequestBody UserLoginDTO dto){
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        if(auth.isAuthenticated()){
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            return JwtUtils.generateToken(principal.getUser().getId());
        }
        return null;
    }
}

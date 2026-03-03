package com.unipi.PlayerHive.controller;

import com.unipi.PlayerHive.DTO.games.GameInfoDTO;
import com.unipi.PlayerHive.DTO.games.GameReducedDTO;
import com.unipi.PlayerHive.DTO.games.UserGameInfoDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
// PREAUTHORIZE

public class UserController {
    private final UserService userService;

    UserController(UserService userService){
        this.userService = userService;
    }

}

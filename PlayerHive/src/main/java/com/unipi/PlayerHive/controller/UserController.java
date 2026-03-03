package com.unipi.PlayerHive.controller;

import com.unipi.PlayerHive.DTO.games.GameInfoDTO;
import com.unipi.PlayerHive.DTO.games.GameReducedDTO;
import com.unipi.PlayerHive.DTO.games.UserGameInfoDTO;
import com.unipi.PlayerHive.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
// PREAUTHORIZE
public class UserController {
    private final UserService userService;

    UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/{userId}/profile")
    @GetMapping("/{userId}/library")

    // how do we know WHO is the user?
    @PostMapping("/addToLibrary")
    public ResponseEntity<String> addToLibrary(@Valid @RequestBody UserGameInfoDTO userGameInfo){
        userService.addGameToLibrary(userGameInfo);
        return ResponseEntity.ok("The game has been added to your library successfully");
    }
    @DeleteMapping("/removeFromLibrary/{gameId}")
    public ResponseEntity<String> removeFromLibrary(@PathVariable String gameId){
        userService.removeGameFromLibrary(gameId);
        return ResponseEntity.ok("The game has been removed from your library successfully");
    }
    @GetMapping("/{userId}/friends")
    @PostMapping("sendFriendRequest/{userId}")
    @PostMapping("approveFriendRequest/{userId}")
    @DeleteMapping("removeFriend/{userId}")


}

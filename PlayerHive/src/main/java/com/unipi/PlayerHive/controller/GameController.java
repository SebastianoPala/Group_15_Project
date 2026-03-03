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
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService){
        this.gameService = gameService;
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameInfoDTO> getInfo(@PathVariable String gameId){
        return ResponseEntity.ok(gameService.method(gameId));
    }

    @GetMapping("/search/{gameName}")
    public ResponseEntity<List<GameReducedDTO>> searchByName(@PathVariable String gameName){
        return ResponseEntity.ok(gameService.method(gameName));
    }

    @PostMapping("/addToLibrary")
    public ResponseEntity<String> addToLibrary(@Valid @RequestBody UserGameInfoDTO userGameInfo){
        return ResponseEntity.ok("The game has been added successfully");
    }
    @DeleteMapping("/removeFromLibrary/{gameId}")
    public ResponseEntity<String> removeFromLibrary(@PathVariable String gameId){
        return ResponseEntity.ok("The game has been added successfully");
    }

    //ADMIN
    @PostMapping("/AddGame")
    @PostMapping("/ModifyGame")
    @DeleteMapping("/DeleteGame/{gameId}")
}

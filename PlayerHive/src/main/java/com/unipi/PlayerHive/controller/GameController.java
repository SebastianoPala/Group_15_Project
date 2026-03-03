package com.unipi.PlayerHive.controller;

import com.unipi.PlayerHive.DTO.games.GameInfoDTO;
import com.unipi.PlayerHive.DTO.games.GameReducedDTO;
import com.unipi.PlayerHive.DTO.games.UserGameInfoDTO;
import com.unipi.PlayerHive.service.GameService;
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
        return ResponseEntity.ok(gameService.getGameById(gameId));
    }

    @GetMapping("/search/{gameName}")
    public ResponseEntity<List<GameReducedDTO>> searchByName(@PathVariable String gameName){
        return ResponseEntity.ok(gameService.searchGameById(gameName));
    }



    //ADMIN
    @PostMapping("/addGame")
    public ResponseEntity<String> addGame(@Valid @RequestBody GameInfoDTO gameInfo){
        gameService.addGame(gameInfo);
        return ResponseEntity.ok("The game has been added successfully");
    }
    @PostMapping("/editGame")
    public ResponseEntity<String> editGame(@RequestBody GameInfoDTO gameInfo){
        gameService.editGame(gameInfo);
        return ResponseEntity.ok("The game info has been edited successfully");
    }
    @DeleteMapping("/deleteGame/{gameId}")
    public ResponseEntity<String> deleteGame(@PathVariable String gameId){
        gameService.deleteGame(gameId);
        return ResponseEntity.ok("The game info has been edited successfully");
    }
}

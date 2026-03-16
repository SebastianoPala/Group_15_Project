package com.unipi.PlayerHive.controller;

import com.unipi.PlayerHive.DTO.games.*;
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
    public ResponseEntity<List<GameSearchDTO>> searchByName(@PathVariable String gameName){
        return ResponseEntity.ok(gameService.searchGameByName(gameName));
    }

    @PostMapping("/addReview")
    public ResponseEntity<String> addReview(@Valid @RequestBody addReviewDTO addReviewDTO){
        gameService.addReview(addReviewDTO);
        return ResponseEntity.ok("Review added successfully");
    }

    @DeleteMapping("/deleteReview/{gameId}")
    public ResponseEntity<String> deleteReview(@PathVariable String gameId){
        gameService.deleteReviewFromGame(gameId);
        return ResponseEntity.ok("Review deleted successfully");
    }


    //ADMIN
    @PostMapping("/addGame")
    public ResponseEntity<String> addGame(@Valid @RequestBody EditGameDTO newGame){
        gameService.addGame(newGame);
        return ResponseEntity.ok("The game has been added successfully");
    }
    @PostMapping("/editGame/{gameId}")
    public ResponseEntity<String> editGame(@PathVariable String gameId,@RequestBody EditGameDTO editGame){
        gameService.editGame(gameId,editGame);
        return ResponseEntity.ok("The game info has been edited successfully");
    }
    @DeleteMapping("/deleteGame/{gameId}")
    public ResponseEntity<String> deleteGame(@PathVariable String gameId){
        gameService.deleteGame(gameId);
        return ResponseEntity.ok("The game has been deleted successfully");
    }
}

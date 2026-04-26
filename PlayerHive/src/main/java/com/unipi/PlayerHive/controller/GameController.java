package com.unipi.PlayerHive.controller;

import com.unipi.PlayerHive.DTO.games.*;
import com.unipi.PlayerHive.DTO.reviews.AddReviewDTO;
import com.unipi.PlayerHive.DTO.reviews.ReviewDTO;
import com.unipi.PlayerHive.service.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")

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
    public ResponseEntity<Slice<GameSearchDTO>> searchByName(@PathVariable String gameName,
                                                             @RequestParam(defaultValue = "0") @Min(0) int page,
                                                             @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size){
        return ResponseEntity.ok(gameService.searchGameByName(gameName,page,size));
    }

    @GetMapping("showReviews/{gameId}")
    public ResponseEntity<List<ReviewDTO>> showGameReviews(@PathVariable String gameId,
                                                           @RequestParam(defaultValue = "0") @Min(0) int page,
                                                           @RequestParam(defaultValue = "25") @Min(25) @Max(100) int size){
        return ResponseEntity.ok(gameService.getGameReviews(gameId,page,size));
    }

    @PostMapping("/addReview/{gameId}")
    public ResponseEntity<String> addReview(@PathVariable String gameId,@Valid @RequestBody AddReviewDTO addReviewDTO){
        gameService.addReview(gameId, addReviewDTO);
        return ResponseEntity.ok("Review added successfully");
    }

    @DeleteMapping("/deleteReview/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable String reviewId){
        gameService.deleteReview(reviewId);
        return ResponseEntity.ok("Review deleted successfully");
    }


}

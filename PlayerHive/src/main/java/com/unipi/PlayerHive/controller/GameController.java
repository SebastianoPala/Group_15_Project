package com.unipi.PlayerHive.controller;

import com.unipi.PlayerHive.DTO.games.*;
import com.unipi.PlayerHive.DTO.reviews.AddReviewDTO;
import com.unipi.PlayerHive.DTO.reviews.ReviewContainerDTO;
import com.unipi.PlayerHive.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")

// TODO FIX RANGES OF REQUESTS

@Tag(name = "3. Games", description = "Game search, reviews, and advanced queries")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService){
        this.gameService = gameService;
    }

    @GetMapping("/{gameId}")
    @Operation(summary = "Get game info", description = "Returns full details, user score, and average playtime.")
    @ApiResponse(responseCode = "200", description = "Game details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    public ResponseEntity<GameInfoDTO> getInfo(@PathVariable @NotNull  @Size(min = 24, max = 24) String gameId){
        return ResponseEntity.ok(gameService.getGameById(gameId));
    }

    @GetMapping("/search/{gameName}")
    @Operation(summary = "Search games by name", description = "Paginated text search within the catalog.")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    public ResponseEntity<GameSearchContainerDTO> searchByName(@PathVariable String gameName,
                                                             @RequestParam(defaultValue = "0") @Min(0) int page,
                                                             @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size){
        return ResponseEntity.ok(gameService.searchGameByName(gameName,page,size));
    }

    @GetMapping("/reviews/{gameId}")
    @Operation(summary = "Show reviews", description = "Returns a paginated list of reviews associated with a specific game.")
    @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    public ResponseEntity<ReviewContainerDTO> showGameReviews(@PathVariable @NotNull  @Size(min = 24, max = 24) String gameId,
                                                           @RequestParam(defaultValue = "0") @Min(0) int page,
                                                           @RequestParam(defaultValue = "25") @Min(1) @Max(100) int size){
        return ResponseEntity.ok(gameService.getGameReviews(gameId,page,size));
    }

    @PostMapping("/addReview/{gameId}")
    @Operation(summary = "Add a review", description = "Adds a review to a game. A user cannot review the same game twice.")
    @ApiResponse(responseCode = "200", description = "Review added successfully")
    @ApiResponse(responseCode = "409", description = "The user already reviewed this game")
    public ResponseEntity<String> addReview(@PathVariable @NotNull @Size(min = 24, max = 24) String gameId, @Valid @RequestBody AddReviewDTO addReviewDTO){
        gameService.addReview(gameId, addReviewDTO);
        return ResponseEntity.ok("Review added successfully");
    }

    @DeleteMapping("/deleteReview/{reviewId}")
    @Operation(summary = "Delete a review", description = "Removes a review. Can only be done by the author or an Admin.")
    @ApiResponse(responseCode = "200", description = "Review deleted successfully")
    @ApiResponse(responseCode = "403", description = "Not authorized to delete this review")
    public ResponseEntity<String> deleteReview(@PathVariable @NotNull  @Size(min = 24, max = 24) String reviewId){
        gameService.deleteReview(reviewId);
        return ResponseEntity.ok("Review deleted successfully");
    }

    // INTERESTING QUERIES ============================================
    //TODO ADD VARIABLES
    @GetMapping("/getDeals")
    public ResponseEntity<List<GameStatsDTO>> getDeals(){
        return ResponseEntity.ok(gameService.getDeals());
    }

    @GetMapping("/getInvestments")
    public ResponseEntity<List<GameInvestmentDTO>> getInvestments(){
        return ResponseEntity.ok(gameService.getInvestments());
    }

    @GetMapping("/getDiscussed")
    public ResponseEntity<List<GameStatsDTO>> getDiscussed(){
        return ResponseEntity.ok(gameService.getDiscussed());
    }

    @GetMapping("/getTopGames")
    public ResponseEntity<List<GameStatsDTO>> getTopGames(){
        return ResponseEntity.ok(gameService.getTopGames());
    }
}

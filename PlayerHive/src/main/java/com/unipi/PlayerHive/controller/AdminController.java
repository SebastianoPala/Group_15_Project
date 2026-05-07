package com.unipi.PlayerHive.controller;


import com.unipi.PlayerHive.DTO.games.AddGameDTO;
import com.unipi.PlayerHive.DTO.games.EditGameDTO;
import com.unipi.PlayerHive.service.AdminService;
import com.unipi.PlayerHive.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")

@Tag(name = "1. Admin", description = "Admin operations (Game and User management)")
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;

    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @PostMapping("/addGame")
    @Operation(summary = "Add a new game", description = "Inserts a new game into the database (MongoDB and Neo4j).")
    @ApiResponse(responseCode = "200", description = "The game has been added successfully")
    @ApiResponse(responseCode = "409", description = "The game already exists")
    public ResponseEntity<String> addGame(@Valid @RequestBody AddGameDTO newGame){
        adminService.addGame(newGame);
        return ResponseEntity.ok("The game has been added successfully");
    }


    @PatchMapping("/editGame/{gameId}")
    @Operation(summary = "Edit a game", description = "Updates the details of an existing game by its ID.")
    @ApiResponse(responseCode = "200", description = "The game info has been edited successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    public ResponseEntity<String> editGame(@PathVariable @NotNull @Size(min = 24, max = 24) String gameId, @RequestBody EditGameDTO editGame){
        adminService.editGame(gameId,editGame);
        return ResponseEntity.ok("The game info has been edited successfully");
    }

    @DeleteMapping("/deleteGame/{gameId}")
    @Operation(summary = "Delete a game", description = "Permanently removes a game, its reviews, and updates user statistics.")
    @ApiResponse(responseCode = "200", description = "The game has been deleted successfully")
    @ApiResponse(responseCode = "404", description = "Game not found")
    public ResponseEntity<String> deleteGame(@PathVariable @NotNull @Size(min = 24, max = 24) String gameId){
        adminService.deleteGame(gameId);
        return ResponseEntity.ok("The game has been deleted successfully");
    }

    @DeleteMapping("/deleteUser/{userId}")
    @Operation(summary = "Force delete a user", description = "Allows an admin to remove a user and all related data.")
    @ApiResponse(responseCode = "200", description = "The user has been deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<String> deleteUser(@PathVariable @NotNull @Size(min = 24, max = 24) String userId){
        userService.deleteUser(userId);
        return  ResponseEntity.ok("The user has been deleted successfully");
    }
}

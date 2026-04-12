package com.unipi.PlayerHive.controller;


import com.unipi.PlayerHive.DTO.games.AddGameDTO;
import com.unipi.PlayerHive.DTO.games.EditGameDTO;
import com.unipi.PlayerHive.service.AdminService;
import com.unipi.PlayerHive.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;

    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @PostMapping("/addGame")
    public ResponseEntity<String> addGame(@Valid @RequestBody AddGameDTO newGame){
        adminService.addGame(newGame);
        return ResponseEntity.ok("The game has been added successfully");
    }
    @PatchMapping("/editGame/{gameId}")
    public ResponseEntity<String> editGame(@PathVariable String gameId, @RequestBody EditGameDTO editGame){
        adminService.editGame(gameId,editGame);
        return ResponseEntity.ok("The game info has been edited successfully");
    }
    @DeleteMapping("/deleteGame/{gameId}")
    public ResponseEntity<String> deleteGame(@PathVariable String gameId){
        adminService.deleteGame(gameId);
        return ResponseEntity.ok("The game has been deleted successfully");
    }

    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
        return  ResponseEntity.ok("The user has been deleted successfully");
    }
}

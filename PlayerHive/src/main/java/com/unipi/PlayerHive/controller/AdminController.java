package com.unipi.PlayerHive.controller;


import com.unipi.PlayerHive.DTO.games.EditGameDTO;
import com.unipi.PlayerHive.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/addGame")
    public ResponseEntity<String> addGame(@Valid @RequestBody EditGameDTO newGame){
        adminService.addGame(newGame);
        return ResponseEntity.ok("The game has been added successfully");
    }
    @PostMapping("/editGame/{gameId}")
    public ResponseEntity<String> editGame(@PathVariable String gameId, @RequestBody EditGameDTO editGame){
        adminService.editGame(gameId,editGame);
        return ResponseEntity.ok("The game info has been edited successfully");
    }
    @DeleteMapping("/deleteGame/{gameId}")
    public ResponseEntity<String> deleteGame(@PathVariable String gameId){
        adminService.deleteGame(gameId);
        return ResponseEntity.ok("The game has been deleted successfully");
    }
}

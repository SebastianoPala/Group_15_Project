package com.unipi.PlayerHive.controller;

import com.unipi.PlayerHive.DTO.games.LibraryGameDTO;
import com.unipi.PlayerHive.DTO.users.*;
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

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDTO> showUserProfile(@PathVariable String userId){
        return ResponseEntity.ok(userService.getProfileById(userId));
    }

    @GetMapping("/MyProfile")
    public ResponseEntity<OwnProfileDTO> showOwnProfile(){
        return ResponseEntity.ok(userService.getOwnProfileById("efeacf7066b54bc983180329")); // the user is obtained by the token, TODO
    }

    @GetMapping("/library/{userId}")
    public ResponseEntity<List<LibraryGameDTO>> showUserLibrary(@PathVariable String userId){
        return ResponseEntity.ok(userService.getLibraryById(userId));
    }

    @PostMapping("/editLibrary")
    public ResponseEntity<String> editLibrary(@Valid @RequestBody AddGameToLibraryDTO addGame){
        userService.editLibrary(addGame);
        return ResponseEntity.ok("The library has been updated successfully");
    }
    @DeleteMapping("/removeFromLibrary/{gameId}")
    public ResponseEntity<String> removeFromLibrary(@PathVariable String gameId){
        userService.removeGameFromLibrary(gameId);
        return ResponseEntity.ok("The library has been updated successfully");
    }

    @GetMapping("/friends/{userId}")
    public ResponseEntity<List<FriendDTO>> showFriendList(@PathVariable String userId){
        return ResponseEntity.ok(userService.getFriendListById(userId));
    }

    @GetMapping("/friendRequests") // user is obtained by token
    public ResponseEntity<List<FriendRequestDTO>> showFriendRequests(){
        return ResponseEntity.ok(userService.getFriendRequestsById("fb39502211c742f9a7954e33"));
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<List<UserSearchDTO>> searchUser(@PathVariable String query){
        return ResponseEntity.ok(userService.searchUser(query));
    }

    @PostMapping("/sendFriendRequest/{userId}")
    public ResponseEntity<String> sendFriendRequest(@PathVariable String userId){
        userService.sendRequestToUser(userId);
        return ResponseEntity.ok("Friend request sent successfully");
    }

    @PostMapping("/approveFriendRequest/{userId}")
    public ResponseEntity<String> approveFriendRequest(@PathVariable String userId){
        userService.approveRequestFromUser(userId);
        return ResponseEntity.ok("Friend request has been approved successfully");
    }

    @PostMapping("/denyFriendRequest/{userId}")
    public ResponseEntity<String> denyFriendRequest(@PathVariable String userId){
        userService.denyRequestFromUser(userId);
        return ResponseEntity.ok("Friend request has been denied successfully");
    }

    @DeleteMapping("/removeFriend/{friendId}")
    public ResponseEntity<String> removeFriend(@PathVariable String friendId){
        userService.removeFriend(friendId);
        return ResponseEntity.ok("Friend removed successfully");
    }

}

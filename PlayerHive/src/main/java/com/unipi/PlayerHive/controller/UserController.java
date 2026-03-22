package com.unipi.PlayerHive.controller;

import com.unipi.PlayerHive.DTO.games.LibraryGameDTO;
import com.unipi.PlayerHive.DTO.users.*;
import com.unipi.PlayerHive.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
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
    public ResponseEntity<Page<LibraryGameDTO>> showUserLibrary(@PathVariable String userId,
                                                                 @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                 @RequestParam(defaultValue = "25") @Min(1) @Max(50) int size){
        return ResponseEntity.ok(userService.getLibraryById(userId, page, size));
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
    public ResponseEntity<Page<FriendDTO>> showFriendList(@PathVariable String userId,
                                                          @RequestParam(defaultValue = "0") @Min(0) int page,
                                                          @RequestParam(defaultValue = "25") @Min(1) @Max(50) int size){
        return ResponseEntity.ok(userService.getFriendListById(userId, page, size));
    }

    @GetMapping("/friendRequests") // user is obtained by token
    public ResponseEntity<List<FriendRequestDTO>> showFriendRequests(){
        return ResponseEntity.ok(userService.getFriendRequestsById("fb39502211c742f9a7954e33"));
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<Slice<UserSearchDTO>> searchUser(@PathVariable String query,
                                                            @RequestParam(defaultValue = "0") @Min(0) int page,
                                                            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int size){
        return ResponseEntity.ok(userService.searchUser(query, page, size));
    }

    @PostMapping("/sendFriendRequest/{targetUserId}")
    public ResponseEntity<String> sendFriendRequest(@PathVariable String targetUserId){
        userService.sendRequestToUser(targetUserId);
        return ResponseEntity.ok("Friend request sent successfully");
    }

    @PostMapping("/approveFriendRequest/{targetUserId}")
    public ResponseEntity<String> approveFriendRequest(@PathVariable String targetUserId){
        userService.approveRequestFromUser(targetUserId);
        return ResponseEntity.ok("Friend request has been approved successfully");
    }

    @DeleteMapping("/denyFriendRequest/{targetUserId}")
    public ResponseEntity<String> denyFriendRequest(@PathVariable String targetUserId){
        userService.removeRequestFromUser(targetUserId);
        return ResponseEntity.ok("Friend request has been denied successfully");
    }

    @DeleteMapping("/removeFriend/{friendId}")
    public ResponseEntity<String> removeFriend(@PathVariable String friendId){
        userService.removeFriend(friendId);
        return ResponseEntity.ok("Friend removed successfully");
    }

}

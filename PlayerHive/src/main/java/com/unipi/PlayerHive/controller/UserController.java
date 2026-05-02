package com.unipi.PlayerHive.controller;

import com.unipi.PlayerHive.DTO.games.LibraryGameDTO;
import com.unipi.PlayerHive.DTO.reviews.ReviewDTO;
import com.unipi.PlayerHive.DTO.users.*;
import com.unipi.PlayerHive.DTO.users.friends.FriendDTO;
import com.unipi.PlayerHive.DTO.users.friends.FriendRequestDTO;
import com.unipi.PlayerHive.model.user.UserPrincipal;
import com.unipi.PlayerHive.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    UserController(UserService userService){
        this.userService = userService;
    }

    public String getAuthenticatedUserId(){
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUser().getId();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> showUserProfile(@PathVariable @NotNull  @Size(min = 24, max = 24) String userId){

        // I obtain the principal as a general object, since it can be either a String or UserPrincipal depending on if
        // the user is logged in or not
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(principal instanceof UserPrincipal){
            String currentUserId = ((UserPrincipal) principal).getUser().getId();
            if(userId.equals(currentUserId))
                return ResponseEntity.ok(userService.getOwnProfileById());
        }
        return ResponseEntity.ok(userService.getProfileById(userId));

    }

    @GetMapping("/MyProfile")
    public ResponseEntity<OwnProfileDTO> showOwnProfile(){
        return ResponseEntity.ok(userService.getOwnProfileById());
    }


    @GetMapping("/search/{query}")
    public ResponseEntity<Slice<UserSearchDTO>> searchUser(@PathVariable String query,
                                                           @RequestParam(defaultValue = "0") @Min(0) int page,
                                                           @RequestParam(defaultValue = "10") @Min(1) @Max(30) int size){
        return ResponseEntity.ok(userService.searchUser(query, page, size));
    }

    @GetMapping("/library/{userId}")
    public ResponseEntity<Page<LibraryGameDTO>> showUserLibrary(@PathVariable @NotNull  @Size(min = 24, max = 24) String userId,
                                                                 @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                 @RequestParam(defaultValue = "25") @Min(1) @Max(50) int size){
        return ResponseEntity.ok(userService.getLibraryById(userId, page, size));
    }

    @GetMapping("/MyLibrary")
    public ResponseEntity<Page<LibraryGameDTO>> showOwnProfile(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                        @RequestParam(defaultValue = "25") @Min(1) @Max(50) int size){
        String requestingUserId = getAuthenticatedUserId();
        return ResponseEntity.ok(userService.getLibraryById(requestingUserId ,page,size));
    }

    @PostMapping("/editLibrary")
    public ResponseEntity<String> editLibrary(@Valid @RequestBody AddGameToLibraryDTO addGame){
        userService.editLibrary(addGame);
        return ResponseEntity.ok("The library has been updated successfully");
    }
    @DeleteMapping("/removeFromLibrary/{gameId}")
    public ResponseEntity<String> removeFromLibrary(@PathVariable @NotNull @Size(min = 24, max = 24) String gameId){
        userService.removeGameFromLibrary(gameId);
        return ResponseEntity.ok("The library has been updated successfully");
    }

    @GetMapping("/friends/{userId}")
    public ResponseEntity<Page<FriendDTO>> showFriendList(@PathVariable @NotNull @Size(min = 24, max = 24) String userId,
                                                          @RequestParam(defaultValue = "0") @Min(0) int page,
                                                          @RequestParam(defaultValue = "25") @Min(1) @Max(50) int size){
        return ResponseEntity.ok(userService.getFriendListById(userId, page, size));
    }

    @GetMapping("/MyFriends")
    public ResponseEntity<Page<FriendDTO>> showOwnFriendList(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                          @RequestParam(defaultValue = "25") @Min(1) @Max(50) int size){
        String requestingUserId = getAuthenticatedUserId();
        return ResponseEntity.ok(userService.getFriendListById(requestingUserId,page, size));
    }

    @GetMapping("/friendRequests")
    public ResponseEntity<List<FriendRequestDTO>> showFriendRequests(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                                     @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size){
        return ResponseEntity.ok(userService.getFriendRequests(page,size));
    }

    @PostMapping("/sendFriendRequest/{targetUserId}")
    public ResponseEntity<String> sendFriendRequest(@PathVariable @NotNull @Size(min = 24, max = 24) String targetUserId){
        return ResponseEntity.ok(userService.sendRequestToUser(targetUserId));
    }

    @PostMapping("/approveFriendRequest/{targetUserId}")
    public ResponseEntity<String> approveFriendRequest(@PathVariable @NotNull  @Size(min = 24, max = 24) String targetUserId){
        String message = userService.approveRequestFromUser(targetUserId);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/denyFriendRequest/{targetUserId}")
    public ResponseEntity<String> denyFriendRequest(@PathVariable @NotNull @Size(min = 24, max = 24) String targetUserId){
        userService.removeRequestFromUser(targetUserId);
        return ResponseEntity.ok("Friend request has been denied successfully");
    }

    @DeleteMapping("/removeFriend/{friendId}")
    public ResponseEntity<String> removeFriend(@PathVariable @NotNull @Size(min = 24, max = 24) String friendId){
        userService.removeFriend(friendId);
        return ResponseEntity.ok("Friend removed successfully");
    }

    @GetMapping("/reviews/{userId}")
    public ResponseEntity<List<ReviewDTO>> getUserReviews(@PathVariable @NotNull @Size(min = 24, max = 24) String userId,
                                                    @RequestParam(defaultValue = "0") @Min(0) int page,
                                                    @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size){
        return ResponseEntity.ok(userService.getUserReviews(userId,page,size));
    }

    @GetMapping("/MyReviews")
    public ResponseEntity<List<ReviewDTO>> getOwnReviews(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                          @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size){

        return ResponseEntity.ok(userService.getUserReviews(getAuthenticatedUserId(),page,size));
    }

    @DeleteMapping("/deleteAccount")
    public ResponseEntity<String> deleteAccount(){

        String userId = getAuthenticatedUserId();
        userService.deleteUser(userId);
        return ResponseEntity.ok("Account Deleted successfully");
    }

    // INTERESTING QUERIES ======================================================
    //TODO ADD VARIABLES
    @GetMapping("/getHardcoreGamers")
    public ResponseEntity<List<PlayerStatsDTO>> getHardcoreGamers(){
        return ResponseEntity.ok(userService.getHardcoreGamers());
    }

    @GetMapping("/getKeyboardWarriors")
    public ResponseEntity<List<KeyboardWarriorDTO>> getKeyboardWarriors(){
        return ResponseEntity.ok(userService.getKeyboardWarriors());
    }

    @GetMapping("/getMostActiveGamers")
    public ResponseEntity<List<ActiveGamerDTO>> getMostActiveGamers(){
        return ResponseEntity.ok(userService.getMostActiveGamers());
    }

}

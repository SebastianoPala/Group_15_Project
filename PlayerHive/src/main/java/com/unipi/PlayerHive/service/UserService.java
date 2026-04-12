package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.LibraryGameDTO;
import com.unipi.PlayerHive.DTO.games.LightGameDTO;
import com.unipi.PlayerHive.DTO.users.*;
import com.unipi.PlayerHive.config.Exceptions.ResourceAlreadyExistsException;
import com.unipi.PlayerHive.model.User;
import com.unipi.PlayerHive.repository.games.GameRepository;
import com.unipi.PlayerHive.repository.users.UserNeo4jRepository;
import com.unipi.PlayerHive.repository.users.UserRepository;
import com.unipi.PlayerHive.model.UserPrincipal;
import com.unipi.PlayerHive.utility.UserMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserNeo4jRepository userNeo4jRepository;
    private final GameRepository gameRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserNeo4jRepository userNeo4jRepository, GameRepository gameRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userNeo4jRepository = userNeo4jRepository;
        this.gameRepository = gameRepository;
        this.userMapper = userMapper;
    }

    // JwtFilter already put the authenticated user in the security context earlier in the request, this just reads it back out :)
    private String getAuthenticatedUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal())
                .getUser().getId();
    }

    public ProfileDTO getProfileById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        return userMapper.userToProfileDTO(user);
    }

    public OwnProfileDTO getOwnProfileById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));

        OwnProfileDTO ownProfile = userMapper.userToOwnProfileDTO(user);

        ownProfile.setFriendRequestsNumber(user.getFriendRequests().size()); // todo ponder if we just send the friend requests and that's it

        return ownProfile;
    }

    public Page<LibraryGameDTO> getLibraryById(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        return userNeo4jRepository.findLibraryById(userId, pageable);
    }

    @Transactional
    public void editLibrary(@Valid AddGameToLibraryDTO addGame) {
        String userId = getAuthenticatedUserId();

        Optional<Double> userGamePlaytime = userNeo4jRepository.findUserGamePlaytime(userId, addGame.getGameId());

        //float totalPlaytime = game.getTotalHoursPlayed() + addGame.getHoursPlayed(); TODO DELAY UPDATE
        float userPlaytime = addGame.getHoursPlayed();
        int gameNumberToAdd = 0;

        // DO I just assume mongodb and neo4j data are synchronized?
        if(userGamePlaytime.isEmpty()){
            // game.setNumPlayers(game.getNumPlayers() + 1 ); TODO DELAY UPDATE
            gameNumberToAdd++;
        }else{
            //totalPlaytime -= userGamePlaytime.get().floatValue();
            userPlaytime -= userGamePlaytime.get().floatValue();
        }
        boolean success = userNeo4jRepository.saveGameInLibrary(userId,addGame.getGameId(),addGame.getHoursPlayed().doubleValue(),addGame.getAchievements());
        if(!success){
            throw new IllegalArgumentException("The achievement number exceeds the game's achievement number");
        }
        //game.setTotalHoursPlayed(totalPlaytime);
        int modified = userRepository.updateUserStats(userId, userPlaytime,gameNumberToAdd);
        if(modified<=0)
            throw new RuntimeException("The server was unable to increase the player's gaming stats");
        //gameRepository.save(game);!! TODO delay update

    }

    @Transactional
    public void removeGameFromLibrary(String gameId) {
        String userId = getAuthenticatedUserId();

        Double userGamePlaytime = userNeo4jRepository.findUserGamePlaytime(userId, gameId)
                .orElseThrow(() -> new NoSuchElementException("The game specified is not present in the user's library"));

        // DO I just assume mongodb and neo4j data are synchronized?
        //game.setNumPlayers(game.getNumPlayers() -1 ); // TODO REMOVE

        //game.setTotalHoursPlayed(game.getTotalHoursPlayed() - userGamePlaytime.floatValue()); // TODO REMOVE
        int modified = userRepository.updateUserStats(userId, -userGamePlaytime.floatValue(),-1);
        if(modified<=0)
            throw new RuntimeException("The server was unable to decrease the player's gaming stats");

        boolean success = userNeo4jRepository.removeGameFromLibrary(userId,gameId);
        if(!success)
            throw new RuntimeException("The server was unable to remove the game from the library");
    }

    public Page<FriendDTO> getFriendListById(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        return userNeo4jRepository.findUsersFriends(userId, pageable);
    }

    public List<FriendRequestDTO> getFriendRequests() {
        String userId = getAuthenticatedUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        return user.getFriendRequests();
    }

    public Slice<UserSearchDTO> searchUser(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);

        Slice<UserSearchDTO> searchResult = userRepository.searchByUsernameContaining(username, pageable);

        if(searchResult.isEmpty()) // do we have to throw the exception?
            throw new NoSuchElementException("No user matches the search parameters");

        return searchResult;
    }

    @Transactional
    public String sendRequestToUser(String targetUserId) {

        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User user = principal.getUser();
        String userId = user.getId();

        if(userId.equalsIgnoreCase(targetUserId)){
            throw new IllegalArgumentException("The user attempted to send a request to himself");
        }
        if(userNeo4jRepository.checkFriendshipExistence(userId,targetUserId)){
            throw new ResourceAlreadyExistsException("The users are already friends");
        }

        try { // we first check if we already have a request from targetUser
            this.approveRequestFromUser(targetUserId); //TODO TRANSACTIONAL DOES not work in function calls
            return "The friendship has been established";

        } catch (NoSuchElementException ignored) {} // if no friend request was present, NoSuchElementException is thrown

        FriendRequestMongoDTO requestDTO = new FriendRequestMongoDTO(new ObjectId(userId),user.getUsername(),user.getPfpURL(), LocalDateTime.now());

        int modified = userRepository.addFriendRequest(targetUserId,requestDTO.getUserId(),requestDTO); // add controls to return value
        if(modified != 1)
            throw new ResourceAlreadyExistsException("The friend request is already present");

        return "Friend request sent successfully";
    }

    @Transactional
    public void approveRequestFromUser(String targetUserId) {
        String userId = getAuthenticatedUserId();

        int result = userRepository.acceptFriendRequest(userId,targetUserId);
        if(result != 1)
            throw new NoSuchElementException("Friend request was not present!");

        result = userRepository.editFriendCounter(targetUserId, 1);
        if(result != 1)
            throw new RuntimeException("The server could't increase the user's friend counter");

        boolean success = userNeo4jRepository.createFriendship(userId,targetUserId);
        if(!success)
            throw new RuntimeException("The server was unable to complete the operation");

    }

    public void removeRequestFromUser(String targetUserId) {
        String userId = getAuthenticatedUserId();
        //if(!userRepository.existsById(userId)) if the user does not exist, the request will simply not be present
        // ...
        int result = userRepository.removeFriendRequest(userId,targetUserId);
        if(result != 1)
            throw new NoSuchElementException("Friend request was not present!");
    }

    @Transactional
    public void removeFriend(String friendId) {
        String userId = getAuthenticatedUserId();

        boolean success = userNeo4jRepository.removeFriendById(userId,friendId);
        if(!success){
            throw new NoSuchElementException("No Friend was found matching the given Id");
        }

        int result = userRepository.editFriendCounter(userId, -1);
        if(result != 1)
            throw new RuntimeException("The server was unable to decrease the friend counter");
    }

}

package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.LibraryGameDTO;
import com.unipi.PlayerHive.DTO.users.*;
import com.unipi.PlayerHive.model.Game;
import com.unipi.PlayerHive.model.User;
import com.unipi.PlayerHive.repository.games.GameRepository;
import com.unipi.PlayerHive.repository.users.UserNeo4jRepository;
import com.unipi.PlayerHive.repository.users.UserRepository;
import com.unipi.PlayerHive.utility.UserMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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

    public ProfileDTO getProfileById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        return userMapper.userToProfileDTO(user);
    }
    public OwnProfileDTO getOwnProfileById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));

        OwnProfileDTO ownProfile = userMapper.userToOwnProfileDTO(user);

        ownProfile.setFriendRequestsNumber(user.getFriendRequests().size());

        return ownProfile;
    }

    public List<LibraryGameDTO> getLibraryById(String userId) {
        return userNeo4jRepository.findLibraryById(userId);
    }

    @Transactional
    public void editLibrary(@Valid AddGameToLibraryDTO addGame) {
        String userId = "08c5af2f8ce54ffea778ad15"; //will be obtained by token
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("The user you are trying to access does not exist"));
        // TODO
        // FIX

        Game game = gameRepository.findById(addGame.getGameId()).orElseThrow(() -> new NoSuchElementException("The game specified does not exist"));
        if(addGame.getAchievements() > game.getAchievements()){
            throw new IllegalArgumentException("The achievement number exceeds the game's achievement number"); // ADD TO EXCEPTION MANAGER
        }
        Optional<Double> userGamePlaytime = userNeo4jRepository.findUserGamePlaytime(userId, game.getId());

        float totalPlaytime = game.getTotalHoursPlayed() + addGame.getHoursPlayed();
        float userPlaytime = user.getHoursPlayed() + addGame.getHoursPlayed();

        // DO I just assume mongodb and neo4j data are synchronized?
        if(userGamePlaytime.isEmpty()){
            game.setNumPlayers(game.getNumPlayers() + 1 );
            user.setNumGames(user.getNumGames() + 1);
        }else{
           totalPlaytime -= userGamePlaytime.get().floatValue();
           userPlaytime -= userGamePlaytime.get().floatValue();
        }
        game.setTotalHoursPlayed(totalPlaytime);
        user.setHoursPlayed(userPlaytime);

        gameRepository.save(game);
        userRepository.save(user);
        userNeo4jRepository.saveGameInLibrary(userId,addGame.getGameId(),addGame.getHoursPlayed().doubleValue(),addGame.getAchievements());
    }

    @Transactional
    public void removeGameFromLibrary(String gameId) {
        String userId = "08c5af2f8ce54ffea778ad15"; //will be obtained by token
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("The user you are trying to access does not exist"));
        // TODO
        // FIX

        Game game = gameRepository.findById(gameId).orElseThrow(() -> new NoSuchElementException("The game specified does not exist"));

        Double userGamePlaytime = userNeo4jRepository.findUserGamePlaytime(userId, game.getId())
                .orElseThrow(() -> new NoSuchElementException("The game specified is not present in the user's library"));

        // DO I just assume mongodb and neo4j data are synchronized?
        game.setNumPlayers(game.getNumPlayers() -1 );
        user.setNumGames(user.getNumGames() - 1);

        game.setTotalHoursPlayed(game.getTotalHoursPlayed() - userGamePlaytime.floatValue());
        user.setHoursPlayed(user.getHoursPlayed() - userGamePlaytime.floatValue());
        gameRepository.save(game);
        userRepository.save(user);
        userNeo4jRepository.removeGameFromLibrary(userId,gameId);
    }

    public List<FriendDTO> getFriendListById(String userId) {
        return userNeo4jRepository.findUsersFriends(userId);
    }

    public List<FriendRequestDTO> getFriendRequestsById(String userId) { // THE STRING MUST BE OBTAINED BY THE TOKEN TODO
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        return user.getFriendRequests();
    }

    public List<UserSearchDTO> searchUser(String username) {
        List<User> searchResult = userRepository.searchByUsername(username).orElseThrow(() -> new NoSuchElementException("User not found"));

        return searchResult.stream()
                .map(userMapper::userToUserSearchDTO)
                .toList();
    }

    public void sendRequestToUser(String targetUserId) {
        String userId ="a1ab4293a9804029882b411f";
        // is there a way to avoid this query?? check auth maybe, we need the pfp link
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Who is bro -_-"));
        FriendRequestDTO requestDTO = new FriendRequestDTO(userId,user.getUsername(),user.getPfpURL(), LocalDateTime.now());
        userRepository.addFriendRequest(targetUserId,userId,requestDTO);
    }

    public void approveRequestFromUser(String targetUserId) {
        this.removeRequestFromUser(targetUserId);
        String userId ="07b4280e2bec46419547f8e1";
        userNeo4jRepository.createFriendship(userId,targetUserId);

    }

    public void removeRequestFromUser(String targetUserId) {
        String userId ="07b4280e2bec46419547f8e1";
        //if(!userRepository.existsById(userId)) if the user does not exist, the request will simply not be present
        // ...
        int result = userRepository.removeFriendRequest(userId,targetUserId);
        if(result != 1)
            throw new NoSuchElementException("Friend request was not present!");
    }

    public void removeFriend(String friendId) {
        String userId ="07b4280e2bec46419547f8e1";
        userNeo4jRepository.removeFriendById(userId,friendId);
    }

}

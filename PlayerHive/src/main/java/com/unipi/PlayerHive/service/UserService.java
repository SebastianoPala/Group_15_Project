package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.LibraryGameDTO;
import com.unipi.PlayerHive.DTO.users.*;
import com.unipi.PlayerHive.model.User;
import com.unipi.PlayerHive.repository.users.UserNeo4jRepository;
import com.unipi.PlayerHive.repository.users.UserRepository;
import com.unipi.PlayerHive.utility.UserMapper;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserNeo4jRepository userNeo4jRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserNeo4jRepository userNeo4jRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userNeo4jRepository = userNeo4jRepository;
        this.userMapper = userMapper;
    }

    public ProfileDTO getProfileById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.userToProfileDTO(user);
    }
    public OwnProfileDTO getOwnProfileById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        OwnProfileDTO ownProfile = userMapper.userToOwnProfileDTO(user);
        ownProfile.setFriendRequestsNumber(user.getFriendRequests().size());
        return ownProfile;
    }

    public List<LibraryGameDTO> getLibraryById(String userId) {
        return userNeo4jRepository.findLibraryById(userId);
    }

    public void editLibrary(@Valid AddGameToLibraryDTO addGame) {
        // FIX
        String userId = "516b42fbf46d4c32b8dc41eb"; //will be obtained by token
        userNeo4jRepository.saveGameInLibrary(userId,addGame.getGameId(),addGame.getHoursPlayed(),addGame.getAchievements()); // maybe add checks for achievement numbers
    }

    public void removeGameFromLibrary(String gameId) {
        String userId = "516b42fbf46d4c32b8dc41eb";
        userNeo4jRepository.removeGameFromLibrary(userId,gameId);
    }

    public List<FriendDTO> getFriendListById(String userId) {
        return userNeo4jRepository.findUsersFriends(userId);
    }

    public List<FriendRequestDTO> getFriendRequestsById(String userId) { // THE STRING MUST BE OBTAINED BY THE TOKEN TODO
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getFriendRequests();
    }

    public List<UserSearchDTO> searchUser(String username) {
        List<User> searchResult = userRepository.searchByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        return searchResult.stream()
                .map(userMapper::userToUserSearchDTO)
                .toList();
    }

    public void sendRequestToUser(String targetUserId) {
        String userId ="a1ab4293a9804029882b411f";
        // is there a way to avoid this query?? check auth maybe, we need the pfp link
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Who is bro -_-"));
        FriendRequestDTO requestDTO = new FriendRequestDTO(userId,user.getUsername(),user.getPfpURL(), LocalDateTime.now());
        userRepository.addFriendRequest(targetUserId,userId,requestDTO);
    }

    public void approveRequestFromUser(String targetUserId) {
        String userId ="07b4280e2bec46419547f8e1";
        //if(!userRepository.existsById(userId)) if the user does not exist, the request will simply not be present
            // ...
        int result = userRepository.removeFriendRequest(userId,targetUserId);
        if(result != 1)
            throw new RuntimeException("Friend request was not present!");
        userNeo4jRepository.createFriendship(userId,targetUserId);

    }

    public void denyRequestFromUser(String targetUserId) {
        String userId ="07b4280e2bec46419547f8e1";
        int result = userRepository.removeFriendRequest(userId,targetUserId);
        if(result != 1)
            throw new RuntimeException("Friend request was not present!");
    }

    public void removeFriend(String friendId) {
        String userId ="07b4280e2bec46419547f8e1";
        userNeo4jRepository.removeFriendById(userId,friendId);
    }

}

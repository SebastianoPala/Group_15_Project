package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.GameSearchDTO;
import com.unipi.PlayerHive.DTO.users.*;
import com.unipi.PlayerHive.model.Game;
import com.unipi.PlayerHive.model.User;
import com.unipi.PlayerHive.model.UserNeo4j;
import com.unipi.PlayerHive.repository.users.UserNeo4jRepository;
import com.unipi.PlayerHive.repository.users.UserRepository;
import com.unipi.PlayerHive.utility.UserMapper;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

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

    public LibraryDTO getLibraryById(String userId) {
        return new LibraryDTO();
    }

    public void addGameToLibrary(@Valid AddGameDTO addGameDTO) {
    }

    public void removeGameFromLibrary(String gameId) {
    }

    public List<FriendDTO> getFriendListById(String userId) {
        return null;
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

    public void sendRequestToUser(String userId) {
    }

    public void approveRequestFromUser(String userId) {
    }

    public void denyRequestFromUser(String userId) {
    }

    public void removeFriend(String userId) {
    }

}

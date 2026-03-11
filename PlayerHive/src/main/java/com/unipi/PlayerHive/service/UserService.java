package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.users.*;
import com.unipi.PlayerHive.model.UserNeo4j;
import com.unipi.PlayerHive.repository.users.UserNeo4jRepository;
import com.unipi.PlayerHive.repository.users.UserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserNeo4jRepository userNeo4jRepository;

    public ProfileDTO getProfileById(String userId) {
        return new ProfileDTO();
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

    public List<FriendRequestDTO> getFriendRequestsById(String userId) {
        return null;
    }

    public List<UserSearchDTO> searchUser(String query) {
        return null;
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

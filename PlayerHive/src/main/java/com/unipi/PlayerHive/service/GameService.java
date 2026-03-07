package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.GameInfoDTO;
import com.unipi.PlayerHive.DTO.games.GameReducedDTO;
import com.unipi.PlayerHive.DTO.games.UserGameInfoDTO;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    public GameInfoDTO getGameById(String gameId) {
        return null;
    }

    public List<GameReducedDTO> searchGameById(String gameName) {
        return List.of();
    }

    public void addGame(@Valid GameInfoDTO gameInfo) {
    }

    public void editGame(GameInfoDTO gameInfo) {
    }

    public void deleteGame(String gameId) {
    }
}

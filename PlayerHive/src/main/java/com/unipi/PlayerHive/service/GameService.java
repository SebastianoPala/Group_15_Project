package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.GameInfoDTO;
import com.unipi.PlayerHive.DTO.games.GameSearchDTO;
import com.unipi.PlayerHive.DTO.games.addReviewDTO;
import com.unipi.PlayerHive.model.Game;
import com.unipi.PlayerHive.repository.games.GameNeo4jRepository;
import com.unipi.PlayerHive.repository.games.GameRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameNeo4jRepository gameNeo4jRepository;

    public GameService(GameRepository gameRepository,
                       GameNeo4jRepository gameNeo4jRepository
                       ){
        this.gameRepository = gameRepository;
        this.gameNeo4jRepository = gameNeo4jRepository;
    }

    public GameInfoDTO getGameById(String gameId) { // manage high reviews number case
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        return new GameInfoDTO(game);
    }

    public List<GameSearchDTO> searchGameByName(String gameName) { // paginate results
        List<Game> searchResult = gameRepository.searchByName(gameName).orElseThrow(() -> new RuntimeException("Game not found"));

        return searchResult.stream()
                .map(GameSearchDTO::new)
                .toList();
    }

    public void addGame(@Valid GameInfoDTO gameInfo) {
    }

    public void editGame(GameInfoDTO gameInfo) {
    }

    public void deleteGame(String gameId) {
    }

    public void addReview(@Valid addReviewDTO addReviewDTO) {
    }


    public void deleteReviewFromGame(String gameId) { // where do we get the username from??
    }
}

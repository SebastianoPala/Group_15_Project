package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.GameInfoDTO;
import com.unipi.PlayerHive.DTO.games.GameSearchDTO;
import com.unipi.PlayerHive.DTO.games.addReviewDTO;
import com.unipi.PlayerHive.model.Game;
import com.unipi.PlayerHive.repository.GameNeo4jRepository;
import com.unipi.PlayerHive.repository.GameRepository;
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

    public GameInfoDTO getGameById(String gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        return new GameInfoDTO(
                game.getName(),
                game.getReleaseDate(),
                game.getPrice(),
                game.getDiscount(),
                game.getDescription(),
                game.getReviews(),
                game.getImageURL(),
                game.getSupportedOS(),
                game.getAchievements(),
                game.getUserScore(),
                game.getAveragePlaytime(),
                game.getDevelopers(),
                game.getPublishers(),
                game.getGenres()
        );
    }

    public List<GameSearchDTO> searchGameById(String gameName) {
        return null;
    }

    public void addGame(@Valid GameInfoDTO gameInfo) {
    }

    public void editGame(GameInfoDTO gameInfo) {
    }

    public void deleteGame(String gameId) {
    }

    public void addReview(@Valid addReviewDTO addReviewDTO) {
    }


    public void deleteReviewFromGame(String gameId) {
    }
}

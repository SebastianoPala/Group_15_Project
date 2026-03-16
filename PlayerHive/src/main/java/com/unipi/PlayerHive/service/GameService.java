package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.EditGameDTO;
import com.unipi.PlayerHive.DTO.games.GameInfoDTO;
import com.unipi.PlayerHive.DTO.games.GameSearchDTO;
import com.unipi.PlayerHive.DTO.games.addReviewDTO;
import com.unipi.PlayerHive.model.Game;
import com.unipi.PlayerHive.model.GameNeo4j;
import com.unipi.PlayerHive.repository.games.GameNeo4jRepository;
import com.unipi.PlayerHive.repository.games.GameRepository;
import com.unipi.PlayerHive.utility.GameMapper;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameNeo4jRepository gameNeo4jRepository;
    private final GameMapper gameMapper;

    public GameService(GameRepository gameRepository,
                       GameNeo4jRepository gameNeo4jRepository, GameMapper gameMapper
    ){
        this.gameRepository = gameRepository;
        this.gameNeo4jRepository = gameNeo4jRepository;
        this.gameMapper = gameMapper;
    }

    public GameInfoDTO getGameById(String gameId) { // manage high reviews number case
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        return gameMapper.gameToGameInfoDTO(game);
    }

    public List<GameSearchDTO> searchGameByName(String gameName) { // paginate results
        List<Game> searchResult = gameRepository.searchByName(gameName).orElseThrow(() -> new RuntimeException("Game not found"));
        return searchResult.stream()
                .map(gameMapper::gameToGameSearchDTO)
                .toList();
    }

    public void addGame(@Valid EditGameDTO newGame) {
        Game game = gameMapper.editGameDTOtoGame(newGame);
        game.setReviews(new ArrayList<>());
        System.out.println("Attempting mongodb save");
        Game addedGame= gameRepository.save(game);
        System.out.println("mongodb saved. attempting neo4j...");
        GameNeo4j GameN4j= new GameNeo4j(addedGame.getId(), game.getName(),game.getAchievements(),game.getImageURL());
        gameNeo4jRepository.save(GameN4j);
        System.out.println("neo4j saved");
    }

    public void editGame(String gameId, EditGameDTO editGame) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        // is there a better way??
        if (editGame.getName() != null) {
            game.setName(editGame.getName());
        }
        if (editGame.getReleaseDate() != null) {
            game.setReleaseDate(editGame.getReleaseDate());
        }
        if (editGame.getPrice() != null) {
            game.setPrice(editGame.getPrice());
        }
        if (editGame.getDiscount() != null) {
            game.setDiscount(editGame.getDiscount());
        }
        if (editGame.getDescription() != null) {
            game.setDescription(editGame.getDescription());
        }
        if (editGame.getImageURL() != null) {
            game.setImageURL(editGame.getImageURL());
        }
        if (editGame.getSupportedOS() != null) {
            game.setSupportedOS(editGame.getSupportedOS());
        }
        if (editGame.getAchievements() != null) {
            game.setAchievements(editGame.getAchievements());
        }
        if (editGame.getDevelopers() != null) {
            game.setDevelopers(editGame.getDevelopers());
        }
        if (editGame.getPublishers() != null) {
            game.setPublishers(editGame.getPublishers());
        }
        if (editGame.getGenres() != null) {
            game.setGenres(editGame.getGenres());
        }

        gameRepository.save(game);

        GameNeo4j gameNeo = gameNeo4jRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found on Neo4j"));

        if (editGame.getName() != null) {
            gameNeo.setName(editGame.getName());
        }
        if (editGame.getImageURL() != null) {
            gameNeo.setImage(editGame.getImageURL());
        }
        if (editGame.getAchievements() != null) {
            gameNeo.setAchievements(editGame.getAchievements());
        }

        gameNeo4jRepository.save(gameNeo);
    }

    public void deleteGame(String gameId) {
    }

    public void addReview(@Valid addReviewDTO addReviewDTO) {
    }


    public void deleteReviewFromGame(String gameId) { // where do we get the username from??
    }
}

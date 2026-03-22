package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.AddGameDTO;
import com.unipi.PlayerHive.DTO.games.EditGameDTO;
import com.unipi.PlayerHive.config.Exceptions.ResourceAlreadyExistsException;
import com.unipi.PlayerHive.model.Game;
import com.unipi.PlayerHive.model.GameNeo4j;
import com.unipi.PlayerHive.repository.games.GameNeo4jRepository;
import com.unipi.PlayerHive.repository.games.GameRepository;
import com.unipi.PlayerHive.utility.GameMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.NoSuchElementException;

@Service
public class AdminService {
    private final GameRepository gameRepository;
    private final GameNeo4jRepository gameNeo4jRepository;
    private final GameMapper gameMapper;

    public AdminService(GameRepository gameRepository, GameNeo4jRepository gameNeo4jRepository, GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.gameNeo4jRepository = gameNeo4jRepository;
        this.gameMapper = gameMapper;
    }

    @Transactional
    public void addGame(@Valid @RequestBody AddGameDTO newGame) {

        if(gameRepository.existsByName(newGame.getName()))
                throw new ResourceAlreadyExistsException("Game "+ newGame.getName() +" already exists");

        Game game = gameMapper.editGameDTOtoGame(newGame);

        game.setAllReviews(new ArrayList<>()); // new games obviously have no reviews
        game.setRecentReviews(new ArrayList<>());
        game.setTotalHoursPlayed((float) 0); // can this be added in the entity?
        game.setNumPlayers(0);
        game.setSumScore((float) 0);
        game.setCountScore(0);

        Game addedGame = gameRepository.save(game); // I need the game ID from MongoDb for Neo4j

        GameNeo4j gameN4j= new GameNeo4j(addedGame.getId(), game.getName(),game.getAchievements(),game.getImageURL());

        gameNeo4jRepository.save(gameN4j);
    }

    @Transactional
    public void editGame(String gameId, @Valid @RequestBody EditGameDTO editGame) {

        Game game = gameRepository.findById(gameId).orElseThrow(() -> new NoSuchElementException("The Game with id:\"" + gameId + "\" does not exist"));
        // is there a better way??
        if (!game.getName().equals(editGame.getName())) { // avoids throwing an exception if I modify the game name to itself
            if(gameRepository.existsByName(editGame.getName()))
                throw new ResourceAlreadyExistsException("Game "+ editGame.getName() +" already exists");
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
        GameNeo4j gameNeo = gameNeo4jRepository.findById(gameId).orElseThrow(() -> new NoSuchElementException("Game not found on Neo4j"));

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

    @Transactional
    public void deleteGame(String gameId) { // fix all user libraries <TODO>
        gameRepository.deleteById(gameId);
        gameNeo4jRepository.deleteById(gameId);
    }
}

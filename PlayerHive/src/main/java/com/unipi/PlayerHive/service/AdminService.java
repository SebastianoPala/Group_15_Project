package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.AddGameDTO;
import com.unipi.PlayerHive.DTO.games.EditGameDTO;
import com.unipi.PlayerHive.DTO.users.GameOwnerDTO;
import com.unipi.PlayerHive.config.Exceptions.ResourceAlreadyExistsException;
import com.unipi.PlayerHive.model.Game;
import com.unipi.PlayerHive.model.GameNeo4j;
import com.unipi.PlayerHive.repository.ReviewRepository;
import com.unipi.PlayerHive.repository.games.GameNeo4jRepository;
import com.unipi.PlayerHive.repository.games.GameRepository;
import com.unipi.PlayerHive.utility.batch.UserConsistencyManager;
import com.unipi.PlayerHive.utility.map.GameMapper;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

@Service
public class AdminService {
    private final GameRepository gameRepository;
    private final GameNeo4jRepository gameNeo4jRepository;
    private final GameMapper gameMapper;
    private final MongoTemplate mongoTemplate;

    private final ReviewRepository reviewRepository;

    public AdminService(GameRepository gameRepository, GameNeo4jRepository gameNeo4jRepository, GameMapper gameMapper, MongoTemplate mongoTemplate, ReviewRepository reviewRepository) {
        this.gameRepository = gameRepository;
        this.gameNeo4jRepository = gameNeo4jRepository;
        this.gameMapper = gameMapper;
        this.mongoTemplate = mongoTemplate;
        this.reviewRepository = reviewRepository;
    }

    // This function copies all the non-null fields from source to target, and only matches fields with the same name
    public static void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        return Stream.of(src.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(name -> src.getPropertyValue(name) == null)
                .toArray(String[]::new);
    }

    @Transactional
    public void addGame(@Nonnull @Valid @RequestBody AddGameDTO newGame) {

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
    public void editGame(String gameId, @Nonnull @Valid @RequestBody EditGameDTO editGame) {

        Game game = gameRepository.findById(gameId).orElseThrow(() -> new NoSuchElementException("The Game with id:\"" + gameId + "\" does not exist"));

        copyNonNullProperties(editGame,game);
        if (!game.getName().equals(editGame.getName()) && gameRepository.existsByName(editGame.getName())) { // avoids throwing an exception if I modify the game name to itself
            throw new ResourceAlreadyExistsException("Game "+ editGame.getName() +" already exists");
        }

        gameRepository.save(game);
        GameNeo4j gameNeo = gameNeo4jRepository.findById(gameId).orElseThrow(() -> new NoSuchElementException("Game not found on Neo4j"));

        copyNonNullProperties(editGame,gameNeo);

        gameNeo4jRepository.save(gameNeo);
    }

    @Transactional
    public void deleteGame(String gameId) {

        if(!gameRepository.existsById(gameId)){
            throw new NoSuchElementException("The game chosen for deletion does not exist");
        }

        // we obtain from neo4j all the relationships that point to the game of interest
        Stream<GameOwnerDTO> allOwners = gameNeo4jRepository.findGameOwnersOf(gameId);

        //the user consistency manager requires mongoTemplate for batch operation
        UserConsistencyManager userManager = new UserConsistencyManager(mongoTemplate);

        //todo: this operation is super heavy, but games are basically never deleted, especially popular ones
        //todo: if we do not perform the user update here, it makes the user related queries heavier
        //all users "hoursPlayed" and "numGames" are decreased accordingly
        userManager.adjustUserStatsAfterGameRemoval(allOwners.iterator());

        allOwners.close();

        //all reviews are now deleted
        reviewRepository.removeByGameId(gameId);

        //the game node in neo4j is removed
        gameNeo4jRepository.deleteById(gameId);

        // all reviews of the game are removed from the reviews array present in every user document
        userManager.removeGameReviewsFromUsers(gameId,gameRepository);

        //we can finally delete the JSON document
        gameRepository.deleteById(gameId);
    }

}

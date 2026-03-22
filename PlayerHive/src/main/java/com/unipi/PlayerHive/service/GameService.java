package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.*;
import com.unipi.PlayerHive.model.Review;
import com.unipi.PlayerHive.repository.ReviewRepository;
import com.unipi.PlayerHive.repository.games.GameNeo4jRepository;
import com.unipi.PlayerHive.repository.games.GameRepository;
import com.unipi.PlayerHive.utility.GameMapper;
import com.unipi.PlayerHive.utility.ReviewMapper;
import jakarta.transaction.Transactional;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.NoSuchElementException;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameNeo4jRepository gameNeo4jRepository; // probably removable
    private final GameMapper gameMapper;

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    public GameService(GameRepository gameRepository,
                       GameNeo4jRepository gameNeo4jRepository, GameMapper gameMapper, ReviewRepository reviewRepository, ReviewMapper reviewMapper
    ){
        this.gameRepository = gameRepository;
        this.gameNeo4jRepository = gameNeo4jRepository;
        this.gameMapper = gameMapper;
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
    }

    public GameInfoDTO getGameById(String gameId) { // manage high reviews number case
        LightGameDTO game = gameRepository.findByIdLight(gameId).orElseThrow(() -> new NoSuchElementException("Game not found"));

        GameInfoDTO gameInfo = gameMapper.gameLightDTOToGameInfoDTO(game);

        Float userScore = (game.getCountScore() > 0) ? game.getSumScore() / game.getCountScore() : null;
        gameInfo.setUserScore(userScore);

        Float avgPlay = (game.getNumPlayers() > 0) ? game.getTotalHoursPlayed() / game.getNumPlayers() : 0;
        gameInfo.setAveragePlaytime(avgPlay);

        return gameInfo;
    }

    public Slice<GameSearchDTO> searchGameByName(String gameName, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Slice<GameSearchDTO> searchResult = gameRepository.searchByNameContaining(gameName, pageable);

        if(searchResult.isEmpty()) // do we have to throw an exception?
            throw new NoSuchElementException("No games matching the search parameters were found");

        return searchResult;
    }

    @Transactional
    public void addReview(String gameId, addReviewDTO addReviewDTO) {
        String userId ="aodawjiodjawi9awd"; // FIX TODO, also do we need to check if the user is valid? -_-
        String userPFPurl = "fortnite.com";
        String username = "theChillRapist";
        // TODO. PREVENTR USER FROM DOUBEL REVIEWING
        Review review = new Review(null,gameId,userId,username,userPFPurl,addReviewDTO.getReviewText(), addReviewDTO.getScore(), LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);
        RecentReviewDTO recent = reviewMapper.reviewToRecentReviewDTO(savedReview);
        int modified = gameRepository.addReviewToGame(gameId, new ObjectId(savedReview.getId()), recent, recent.getScore());
        if(modified != 1)
            throw new RuntimeException("An error has occurred");
    }


    public void deleteReviewFromGame(String gameId) { // where do we get the username from??
    }

    //public ???? getGameReviews(String gameId, int page, int size) {

    //}
}

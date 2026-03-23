package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.*;
import com.unipi.PlayerHive.DTO.reviews.OldReviewDTO;
import com.unipi.PlayerHive.DTO.reviews.ReviewContainerDTO;
import com.unipi.PlayerHive.DTO.reviews.ReviewDTO;
import com.unipi.PlayerHive.DTO.reviews.AddReviewDTO;
import com.unipi.PlayerHive.config.Exceptions.ResourceAlreadyExistsException;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
    public void addReview(String gameId, AddReviewDTO addReviewDTO) {
        String userId ="2c6d3a290cee4e9bb0c3b8bc"; // FIX TODO, also do we need to check if the user is valid? -_-
        String userPFPurl = "fortnite.com";
        String username = "theChillRapist";

        ObjectId userIdObj = new ObjectId(userId);

        if(gameRepository.hasUserAlreadyReviewed(gameId, userIdObj)){
            throw new ResourceAlreadyExistsException("The user already reviewed this game");
        }

        Review review = new Review(null,new ObjectId(gameId),userIdObj,username,userPFPurl,
                                            addReviewDTO.getReviewText(), addReviewDTO.getScore(), LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        ReviewDTO recentReview = reviewMapper.reviewToRecentReviewDTO(savedReview);

        OldReviewDTO oldReview = new OldReviewDTO(new ObjectId(recentReview.getId()),userIdObj);

        int modified = gameRepository.addReviewToGame(gameId,oldReview , recentReview, recentReview.getScore());
        if(modified != 1)
            throw new RuntimeException("An error has occurred when adding the review to the game");
    }

    @Transactional
    public void deleteReview(String reviewId) {
        String userId = "1911c59f6d93465999276f1e";
        Review deletedReview = reviewRepository.removeByIdAndUserId(reviewId, new ObjectId(userId)).orElseThrow(() -> new NoSuchElementException("The review provided does not exist, or it belongs to a different user"));

        int modified = gameRepository.deleteReviewFromGame(deletedReview.getGameId(),deletedReview.getId(),-deletedReview.getScore());
        if(modified != 1)
            throw new RuntimeException("The server couldn't delete the review due to inconsistencies");

    }

    public List<ReviewDTO> getGameReviews(String gameId, int page, int size) {
    // IMPORTANT TO RETURN THE REVIEW ID TO ALLOW FOR DELETE
        int reviewNumber = gameRepository.getReviewNumber(gameId);

        int startingReverseIndex = reviewNumber - page*size - 1;

        if(startingReverseIndex < 0)
            throw new NoSuchElementException("No reviews have been found at page: "+ page+", size: "+size); // todo maybe we should return 200 anyways?
        size = (startingReverseIndex + 1 - size < 0) ? startingReverseIndex + 1 : size;

        int startingIndex = startingReverseIndex - size + 1;

        ReviewContainerDTO reviewContainer = gameRepository.getGameReviews(gameId,startingIndex,size);

        List<String> reviewIds = reviewContainer.getReviews().stream().map(oldReviewDTO ->
                                    oldReviewDTO.getReviewId().toString()).toList();
        return reviewRepository.findByIdInOrderByTimestampDesc(reviewIds);
    }
}

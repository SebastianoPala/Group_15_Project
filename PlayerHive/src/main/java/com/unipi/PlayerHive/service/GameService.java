package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.*;
import com.unipi.PlayerHive.DTO.reviews.OldGameReviewDTO;
import com.unipi.PlayerHive.DTO.reviews.GameReviewContainerDTO;
import com.unipi.PlayerHive.DTO.reviews.ReviewDTO;
import com.unipi.PlayerHive.DTO.reviews.AddReviewDTO;
import com.unipi.PlayerHive.DTO.reviews.UserReviewDTO;
import com.unipi.PlayerHive.config.Exceptions.ResourceAlreadyExistsException;
import com.unipi.PlayerHive.model.Review;
import com.unipi.PlayerHive.model.User;
import com.unipi.PlayerHive.model.UserPrincipal;
import com.unipi.PlayerHive.repository.ReviewRepository;
import com.unipi.PlayerHive.repository.games.GameNeo4jRepository;
import com.unipi.PlayerHive.repository.games.GameRepository;
import com.unipi.PlayerHive.repository.users.UserRepository;
import com.unipi.PlayerHive.utility.map.GameMapper;
import com.unipi.PlayerHive.utility.map.ReviewMapper;
import jakarta.transaction.Transactional;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameNeo4jRepository gameNeo4jRepository; // probably removable
    private final GameMapper gameMapper;

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;

    public GameService(GameRepository gameRepository,
                       GameNeo4jRepository gameNeo4jRepository, GameMapper gameMapper, ReviewRepository reviewRepository, ReviewMapper reviewMapper, UserRepository userRepository
    ){
        this.gameRepository = gameRepository;
        this.gameNeo4jRepository = gameNeo4jRepository;
        this.gameMapper = gameMapper;
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.userRepository = userRepository;
    }

    // JwtFilter already put the authenticated user in the security context earlier in the request, this just reads it back out
    private String getAuthenticatedUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal())
                .getUser().getId();
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
        // pull the actual logged-in user from the token, no more hardcoded test data :0
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User user = principal.getUser();
        String userId = user.getId();
        String userPFPurl = user.getPfpURL();
        String username = user.getUsername();

        ObjectId userIdObj = new ObjectId(userId);
        // TODO: move the check in the user array, since it will be shorter
        if(gameRepository.hasUserAlreadyReviewed(gameId, userIdObj)){
            throw new ResourceAlreadyExistsException("The user already reviewed this game");
        }

        Review review = new Review(null,new ObjectId(gameId),userIdObj,username,userPFPurl,
                                            addReviewDTO.getReviewText(), addReviewDTO.getScore(), LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        ReviewDTO recentReview = reviewMapper.reviewToRecentReviewDTO(savedReview);

        OldGameReviewDTO oldReview = new OldGameReviewDTO(new ObjectId(recentReview.getId()),userIdObj);

        int modified = gameRepository.addReviewToGame(gameId,oldReview , recentReview, recentReview.getScore());
        if(modified != 1)
            throw new RuntimeException("An error has occurred when adding the review to the game");

        // keep a lightweight {reviewId, gameId} entry on the user so we can clean up their reviews if they ever get deleted
        UserReviewDTO userReview = new UserReviewDTO(new ObjectId(savedReview.getId()), new ObjectId(gameId));
        userRepository.addReviewToUser(userId, userReview);
    }

    @Transactional
    public void deleteReview(String reviewId) {
        Review deletedReview = reviewRepository.removeById(reviewId).orElseThrow(() -> new NoSuchElementException("The review provided does not exist"));

        // only the review author or an admin can delete this, anyone else gets rejected :/
        String requesterId = getAuthenticatedUserId();
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        // TODO, can we make this prettier? it kinda stinks
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !deletedReview.getUserId().toHexString().equals(requesterId)) {
            throw new IllegalArgumentException("You can only delete your own reviews");
        }

        int modified = gameRepository.deleteReviewFromGame(deletedReview.getGameId(),deletedReview.getId(),-deletedReview.getScore());
        if(modified != 1)
            throw new RuntimeException("The server couldn't delete the review due to inconsistencies");

        // clean the entry out of the user's reviewIds array too
        userRepository.removeReviewFromUser(requesterId, new ObjectId(reviewId));

    }

    public List<ReviewDTO> getGameReviews(String gameId, int page, int size) {
    // IMPORTANT TO RETURN THE REVIEW ID TO ALLOW FOR DELETE
        int reviewNumber = gameRepository.getReviewNumber(gameId);

        int startingReverseIndex = reviewNumber - page*size - 1;

        if(startingReverseIndex < 0)
            throw new NoSuchElementException("No reviews have been found at page: "+ page+", size: "+size); // todo maybe we should return 200 anyways?
        size = (startingReverseIndex + 1 - size < 0) ? startingReverseIndex + 1 : size;

        int startingIndex = startingReverseIndex - size + 1;

        GameReviewContainerDTO reviewContainer = gameRepository.getGameReviews(gameId,startingIndex,size);

        List<String> reviewIds = reviewContainer.getReviews().stream().map(oldReviewDTO ->
                                    oldReviewDTO.getReviewId().toString()).toList();
        return reviewRepository.findByIdInOrderByTimestampDesc(reviewIds);
    }
}

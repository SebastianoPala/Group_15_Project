package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.LibraryGameDTO;
import com.unipi.PlayerHive.DTO.reviews.ReviewDTO;
import com.unipi.PlayerHive.DTO.reviews.UserReviewContainerDTO;
import com.unipi.PlayerHive.DTO.users.*;
import com.unipi.PlayerHive.config.Exceptions.ResourceAlreadyExistsException;
import com.unipi.PlayerHive.model.User;
import com.unipi.PlayerHive.repository.ReviewRepository;
import com.unipi.PlayerHive.repository.games.GameRepository;
import com.unipi.PlayerHive.repository.users.UserNeo4jRepository;
import com.unipi.PlayerHive.repository.users.UserRepository;
import com.unipi.PlayerHive.model.UserPrincipal;
import com.unipi.PlayerHive.utility.ArrayPager;
import com.unipi.PlayerHive.utility.batch.GameConsistencyManager;
import com.unipi.PlayerHive.utility.batch.UserConsistencyManager;
import com.unipi.PlayerHive.utility.map.UserMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserNeo4jRepository userNeo4jRepository;
    private final GameRepository gameRepository;
    private final UserMapper userMapper;
    private final MongoTemplate mongoTemplate;

    private final ReviewRepository reviewRepository;

    public UserService(UserRepository userRepository, UserNeo4jRepository userNeo4jRepository, GameRepository gameRepository, UserMapper userMapper, MongoTemplate mongoTemplate, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.userNeo4jRepository = userNeo4jRepository;
        this.gameRepository = gameRepository;
        this.userMapper = userMapper;
        this.mongoTemplate = mongoTemplate;
        this.reviewRepository = reviewRepository;
    }

    // TODO: REORDER THE FUNCTIONS SO THAT THEY MATCH THE CONTROLLER

    // JwtFilter already put the authenticated user in the security context earlier in the request, this just reads it back out :)
    private User getAuthenticatedUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal())
                .getUser();
    }

    public ProfileDTO getProfileById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        return userMapper.userToProfileDTO(user);
    }

    public OwnProfileDTO getOwnProfileById() {

        User user = getAuthenticatedUser();

        OwnProfileDTO ownProfile = userMapper.userToOwnProfileDTO(user);

        ownProfile.setFriendRequestsNumber(userRepository.getFriendRequestsNumber(user.getId()));

        return ownProfile;
    }

    public Slice<UserSearchDTO> searchUser(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);

        return userRepository.searchByUsernameContaining(username, pageable);
    }

    public Page<LibraryGameDTO> getLibraryById(String userId, int page, int size) {

        if(!userRepository.existsById(userId))
            throw new NoSuchElementException("The requested user does not exist");

        Pageable pageable = PageRequest.of(page,size);
        return userNeo4jRepository.findLibraryById(userId, pageable);
    }

    @Transactional
    public void editLibrary(@Valid AddGameToLibraryDTO addGame) {
        // TODO: add to the exception controller the validation failure exception (negative achievements)

        if(!gameRepository.existsById(addGame.getGameId()))
            throw new NoSuchElementException("The requested game does not exist");

        String userId = getAuthenticatedUser().getId();

        Optional<Double> previousUserPlaytime = userNeo4jRepository.findUserGamePlaytime(userId, addGame.getGameId());

        //float totalPlaytime = game.getTotalHoursPlayed() + addGame.getHoursPlayed(); TODO DELAY UPDATE
        float userPlaytimeToAdd = addGame.getHoursPlayed();
        int gameNumberToAdd = 0;

        // TODO DO I just assume mongodb and neo4j data are synchronized?
        if(previousUserPlaytime.isEmpty()){
            // game.setNumPlayers(game.getNumPlayers() + 1 ); TODO DELAY UPDATE
            gameNumberToAdd++;
        }else{
            //totalPlaytime -= userGamePlaytime.get().floatValue();
            userPlaytimeToAdd -= previousUserPlaytime.get().floatValue();
        }

        if(userPlaytimeToAdd == 0 && gameNumberToAdd == 0)
            return; // nothing to update

        boolean success = userNeo4jRepository.saveGameInLibrary(userId,addGame.getGameId(),addGame.getHoursPlayed().doubleValue(),addGame.getAchievements());
        if(!success){
            throw new IllegalArgumentException("The achievement number exceeds the game's achievement number");
        }
        //game.setTotalHoursPlayed(totalPlaytime);
        int modified = userRepository.updateUserStats(userId, userPlaytimeToAdd,gameNumberToAdd);
        if(modified<=0)
            throw new RuntimeException("The server was unable to increase the player's gaming stats");
        //gameRepository.save(game);!! TODO delay update

    }

    @Transactional
    public void removeGameFromLibrary(String gameId) {
        String userId = getAuthenticatedUser().getId();

        Double userGamePlaytime = userNeo4jRepository.findUserGamePlaytime(userId, gameId)
                .orElseThrow(() -> new NoSuchElementException("The game specified is not present in the user's library"));

        //  TODO: when do we update the game stats? i think that a single query that updates every game is better
        // game.setNumPlayers(game.getNumPlayers() -1 );

        //game.setTotalHoursPlayed(game.getTotalHoursPlayed() - userGamePlaytime.floatValue());
        int modified = userRepository.updateUserStats(userId, -userGamePlaytime.floatValue(),-1);
        if(modified<=0)
            throw new RuntimeException("The server was unable to decrease the player's gaming stats");

        boolean success = userNeo4jRepository.removeGameFromLibrary(userId,gameId);
        if(!success)
            throw new RuntimeException("The server was unable to remove the game from the library");
    }

    public Page<FriendDTO> getFriendListById(String userId, int page, int size) {
        // TODO: THIS QUERY RETURNS 200 and an empty array **EVEN IF** no user matches the id. is this fine or do we have to fix it, adding an extra query?
        Pageable pageable = PageRequest.of(page,size);
        return userNeo4jRepository.findUsersFriends(userId, pageable);
    }

    public List<FriendRequestDTO> getFriendRequests(int page, int size) {
        String userId = getAuthenticatedUser().getId();

        int friendRequestNumber = userRepository.getFriendRequestsNumber(userId);

        ArrayPager pager = new ArrayPager(friendRequestNumber, page, size);

        if(friendRequestNumber == 0 || pager.isOutOfBounds())
            return new ArrayList<>();

        FriendRequestContainerDTO friendRequestContainer = userRepository.findFriendRequestsById(userId,pager.getStart(),pager.getLimit());

        return friendRequestContainer.getFriendRequests();
    }

    @Transactional
    public String sendRequestToUser(String targetUserId) {

        User user = getAuthenticatedUser();
        String userId = user.getId();

        if(userId.equalsIgnoreCase(targetUserId))
            throw new IllegalArgumentException("The user attempted to send a request to himself");

        if(!userRepository.existsById(targetUserId))
            throw new NoSuchElementException("The specified user does not exist");

        if(userNeo4jRepository.checkFriendshipExistence(userId,targetUserId))
            throw new ResourceAlreadyExistsException("The users are already friends");


        try { // we first check if we already have a request from targetUser
            this.approveRequestFromUser(targetUserId); //TODO TRANSACTIONAL DOES not work in function calls
            return "The friendship has been established";

        } catch (NoSuchElementException ignored) {} // if no friend request was present, NoSuchElementException is thrown

        FriendRequestMongoDTO requestDTO = new FriendRequestMongoDTO(new ObjectId(userId),user.getUsername(),user.getPfpURL(), LocalDateTime.now());

        int modified = userRepository.addFriendRequest(targetUserId,requestDTO.getUserId(),requestDTO); // add controls to return value
        if(modified != 1)
            throw new ResourceAlreadyExistsException("The friend request is already present");

        return "Friend request sent successfully";
    }

    @Transactional
    public void approveRequestFromUser(String targetUserId) {
        String userId = getAuthenticatedUser().getId();

        int result = userRepository.acceptFriendRequest(userId,new ObjectId(targetUserId));
        if(result != 1)
            throw new NoSuchElementException("Friend request was not present!");

        result = userRepository.editFriendCounter(targetUserId, 1);
        if(result != 1)
            throw new RuntimeException("The server couldn't increase the user's friend counter");

        boolean success = userNeo4jRepository.createFriendship(userId,targetUserId);
        if(!success)
            throw new RuntimeException("The server was unable to complete the operation");

    }

    public void removeRequestFromUser(String targetUserId) {
        String userId = getAuthenticatedUser().getId();
        //if(!userRepository.existsById(userId)) if the user does not exist, the request will simply not be present
        // ...
        int result = userRepository.removeFriendRequest(userId,new ObjectId(targetUserId));
        if(result != 1)
            throw new NoSuchElementException("Friend request was not present!");
    }

    @Transactional
    public void removeFriend(String friendId) {
        String userId = getAuthenticatedUser().getId();

        boolean success = userNeo4jRepository.removeFriendById(userId,friendId);
        if(!success){
            throw new NoSuchElementException("No Friend was found matching the given Id");
        }

        int result = userRepository.editFriendCounter(userId, -1);
        if(result != 1)
            throw new RuntimeException("The server was unable to decrease the friend counter");

        result = userRepository.editFriendCounter(friendId, -1);
        if(result != 1)
            throw new RuntimeException("The server was unable to decrease the friend counter");


    }


    public List<ReviewDTO> getUserReviews(String userId, int page, int size) {
        if(!userRepository.existsById(userId))
            throw new NoSuchElementException("The user does not exist");

        int reviewNumber = userRepository.getReviewNumber(userId);

        ArrayPager pager = new ArrayPager(reviewNumber, page, size);

        if(reviewNumber == 0 || pager.isOutOfBounds())
            return new ArrayList<>();


        UserReviewContainerDTO reviewContainer = userRepository.getUserReviews(userId,pager.getStart(),pager.getLimit());

        List<String> reviewIds = reviewContainer.getReviews().stream().map(userReviewDTO ->
                userReviewDTO.getReviewId().toString()).toList(); // todo ponder about the removal of users

        return reviewRepository.findByIdInOrderByTimestampDesc(reviewIds);
    }


    // this function looks kinda heavy, probably better to delay its execution?
    @Transactional
    public void deleteUser(String userId){

        //TODO, ADD CONSOLE LOGS
        User requestingUser = getAuthenticatedUser();
        String requesterId = requestingUser.getId();

        boolean isAdmin = requestingUser.getRole().equalsIgnoreCase("ADMIN");

        if(!isAdmin && !requesterId.equals(userId)){
            throw new RuntimeException("You can't delete another user's profile"); //TODO ADD NEW EXCEPTION TYPE
        }

        UserConsistencyManager userManager = new UserConsistencyManager(mongoTemplate);
        GameConsistencyManager gameManager = new GameConsistencyManager(mongoTemplate);

        // we obtain all the friends in a stream for easy access
        Stream<String> friendStream = userNeo4jRepository.findUsersFriendStream(userId);

        // decrements the "friend" value of every user found in the previous query
        userManager.adjustFriendCountersAfterUserRemoval(friendStream.iterator(), userRepository);
        friendStream.close();

        // deletes all user's reviews
        reviewRepository.removeByUserId(userId);

        // we remove the user's reviews from every single game
        gameManager.removeUserReviewsFromGames(userId,userRepository);

        // TODO: now that we removed reviews, the games score should be updated. Manage this!! DONT DO IT IN THIS FUNCTION THO

        userNeo4jRepository.deleteById(userId);
        userRepository.deleteById(userId);

    }
}

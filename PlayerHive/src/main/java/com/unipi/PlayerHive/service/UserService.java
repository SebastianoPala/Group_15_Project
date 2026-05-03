package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.LibraryGameDTO;
import com.unipi.PlayerHive.DTO.games.PlaytimeAchievementsDTO;
import com.unipi.PlayerHive.DTO.reviews.ReviewDTO;
import com.unipi.PlayerHive.DTO.reviews.UserReviewContainerDTO;
import com.unipi.PlayerHive.DTO.users.*;
import com.unipi.PlayerHive.DTO.users.friends.FriendDTO;
import com.unipi.PlayerHive.DTO.users.friends.FriendRequestContainerDTO;
import com.unipi.PlayerHive.DTO.users.friends.FriendRequestDTO;
import com.unipi.PlayerHive.DTO.users.friends.FriendRequestMongoDTO;
import com.unipi.PlayerHive.config.Exceptions.ResourceAlreadyExistsException;
import com.unipi.PlayerHive.model.user.User;
import com.unipi.PlayerHive.repository.ReviewRepository;
import com.unipi.PlayerHive.repository.games.GameNeo4jRepository;
import com.unipi.PlayerHive.repository.games.GameRepository;
import com.unipi.PlayerHive.repository.users.UserNeo4jRepository;
import com.unipi.PlayerHive.repository.users.UserRepository;
import com.unipi.PlayerHive.model.user.UserPrincipal;
import com.unipi.PlayerHive.utility.ArrayPager;
import com.unipi.PlayerHive.utility.batch.GameConsistencyManager;
import com.unipi.PlayerHive.utility.map.UserMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserNeo4jRepository userNeo4jRepository;
    private final GameRepository gameRepository;
    private final GameNeo4jRepository gameNeo4jRepository;
    private final UserMapper userMapper;

    private final GameConsistencyManager gameConsistencyManager;

    private final ReviewRepository reviewRepository;

    public UserService(UserRepository userRepository, UserNeo4jRepository userNeo4jRepository, GameRepository gameRepository, UserMapper userMapper, GameNeo4jRepository gameNeo4jRepository, GameConsistencyManager gameConsistencyManager, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.userNeo4jRepository = userNeo4jRepository;
        this.gameRepository = gameRepository;
        this.userMapper = userMapper;
        this.gameNeo4jRepository = gameNeo4jRepository;
        this.gameConsistencyManager = gameConsistencyManager;
        this.reviewRepository = reviewRepository;
    }

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

        if(addGame.getGameId().length() != 24)
            throw new IllegalArgumentException("The provided game Id is not valid");

        if(!gameRepository.existsById(addGame.getGameId()))
            throw new NoSuchElementException("The requested game does not exist");

        String userId = getAuthenticatedUser().getId();

        Optional<PlaytimeAchievementsDTO> playAchiev = gameNeo4jRepository.findUserPlaytimeAndGameAchievements(userId, addGame.getGameId());

        // PlaytimeAchievementsDTO contains (if present) the user's playtime on a game, and said game's total number of achievements.
        if(playAchiev.isPresent() && playAchiev.get().getAchievements() < addGame.getAchievements())
            throw new IllegalArgumentException("The achievement number exceeds the game's achievement number");

        float playtimeToAdd = addGame.getHoursPlayed();
        boolean gameAlreadyPresent = false;

        if (playAchiev.isPresent()) {
            playtimeToAdd -= playAchiev.get().getHoursPlayed().floatValue(); // if the game was already in the library, we only add the difference
            gameAlreadyPresent = true;
        }

        if( playtimeToAdd == 0 && gameAlreadyPresent)
            return; // nothing to update

        boolean success = userNeo4jRepository.saveGameInLibrary(userId,addGame.getGameId(),addGame.getHoursPlayed().doubleValue(),addGame.getAchievements());
        if(!success){
            throw new IllegalArgumentException("The achievement number exceeds the game's achievement number");
        }


        int modified = userRepository.updateUserStats(userId, playtimeToAdd,(gameAlreadyPresent) ? 0 : 1);
        if(modified<=0)
            throw new RuntimeException("The server was unable to increase the player's gaming stats");

        modified = gameRepository.updateGameStats(addGame.getGameId(), playtimeToAdd,(gameAlreadyPresent) ? 0 : 1);
        if(modified<=0)
            throw new RuntimeException("The server was unable to increase the game's stats");

    }

    @Transactional
    public void removeGameFromLibrary(String gameId) {
        String userId = getAuthenticatedUser().getId();

        Double userGamePlaytime = userNeo4jRepository.findUserGamePlaytime(userId, gameId)
                .orElseThrow(() -> new NoSuchElementException("The game specified is not present in the user's library"));

        int modified = userRepository.updateUserStats(userId, -userGamePlaytime.floatValue(),-1);
        if(modified<=0)
            throw new RuntimeException("The server was unable to decrease the player's gaming stats");

        modified = gameRepository.updateGameStats(gameId, -userGamePlaytime.floatValue(),-1);
        if(modified<=0)
            throw new RuntimeException("The server was unable to decrease the game's stats");

        boolean success = userNeo4jRepository.removeGameFromLibrary(userId,gameId);
        if(!success)
            throw new RuntimeException("The server was unable to remove the game from the library");
    }

    public Page<FriendDTO> getFriendListById(String userId, int page, int size) {
        if(!userRepository.existsById(userId))
            throw new NoSuchElementException("The requested user does not exist");
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
            this.approveRequestFromUser(targetUserId);
            return "The friendship has been established";

        } catch (NoSuchElementException ignored) {} // if no friend request was present, NoSuchElementException is thrown

        FriendRequestMongoDTO requestDTO = new FriendRequestMongoDTO(new ObjectId(userId),user.getUsername(),user.getPfpURL(), LocalDateTime.now());

        int modified = userRepository.addFriendRequest(targetUserId,requestDTO.getUserId(),requestDTO); // add controls to return value
        if(modified != 1)
            throw new ResourceAlreadyExistsException("The friend request is already present");

        return "Friend request sent successfully";
    }

    @Transactional
    public String approveRequestFromUser(String targetUserId) {
        String userId = getAuthenticatedUser().getId();

        if(targetUserId.length() != 24) // prevents ObjectId constructor exception
            throw new IllegalArgumentException("The input given is not a valid user Id");

        if(!userRepository.existsById(targetUserId)){
            int result = userRepository.removeFriendRequest(userId,new ObjectId(targetUserId));
            if(result == 1)
                return "The profile that sent the friend request no longer exists. The friend request has been removed";
            else
                throw new NoSuchElementException("Friend request was not present!");
        }

        int result = userRepository.acceptFriendRequest(userId,new ObjectId(targetUserId));
        if(result != 1)
            throw new NoSuchElementException("Friend request was not present!");

        result = userRepository.editFriendCounter(targetUserId, 1);
        if(result != 1){
            throw new RuntimeException("The server couldn't increase the user's friend counter");
        }

        boolean success = userNeo4jRepository.createFriendship(userId,targetUserId);
        if(!success)
            throw new RuntimeException("The server was unable to complete the operation");

        return "The friend request has been approved successfully";
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
        if(!success)
            throw new NoSuchElementException("No Friend was found matching the given Id");

        int result = userRepository.decrementFriendCounterForUsers(List.of(userId, friendId));
        if (result != 2)
            throw new RuntimeException("The server was unable to decrease the friend counter for one or both users");

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
                userReviewDTO.getReviewId().toString()).toList();

        return reviewRepository.findByIdInOrderByTimestampDesc(reviewIds);
    }


    @Transactional
    public void deleteUser(String userId){

        User requestingUser = getAuthenticatedUser();
        String requesterId = requestingUser.getId();

        boolean isAdmin = requestingUser.getRole().equalsIgnoreCase("ADMIN");

        if(!isAdmin && !requesterId.equals(userId)){
            throw new RuntimeException("You can't delete another user's profile"); //TODO ADD NEW EXCEPTION TYPE
        }

        if(isAdmin && !userRepository.existsById(userId))
            throw new NoSuchElementException("The user requested for deletion does not exist");

        System.out.println("A user with Id: " + userId + " has been scheduled for deletion");


        List<String> friendList = userNeo4jRepository.findAllUsersFriend(userId);

        System.out.println("The target user has " + friendList.size() + " friends");

        if(!friendList.isEmpty()) {
            // decrements the "friend" value of every user found in the previous query
            long modified = userRepository.decrementFriendCounterForUsers(friendList);

            System.out.println(modified + " users had their friend counter decreased");

            friendList.clear();
        }

        // we do not delete friend requests, they will be deleted eventually

        // we remove the user's reviews from every single game
        long modified = gameConsistencyManager.removeUserReviewsFromGames(userId);

        System.out.println(modified + " games had their reviews updated");

        // deletes all user's reviews
        modified = reviewRepository.removeByUserId(new ObjectId(userId));

        System.out.println(modified + " reviews were deleted from the Review Collection");


        modified = gameConsistencyManager.adjustGameStatsAndRemove(userId);

        System.out.println(modified + " games had their stats updated");

        userRepository.deleteById(userId);
    }

    // INTERESTING QUERIES ===========================================

    //TODO ADD VARIABLES
    public List<PlayerStatsDTO> getHardcoreGamers(){
        return userRepository.findHardcoreGamers(5, 100);
    }

    public List<KeyboardWarriorDTO> getKeyboardWarriors(){
        return userRepository.getKeyboardWarriors();
    }

    public List<ActiveGamerDTO> getMostActiveGamers(){
        return userRepository.getMostActiveGamers();
    }
}

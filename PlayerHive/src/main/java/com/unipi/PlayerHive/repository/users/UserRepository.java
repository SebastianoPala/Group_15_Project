package com.unipi.PlayerHive.repository.users;

import com.unipi.PlayerHive.DTO.reviews.GameReviewContainerDTO;
import com.unipi.PlayerHive.DTO.reviews.UserReviewContainerDTO;
import com.unipi.PlayerHive.DTO.reviews.UserReviewDTO;
import com.unipi.PlayerHive.DTO.users.FriendRequestMongoDTO;
import com.unipi.PlayerHive.DTO.users.UserSearchDTO;
import com.unipi.PlayerHive.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User,String> {
    @Query("{ 'username': { $regex: ?0, $options: 'i' } }" +
            "{ '$project': { 'id': '$_id', 'username': 1, 'role': 1, 'pfpURL':1 } }")
    Slice<UserSearchDTO> searchByUsernameContaining(String username, Pageable pageable);

    @Query("{ '_id' : ?0, 'friendRequests.user_id' : { '$ne': ?1 } }")
    @Update("{ '$push' : { 'friendRequests' : ?2 } }")
    int addFriendRequest(String targetUserId, ObjectId senderUserId, FriendRequestMongoDTO request);

    @Query("{ '_id' : ?0, 'friendRequests.user_id' : ?1 }")
    @Update("{ '$pull' : { 'friendRequests' : { 'user_id' : ?1 } }, " +
            "  '$inc' : { 'friends' : 1 } }")
    int acceptFriendRequest(String userId, String userToAccept);

    @Query("{ '_id' : ?0 }")
    @Update("{ '$pull' : { 'friendRequests' : { 'user_id' : ?1 } } }")
    int removeFriendRequest(String userId, String userToRemove);

    @Query("{ '_id' : ?0 }")
    @Update("{ '$inc' : { 'friends' : ?1 } }")
    int editFriendCounter(String userId, int quantity);

    @Query("{ '_id' : { $in : ?0 } }")
    @Update("{ '$inc' : { 'friends' : -1 } }")
    void decrementFriendCounterForUsers(List<String> userIds);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'hoursPlayed': ?1, 'numGames': ?2 } }")
    int updateUserStats(String userId, float playtimeDifference, int gameNumberToAdd);

    User findByEmail(String email);

    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);

    @Aggregation(pipeline = {
            "{ '$match': { '_id': ?0 } }",
            "{ '$project': { 'reviews': { '$slice': ['$reviewIds', ?1, ?2] } } }"
    })
    UserReviewContainerDTO getUserReviews(String userId, int skip, int limit);

    // push a new {reviewId, gameId} pair into the user's reviewIds array when they write a review
    @Query("{ '_id': ?0 }")
    @Update("{ '$push': { 'reviewIds': ?1 } }")
    void addReviewToUser(String userId, UserReviewDTO review);

    // pull the review entry out of reviewIds when the review is deleted
    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'reviewIds': { 'review_id': ?1 } } }")
    void removeReviewFromUser(String userId, org.bson.types.ObjectId reviewId);

}

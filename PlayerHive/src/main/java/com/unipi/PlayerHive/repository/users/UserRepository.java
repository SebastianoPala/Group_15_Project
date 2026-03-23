package com.unipi.PlayerHive.repository.users;

import com.unipi.PlayerHive.DTO.users.FriendRequestMongoDTO;
import com.unipi.PlayerHive.DTO.users.UserSearchDTO;
import com.unipi.PlayerHive.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

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



    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'hoursPlayed': ?1, 'numGames': ?2 } }")
    int updateUserStats(String userId, float playtimeDifference, int gameNumberToAdd);
}

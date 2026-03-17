package com.unipi.PlayerHive.repository.users;

import com.unipi.PlayerHive.DTO.games.LibraryGameDTO;
import com.unipi.PlayerHive.DTO.users.FriendRequestDTO;
import com.unipi.PlayerHive.model.Game;
import com.unipi.PlayerHive.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User,String> {
    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    Optional<List<User>> searchByUsername(String username);

    @Query("{ '_id' : ?0, 'friendRequests.user_id' : { '$ne': ?1 } }")
    @Update("{ '$push' : { 'friendRequests' : ?2 } }")
    int addFriendRequest(String targetUserId, String senderUserId, FriendRequestDTO requestDTO);

    @Query("{ '_id' : ?0 }")
    @Update("{ '$pull' : { 'friendRequests' : { 'user_id' : ?1 } } }")
    int removeFriendRequest(String userId, String userToRemove);
}

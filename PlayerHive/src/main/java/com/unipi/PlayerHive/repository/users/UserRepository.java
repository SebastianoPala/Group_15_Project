package com.unipi.PlayerHive.repository.users;

import com.unipi.PlayerHive.model.Game;
import com.unipi.PlayerHive.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User,String> {
    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    Optional<List<User>> searchByUsername(String username);
}

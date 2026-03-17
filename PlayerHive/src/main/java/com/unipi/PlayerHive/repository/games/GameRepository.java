package com.unipi.PlayerHive.repository.games;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.unipi.PlayerHive.model.Game;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Optional<List<Game>> searchByName(String gameName);

    boolean existsByName(String name);
}

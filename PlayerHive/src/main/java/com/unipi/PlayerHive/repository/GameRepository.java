package com.unipi.PlayerHive.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import com.unipi.PlayerHive.model.Game;
import java.util.Optional;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {
}

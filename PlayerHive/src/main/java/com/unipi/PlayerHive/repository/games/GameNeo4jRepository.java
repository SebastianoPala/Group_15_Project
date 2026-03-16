package com.unipi.PlayerHive.repository.games;

import com.unipi.PlayerHive.model.GameNeo4j;
import org.springframework.stereotype.Repository;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

@Repository
public interface GameNeo4jRepository extends Neo4jRepository<GameNeo4j,String>{
}

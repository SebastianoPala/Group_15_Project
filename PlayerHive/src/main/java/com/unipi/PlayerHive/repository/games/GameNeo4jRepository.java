package com.unipi.PlayerHive.repository.games;

import com.unipi.PlayerHive.DTO.users.GameOwnerDTO;
import com.unipi.PlayerHive.model.GameNeo4j;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface GameNeo4jRepository extends Neo4jRepository<GameNeo4j,String>{

    @Query("MATCH (u:User)-[r:PLAYED]->(g:Game {id: $gameId}) RETURN u.id as id " +
            " r.hoursPlayed as hoursPlayed")
    Stream<GameOwnerDTO> findGameOwnersOf(String gameId);
}

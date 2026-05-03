package com.unipi.PlayerHive.repository.games;

import com.unipi.PlayerHive.DTO.games.PlaytimeAchievementsDTO;
import com.unipi.PlayerHive.DTO.users.GameOwnerDTO;
import com.unipi.PlayerHive.model.game.GameNeo4j;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameNeo4jRepository extends Neo4jRepository<GameNeo4j,String>{

    @Query("MATCH (u:User)-[r:PLAYED]->(g:Game {id: $gameId}) " +
            "WITH u, r LIMIT $batchSize " +
            "WITH u.id AS id, r.hoursPlayed AS hoursPlayed, r " +
            "DELETE r " +
            "RETURN id, hoursPlayed")
    List<GameOwnerDTO> deletePlayedEdgesInBatch(String gameId, int batchSize);

    // gets the USER's playtime and the GAME'S (NOT the user's) achievements
    @Query("MATCH (u:User {id: $userId})-[r:PLAYED]->(g:Game {id: $gameId}) " +
            "RETURN r.hoursPlayed as hoursPlayed, g.achievements as achievements")
    Optional<PlaytimeAchievementsDTO> findUserPlaytimeAndGameAchievements(String userId, String gameId);

    // INTERESTING QUERIES ========================================


}

package com.unipi.PlayerHive.repository.games;

import com.unipi.PlayerHive.DTO.games.PlaytimeAchievementsDTO;
import com.unipi.PlayerHive.DTO.users.GameOwnerDTO;
import com.unipi.PlayerHive.model.game.GameNeo4j;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

@Repository
public interface GameNeo4jRepository extends Neo4jRepository<GameNeo4j,String>{

    @Query("MATCH (u:User)-[r:PLAYED]->(g:Game {id: $gameId}) " +
            "WITH u, r LIMIT $batchSize " +
            "WITH u.id AS id, r.hoursPlayed AS hoursPlayed, r " +
            "DELETE r " +
            "RETURN id, hoursPlayed")
    List<GameOwnerDTO> deletePlayedEdgesInBatch(String gameId, int batchSize);

    // gets the USER's playtime (if present) and the GAME'S (NOT the user's) achievements
    @Query("MATCH (g:Game {id: $gameId}) " +
            "OPTIONAL MATCH (u:User {id: $userId})-[r:PLAYED]->(g) " +
            "RETURN r.hoursPlayed as hoursPlayed, g.achievements as achievements")
    PlaytimeAchievementsDTO findUserPlaytimeAndGameAchievements(String userId, String gameId);

    // INTERESTING QUERIES ========================================

    // 2. The "Game Recommendation" (Item-Based Collaborative Filtering)
    @Query("MATCH (u:User {id: $userId})-[:FRIENDS_WITH]-(friend:User)-[:PLAYED]->(recGame:Game) " +
            "WHERE NOT (u)-[:PLAYED]->(recGame) " +
            "RETURN recGame.name AS title, count(friend) AS friendsWhoPlay " +
            "ORDER BY friendsWhoPlay DESC " +
            "LIMIT $limit")
    List<Object> getGameRecommendations(String userId, int limit);

    // 5. Efficient Global Query: "Trending Games Among Friend Groups"
    @Query("MATCH (u1:User)-[:FRIENDS_WITH]-(u2:User) " +
            "WHERE id(u1) < id(u2) " +
            "MATCH (u1)-[:PLAYED]->(g:Game)<-[:PLAYED]-(u2) " +
            "RETURN g.name AS title, count(*) AS socialPlayCount " +
            "ORDER BY socialPlayCount DESC " +
            "LIMIT $limit")
    List<Object> getTrendingGamesAmongFriends(int limit);


}

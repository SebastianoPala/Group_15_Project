package com.unipi.PlayerHive.repository.users;

import com.unipi.PlayerHive.DTO.games.LibraryGameDTO;
import com.unipi.PlayerHive.DTO.users.friends.FriendDTO;
import com.unipi.PlayerHive.model.user.UserNeo4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNeo4jRepository extends Neo4jRepository<UserNeo4j,String> {

    @Query(value = "MATCH (u:User {id: $userId})-[r:PLAYED]->(g:Game) " +
                    "RETURN g.id AS id, g.name AS name, g.image AS image, g.achievements as achievements, " +
                    "r.hoursPlayed AS hoursPlayed, r.achievements AS achievementsObtained "+
                    "SKIP $skip LIMIT $limit",
            countQuery = "MATCH (u:User {id: $userId})-[r:PLAYED]->(g:Game) RETURN count(g)"
    )
    Page<LibraryGameDTO> findLibraryById(String userId, Pageable pageable);

    @Query("MATCH (u:User {id: $userId}) " +
            "MATCH (g:Game {id: $gameId}) " +
            "WHERE $achievements <= g.achievements " +
            "MERGE (u)-[r:PLAYED]->(g) " +
            "SET r.hoursPlayed = $hoursPlayed, " +
            "    r.achievements = $achievements " +
            "RETURN count(u) > 0")
    boolean saveGameInLibrary(String userId, String gameId, Double hoursPlayed, Integer achievements);

    @Query("MATCH (u:User {id: $userId})-[r:PLAYED]->(g:Game {id: $gameId}) " +
            "WITH r.hoursPlayed AS playtime, r " +
            "DELETE r " +
            "RETURN playtime")
    Optional<Double> removeGameAndGetPlaytime(String userId, String gameId);

    @Query(value = "MATCH (u1:User {id: $userId})-[r:FRIENDS_WITH]->(u2:User) " +
            "RETURN u2.id as id, u2.username as username, u2.pfpURL as pfpURL " +
            "SKIP $skip LIMIT $limit",
            countQuery = "MATCH (u1:User {id: $userId})-[r:FRIENDS_WITH]->(u2:User) RETURN count(u2)")
    Page<FriendDTO> findUsersFriends(String userId, Pageable pageable);

    @Query("MATCH (u1:User {id: $userId})-[r:FRIENDS_WITH]->(u2:User) " +
            " RETURN u2.id as id")
    List<String> findAllUsersFriend(String userId);

    @Query("MATCH (u1:User {id: $userId})-[r:FRIENDS_WITH]-(u2:User {id: $friendId}) " +
            "DELETE r " +
            "RETURN count(u1) >0"
    )
    boolean removeFriendById(String userId, String friendId);

    @Query("MATCH p=(u1:User {id: $userId1}) - [r:FRIENDS_WITH] -" +
            " (u2:User {id: $userId2}) " +
            "RETURN count(p) > 0")
    boolean checkFriendshipExistence(String userId1, String userId2);

    @Query("MATCH (u1:User {id: $userId1}) " +
            "MATCH (u2:User {id: $userId2}) " +
            "MERGE (u1)-[:FRIENDS_WITH]->(u2) " +
            "MERGE (u2)-[:FRIENDS_WITH]->(u1) " +
            "RETURN count(u1) > 0")
    boolean createFriendship(String userId1, String userId2);

    @Query("MATCH (u:User {id: $userId}) " +
            "OPTIONAL MATCH (u)-[r:PLAYED]->(g:Game) " +
            "WITH u, " +
            "     g.id AS id, " +
            "     r.hoursPlayed AS hoursPlayed " +
            "DETACH DELETE u " +
            "WITH id, hoursPlayed " +
            "WHERE id IS NOT NULL " +
            "RETURN id, hoursPlayed")
    List<LibraryGameDTO> deleteUserAndRetrieveLibrary(String userId);

    // INTERESTING QUERIES ===================================================

    // 1. The "Friend Recommendation" (Collaborative Filtering / Triadic Closure)
    @Query("MATCH (u:User {id: $userId})-[:FRIENDS_WITH]-(mutual:User)-[:FRIENDS_WITH]-(suggested:User) " +
            "WHERE u <> suggested AND NOT (u)-[:FRIENDS_WITH]-(suggested) " +
            "RETURN suggested.username AS username, count(mutual) AS mutualFriendsCount " +
            "ORDER BY mutualFriendsCount DESC " +
            "LIMIT $limit")
    List<Object> getFriendRecommendations(String userId, int limit);



    // 3. The "Player Matchmaker" (Common Interests)
    @Query("MATCH (u:User {id: $userId})-[:PLAYED]->(g:Game)<-[:PLAYED]-(other:User) " +
            "WHERE u <> other AND NOT (u)-[:FRIENDS_WITH]-(other) " +
            "RETURN other.username AS username, count(g) AS sharedGamesCount, collect(g.name) AS sharedGames " +
            "ORDER BY sharedGamesCount DESC " +
            "LIMIT $limit")
    List<Object> getPlayerMatchmaker(String userId, int limit);

    // 4. The "Game's Core Community" (Degree Centrality)
    @Query("MATCH (g:Game {id: $gameId})<-[:PLAYED]-(player:User) " +
            "RETURN player.username AS username, count { (player)-[:FRIENDS_WITH]-() } AS totalFriends " +
            "ORDER BY totalFriends DESC " +
            "LIMIT $limit")
    List<Object> getGameCoreCommunity(String gameId, int limit);


    // 7. Gaming Twin (Jaccard Similarity)
    @Query("MATCH (u1:User {id: $userId})-[:PLAYED]->(g:Game)<-[:PLAYED]-(u2:User) " +
            "WITH u1, u2, count(g) AS sharedGames " +
            "MATCH (u1)-[:PLAYED]->(g1:Game) " +
            "WITH u1, u2, sharedGames, count(g1) AS u1Total " +
            "MATCH (u2)-[:PLAYED]->(g2:Game) " +
            "WITH u2, sharedGames, u1Total, count(g2) AS u2Total " +
            "WITH u2, sharedGames, (u1Total + u2Total - sharedGames) AS unionGames " +
            "RETURN u2.username AS username, toFloat(sharedGames) / unionGames AS jaccardSimilarity " +
            "ORDER BY jaccardSimilarity DESC " +
            "LIMIT $limit")
    List<Object> getGamingTwin(String userId, int limit);

    // 8. The "Hidden Gem" Recommendation (Inverse Popularity)
    @Query("MATCH (u:User {id: $userId})-[:FRIENDS_WITH]-(friend)-[:PLAYED]->(game:Game) " +
            "WHERE NOT (u)-[:PLAYED]->(game) " +
            "WITH game, count(DISTINCT friend) AS friendsPlaying " +
            "MATCH (game)<-[:PLAYED]-(globalPlayer) " +
            "WITH game, friendsPlaying, count(globalPlayer) AS globalPopularity " +
            "WHERE globalPopularity < $nicheThreshold " +
            "RETURN game.name AS title, friendsPlaying AS friendsPlaying, globalPopularity AS globalPopularity " +
            "ORDER BY friendsPlaying DESC, globalPopularity ASC " +
            "LIMIT $limit")
    List<Object> getHiddenGems(String userId, int nicheThreshold, int limit);

}

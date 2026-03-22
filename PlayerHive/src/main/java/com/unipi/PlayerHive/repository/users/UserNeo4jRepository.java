package com.unipi.PlayerHive.repository.users;

import com.unipi.PlayerHive.DTO.games.LibraryGameDTO;
import com.unipi.PlayerHive.DTO.users.FriendDTO;
import com.unipi.PlayerHive.model.UserNeo4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

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

    @Query("MATCH (u:User {id: $userId})-[r:PLAYED]->(g:Game {id: $gameId}) " +
            "RETURN r.hoursPlayed")
    Optional<Double> findUserGamePlaytime(String userId, String gameId);

    @Query("MATCH (u:User {id: $userId}) " +
            "MATCH (g:Game {id: $gameId}) " +
            "WHERE $achievements <= g.achievements " +
            "MERGE (u)-[r:PLAYED]->(g) " +
            "SET r.hoursPlayed = $hoursPlayed, " +
            "    r.achievements = $achievements " +
            "RETURN count(u) > 0")
    boolean saveGameInLibrary(String userId, String gameId, Double hoursPlayed, Integer achievements);

    @Query("MATCH (u:User {id: $userId})-[r:PLAYED]->(g:Game {id: $gameId}) " +
            "DELETE r " +
            "RETURN count(u) > 0")
    boolean removeGameFromLibrary(String userId, String gameId);

    @Query(value = "MATCH (u1:User {id: $userId})-[r:FRIENDS_WITH]->(u2:User) " +
            "RETURN u2.id as id, u2.username as username, u2.pfpURL as pfpURL " +
            "SKIP $skip LIMIT $limit",
            countQuery = "MATCH (u1:User {id: $userId})-[r:FRIENDS_WITH]->(u2:User) RETURN count(u2)")
    Page<FriendDTO> findUsersFriends(String userId, Pageable pageable);

    @Query("MATCH (u1:User {id: $userId})-[r:FRIENDS_WITH]-(u2:User {id: $friendId}) " +
            "DELETE r " +
            "RETURN count(u1) >0"
    )
    boolean removeFriendById(String userId, String friendId);

    @Query("MATCH (u1:User {id: $userId1}) " +
            "MATCH (u2:User {id: $userId2}) " +
            "MERGE (u1)-[:FRIENDS_WITH]->(u2) " +
            "MERGE (u2)-[:FRIENDS_WITH]->(u1) " +
            "RETURN count(u1) > 0")
    boolean createFriendship(String userId1, String userId2);
}

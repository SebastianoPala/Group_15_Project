package com.unipi.PlayerHive.repository.users;

import com.unipi.PlayerHive.DTO.games.LibraryGameDTO;
import com.unipi.PlayerHive.DTO.users.FriendDTO;
import com.unipi.PlayerHive.model.UserNeo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNeo4jRepository extends Neo4jRepository<UserNeo4j,String> {

    @Query("MATCH (u:User {id: $userId})-[r:PLAYED]->(g:Game) " +
            "RETURN g.id AS id, g.name AS name, g.image AS image, g.achievements as achievements, " +
            "r.hoursPlayed AS hoursPlayed, r.achievements AS achievementsObtained")
    List<LibraryGameDTO> findLibraryById(String userId);

    @Query("MATCH (u:User {id: $userId})-[r:PLAYED]->(g:Game {id: $gameId}) " +
            "RETURN r.hoursPlayed")
    Optional<Double> findUserGamePlaytime(String userId, String gameId);

    @Query("MATCH (u:User {id: $userId}) " +
            "MATCH (g:Game {id: $gameId}) " +
            "MERGE (u)-[r:PLAYED]->(g) " +
            "SET r.hoursPlayed = $hoursPlayed, " +
            "    r.achievements = $achievements")
    void saveGameInLibrary(String userId, String gameId, Double hoursPlayed, Integer achievements);

    @Query("MATCH (u:User {id: $userId})-[r:PLAYED]->(g:Game {id: $gameId}) " +
            "DELETE r")
    void removeGameFromLibrary(String userId, String gameId);

    @Query("MATCH (u1:User {id: $userId})-[r:FRIENDS_WITH]->(u2:User) " +
            "RETURN u2.id as id, u2.username as username, u2.pfpURL as pfpURL")
    List<FriendDTO> findUsersFriends(String userId);

    @Query("MATCH (u1:User {id: $userId})-[r:FRIENDS_WITH]-(u2:User {id: $friendId}) " +
            "DELETE r")
    void removeFriendById(String userId, String friendId);

    @Query("MATCH (u1:User {id: $userId1}) " +
            "MATCH (u2:User {id: $userId2}) " +
            "MERGE (u1)-[:FRIENDS_WITH]->(u2) " +
            "MERGE (u2)-[:FRIENDS_WITH]->(u1)")
    void createFriendship(String userId1, String userId2);
}

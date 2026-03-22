package com.unipi.PlayerHive.repository.games;

import com.unipi.PlayerHive.DTO.games.GameSearchDTO;
import com.unipi.PlayerHive.DTO.games.LightGameDTO;
import com.unipi.PlayerHive.DTO.games.RecentReviewDTO;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import com.unipi.PlayerHive.model.Game;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }" +
            "{ '$project': { 'id': '$_id', 'name': 1, 'price': 1, 'discount':1,'imageURL':1 } }")
    Slice<GameSearchDTO> searchByNameContaining(String gameName, Pageable pageable);

    boolean existsByName(String name);

    @Query(value = "{ '_id': ?0 }", fields = "{ 'allReviews': 0 }")
    Optional<LightGameDTO> findByIdLight(String gameId);

    @Query("{ '_id': ?0 }")
    @Update("{ '$push': { " +
            "    'allReviews': ?1, " + // Inserisce l'ID in coda (comportamento standard)
            "    'recentReviews': { " +
            "        '$each': [?2], " + // Il nuovo oggetto recensione
            "        '$position': 0, " + // Lo mette in testa
            "        '$slice': 25 " + // Trattiene solo i primi 25 elementi (elimina il più vecchio in coda)
            "    } " +
            "}, " +
            "'$inc' :{ 'countScore': 1, 'sumScore' : ?3} }")
    int addReviewToGame(String gameId, ObjectId reviewId, RecentReviewDTO recentReview, float score);

}

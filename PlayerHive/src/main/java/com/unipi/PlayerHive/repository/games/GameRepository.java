package com.unipi.PlayerHive.repository.games;

import com.unipi.PlayerHive.DTO.games.GameSearchDTO;
import com.unipi.PlayerHive.DTO.games.LightGameDTO;
import com.unipi.PlayerHive.DTO.reviews.GameReviewContainerDTO;
import com.unipi.PlayerHive.DTO.reviews.ReviewDTO;
import com.unipi.PlayerHive.DTO.reviews.OldGameReviewDTO;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import com.unipi.PlayerHive.model.Game;

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
            "    'allReviews': ?1, " +
            "    'recentReviews': { " +
            "        '$each': [?2], " +
            "        '$position': 0, " +
            "        '$slice': 25 " +
            "    } " +
            "}, " +
            "'$inc' :{ 'countScore': 1, 'sumScore' : ?3} }")
    int addReviewToGame(String gameId, OldGameReviewDTO oldReview, ReviewDTO recentReview, float score);

    @Query("{ '_id': ?0, 'allReviews.review_id': ObjectId(?1) }") // todo fix
    @Update("{" +
            "  '$inc': { 'countScore': -1, 'sumScore': ?2 }," +
            "  '$pull': {" +
            "    'allReviews': { 'review_id': ObjectId(?1) }," +
            "    'recentReviews': { '_id': ObjectId(?1) }" +
            "  }" +
            "}")
    int deleteReviewFromGame(ObjectId gameId, String reviewId, Float score);

    @Aggregation(pipeline = {
            "{ '$match': { '_id': ?0 } }",
            "{ '$project': { '_id': 0, 'count': { '$size': '$allReviews' } } }"
    })
    int getReviewNumber(String gameId);

    @Aggregation(pipeline = {
            "{ '$match': { '_id': ?0 } }",
            "{ '$project': { 'reviews': { '$slice': ['$allReviews', ?1, ?2] } } }"
    })
    GameReviewContainerDTO getGameReviews(String gameId, int skip, int limit);
}

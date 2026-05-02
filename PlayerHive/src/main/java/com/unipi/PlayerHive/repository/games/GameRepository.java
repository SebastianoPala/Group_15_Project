package com.unipi.PlayerHive.repository.games;

import com.unipi.PlayerHive.DTO.games.*;
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
import com.unipi.PlayerHive.model.game.Game;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }" +
            "{ '$project': { 'id': '$_id', 'name': 1, 'price': 1, 'discount':1, 'finalPrice': 1,'imageURL':1 } }")
    Slice<GameSearchDTO> searchByNameContaining(String gameName, Pageable pageable);

    boolean existsByName(String name);

    @Query("{ '_id': ?0 }" +
            "{ '$project': { 'allReviews': 0 }}")
    Optional<Game> findByIdLight(String gameId);

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

    @Query("{ '_id': ?0, 'allReviews.review_id': ObjectId(?1) }")
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

    // INTERESTING QUERIES ===========================================

    @Aggregation(pipeline = {
            "{ $match: { countScore: { $gt: ?0 } } }",
            "{ $project: { _id: 1, name: 1, price: 1, discount: 1, image: 1, avgRating: { $divide: ['$sumScore', '$countScore'] }, finalPrice: 1, genres: 1} }",
            "{ $match: { finalPrice: { $gte: ?1, $lte: ?2 }, avgRating: { $gte: ?3 } } }",
            "{ $addFields: { qualityPerPrice: { $cond: [ { $eq: ['$finalPrice', 0] }, 999999, { $divide: ['$avgRating', '$finalPrice'] } ] } } }",
            "{ $sort: { qualityPerPrice: -1 } }",
            "{ $limit: 15 }"
    })
    List<GameStatsDTO> getQualityToPriceGames(int minReviews, double minPrice, double maxPrice, double minRating);

    @Aggregation(pipeline = {
            "{ $match: { numPlayers: { $gt: ?0 } } }",
            "{ $project: { _id: 1, name: 1, price: 1, discount: 1, finalPrice: 1, image: 1, numPlayers: 1, avgTimePlayed: { $divide: ['$totalHoursPlayed', '$numPlayers'] } , genres: 1 } }",
            "{ $match: { finalPrice: { $gte: ?1, $lte: ?2 }, avgTimePlayed: { $gte: ?3 } } }",
            "{ $addFields: { valueForMoney: { $cond: [ { $eq: ['$finalPrice', 0] }, 999999, { $divide: ['$avgTimePlayed', '$finalPrice'] } ] } } }",
            "{ $sort: { valueForMoney: -1 } }",
            "{ $limit: 15 }"
    })
    List<GameInvestmentDTO> getTimeToPriceGames(int minPlayers, double minPrice, double maxPrice, double minAvgTime);

    @Aggregation(pipeline = {

            "{ $match: { 'recentReviews.1': { $exists: true } } }",
            "{ $project: { " +
                        "_id: 1, " +
                    "    name: 1, " +
                    "    price: 1, " +
                    "    discount: 1, " +
                        "finalPrice: 1" +
                    "    image: 1, " +
                    "    sumScore: 1, " +
                    "    countScore: 1, " +
                        "genres: 1," +
                    "    timeDistanceMs: { " +
                    "      $subtract: [ " +
                    "        { $max: '$recentReviews.timestamp' }, " +
                    "        { $min: '$recentReviews.timestamp' } " +
                    "      ] " +
                    "    } " +
                    "} }",
            "{ $sort: { timeDistanceMs: 1 } }",
            "{ $limit: 15 }",
            "{ $addFields: { avgRating: { $cond: [ { $eq: ['$countScore', 0] }, 0, { $divide: ['$sumScore', '$countScore'] } ] } } }"
    })
    List<GameStatsDTO> findMostDiscussedGames();


    @Aggregation(pipeline = {

            "{ $match: { countScore: { $gte: ?0 } } }",

            "{ $project: { _id: 1, name: 1, price: 1, discount: 1 , finalPrice: 1, avgRating: { $divide: ['$sumScore', '$countScore'] }, image: 1, genres: 1 } }",

            "{ $sort: { avgRating: -1, countScore: -1 } }",

            "{ $limit: 15 }",
    })
    List<GameStatsDTO> getTopRatedGames(int minReviews);

}

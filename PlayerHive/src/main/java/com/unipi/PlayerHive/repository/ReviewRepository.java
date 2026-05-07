package com.unipi.PlayerHive.repository;

import com.unipi.PlayerHive.DTO.reviews.ReviewDTO;
import com.unipi.PlayerHive.DTO.reviews.ReviewScoreDTO;
import com.unipi.PlayerHive.model.Review;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    Review removeById(String reviewId);

    List<ReviewDTO> findByIdInOrderByTimestampDesc(List<String> reviewIds);

    long removeByGameId(ObjectId gameId);

    long removeByUserId(ObjectId userId);

    Review removeByIdAndUserId(String reviewId, ObjectId requesterId);

    @Query("{ '_id': { $in: ?0 } }")
    List<ReviewScoreDTO> findGameScoreByIdIn(List<String> reviewIds);
}

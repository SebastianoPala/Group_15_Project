package com.unipi.PlayerHive.utility.batch;

import com.unipi.PlayerHive.DTO.reviews.UserReviewDTO;
import com.unipi.PlayerHive.model.Game;
import com.unipi.PlayerHive.repository.users.UserRepository;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;


public record GameConsistencyManager(MongoTemplate mongoTemplate) {

    public void removeUserReviewsFromGames(String userId, UserRepository userRepository){
        boolean reviews_left = true;
        int page_size = 10000;
        int step = 0;

        while(reviews_left){
            List<UserReviewDTO> reviews = userRepository.getUserReviews(userId,step,page_size).getReviews();
            step += page_size;

            if(reviews.isEmpty())
                break;
            else if(reviews.size() < page_size)
                reviews_left = false;

            BulkOperations bulkOps = mongoTemplate.bulkOps(
                    BulkOperations.BulkMode.UNORDERED,
                    Game.class
            );

            for (UserReviewDTO review : reviews) {
                Query query = new Query(Criteria.where("_id").is(review.getGameId()));

                Update update = new Update().pull("allReviews", review.getReviewId())
                        .pull("recentReviews",review.getReviewId());

                bulkOps.updateOne(query, update);
            }

            bulkOps.execute();

        }

    }

}

package com.unipi.PlayerHive.utility.batch;

import com.mongodb.bulk.BulkWriteResult;
import com.unipi.PlayerHive.DTO.reviews.UserReviewDTO;
import com.unipi.PlayerHive.model.game.Game;
import com.unipi.PlayerHive.repository.users.UserRepository;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class GameConsistencyManager {

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public GameConsistencyManager(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public long removeUserReviewsFromGames(String userId){
        boolean reviews_left = true;
        int page_size = 10000;
        int step = 0;

        long modified = 0;

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

            BulkWriteResult result = bulkOps.execute();
            modified += result.getModifiedCount();

        }

        return modified;

    }

}

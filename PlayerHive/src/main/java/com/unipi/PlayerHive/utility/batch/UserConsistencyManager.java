package com.unipi.PlayerHive.utility.batch;

import com.unipi.PlayerHive.DTO.reviews.OldGameReviewDTO;
import com.unipi.PlayerHive.DTO.users.GameOwnerDTO;
import com.unipi.PlayerHive.model.User;
import com.unipi.PlayerHive.repository.games.GameRepository;
import com.unipi.PlayerHive.repository.users.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public record UserConsistencyManager(MongoTemplate mongoTemplate) {

    public void adjustUserStatsAfterGameRemoval(Iterator<GameOwnerDTO> iterator) {

        List<GameOwnerDTO> batch = new ArrayList<>();
        int batchSize = 10000;

        while (iterator.hasNext()) {
            batch.add(iterator.next());

            if (batch.size() == batchSize) {
                batchDecreaseUserGameStats(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            batchDecreaseUserGameStats(batch);
        }
    }

    private void batchDecreaseUserGameStats(List<GameOwnerDTO> batch) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);

        for (GameOwnerDTO dto : batch) {
            Query query = new Query(Criteria.where("_id").is(dto.getId()));

            Update update = new Update()
                    .inc("hoursPlayed", -dto.getHoursPlayed())
                    .inc("numGames", -1);

            bulkOps.updateOne(query, update);
        }

        bulkOps.execute();
    }

    public void removeGameReviewsFromUsers(String gameId, GameRepository gameRepository){
        boolean reviews_left = true;
        int page_size = 10000;
        int step = 0;

        while(reviews_left){
            List<OldGameReviewDTO> reviews = gameRepository.getGameReviews(gameId,step,page_size).getReviews();
            step += page_size;

            if(reviews.isEmpty())
                break;
            else if(reviews.size() < page_size)
                reviews_left = false;

            BulkOperations bulkOps = mongoTemplate.bulkOps(
                    BulkOperations.BulkMode.UNORDERED,
                    User.class
            );

            for (OldGameReviewDTO review : reviews) {
                Query query = new Query(Criteria.where("_id").is(review.getUserId()));

                //Update update = new Update().pull("reviewIds", review.getReviewId());
                Update update = new Update().pull("reviewIds", new org.bson.Document("review_id", review.getReviewId()));

                bulkOps.updateOne(query, update);
            }

            bulkOps.execute();
        }
    }

    public void adjustFriendCountersAfterUserRemoval(Iterator<String> iterator, UserRepository userRepository){
        List<String> batch = new ArrayList<>();
        int batchSize = 10000;

        while (iterator.hasNext()) {
            batch.add(iterator.next());

            if (batch.size() == batchSize) {
                userRepository.decrementFriendCounterForUsers(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            userRepository.decrementFriendCounterForUsers(batch);
        }

    }
}

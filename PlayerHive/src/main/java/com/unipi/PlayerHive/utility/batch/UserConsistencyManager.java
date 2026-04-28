package com.unipi.PlayerHive.utility.batch;

import com.mongodb.bulk.BulkWriteResult;
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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class UserConsistencyManager {
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;

    public UserConsistencyManager(MongoTemplate mongoTemplate, UserRepository userRepository) {
        this.mongoTemplate = mongoTemplate;

        this.userRepository = userRepository;
    }

    public long adjustUserStatsAfterGameRemoval(Iterator<GameOwnerDTO> iterator) {

        List<GameOwnerDTO> batch = new ArrayList<>();
        int batchSize = 10000;

        long modified = 0;

        while (iterator.hasNext()) {
            batch.add(iterator.next());

            if (batch.size() == batchSize) {
                modified += batchDecreaseUserGameStats(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            modified += batchDecreaseUserGameStats(batch);
        }
        return modified;
    }

    private long batchDecreaseUserGameStats(List<GameOwnerDTO> batch) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);

        for (GameOwnerDTO dto : batch) {
            Query query = new Query(Criteria.where("_id").is(dto.getId()));

            Update update = new Update()
                    .inc("hoursPlayed", -dto.getHoursPlayed())
                    .inc("numGames", -1);

            bulkOps.updateOne(query, update);
        }

        BulkWriteResult result = bulkOps.execute();

        return result.getModifiedCount();
}

    public long adjustFriendCountersAfterUserRemoval(Iterator<String> iterator){
        List<String> batch = new ArrayList<>();
        int batchSize = 10000;

        long modified = 0;

        while (iterator.hasNext()) {
            batch.add(iterator.next());

            if (batch.size() == batchSize) {
                modified += userRepository.decrementFriendCounterForUsers(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            modified += userRepository.decrementFriendCounterForUsers(batch);
        }
        return modified;
    }
}

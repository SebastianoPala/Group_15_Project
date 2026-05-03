package com.unipi.PlayerHive.utility.batch;

import com.mongodb.bulk.BulkWriteResult;
import com.unipi.PlayerHive.DTO.users.GameOwnerDTO;
import com.unipi.PlayerHive.model.user.User;
import com.unipi.PlayerHive.repository.games.GameNeo4jRepository;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class UserConsistencyManager {
    private final GameNeo4jRepository gameNeo4jRepository;
    private final MongoTemplate mongoTemplate;

    public UserConsistencyManager(GameNeo4jRepository gameNeo4jRepository, MongoTemplate mongoTemplate) {
        this.gameNeo4jRepository = gameNeo4jRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public long adjustUserStatsAfterRemovalOf(String gameId) {

        int batchSize = 10000;

        long modified = 0;
        boolean relationshipsLeft = true;

        while (relationshipsLeft) {

            List<GameOwnerDTO> owners = gameNeo4jRepository.deletePlayedEdgesInBatch(gameId,batchSize);
            if(owners.isEmpty())
                break;
            else if(owners.size() < batchSize)
                relationshipsLeft = false;

            modified += batchDecreaseUserGameStats(owners);
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

}

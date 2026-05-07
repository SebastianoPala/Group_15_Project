package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.analytics.GenreStatsDTO;
import com.unipi.PlayerHive.DTO.analytics.OsPlatformStatsDTO;
import com.unipi.PlayerHive.DTO.analytics.ReleaseYearStatsDTO;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;

    public AnalyticsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // Average score and average hours per player, grouped by genre.
    // Only includes games that have at least one review and at least one player.
    public List<GenreStatsDTO> getGenreStats() {
        Aggregation agg = newAggregation(
            match(Criteria.where("countScore").gt(0).and("numPlayers").gt(0)),
            project()
                .andExpression("sumScore / countScore").as("avgScore")
                .andExpression("totalHoursPlayed / numPlayers").as("avgHoursPerPlayer")
                .and("genres").as("genres"),
            unwind("genres"),
            group("genres")
                .avg("avgScore").as("avgScore")
                .avg("avgHoursPerPlayer").as("avgHoursPerPlayer")
                .count().as("totalGames"),
            sort(org.springframework.data.domain.Sort.Direction.DESC, "avgScore")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "games", Document.class);

        return results.getMappedResults().stream()
                .map(doc -> new GenreStatsDTO(
                        doc.getString("_id"),
                        doc.getDouble("avgScore"),
                        doc.getDouble("avgHoursPerPlayer"),
                        doc.getInteger("totalGames")
                ))
                .toList();
    }

    // Average score grouped by number of supported operating systems.
    // Tests whether cross-platform games score differently from single-platform titles.
    public List<OsPlatformStatsDTO> getOsPlatformStats() {
        Aggregation agg = newAggregation(
            match(Criteria.where("countScore").gt(0)),
            project()
                .andExpression("size(supportedOS)").as("osCount")
                .andExpression("sumScore / countScore").as("avgScore"),
            group("osCount")
                .avg("avgScore").as("avgScore")
                .count().as("totalGames"),
            sort(org.springframework.data.domain.Sort.Direction.ASC, "_id")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "games", Document.class);

        return results.getMappedResults().stream()
                .map(doc -> new OsPlatformStatsDTO(
                        doc.getInteger("_id"),
                        doc.getDouble("avgScore"),
                        doc.getInteger("totalGames")
                ))
                .toList();
    }

    // Average score and game count grouped by release year.
    // Shows whether game quality on the platform has changed over time.
    public List<ReleaseYearStatsDTO> getReleaseYearStats() {
        Aggregation agg = newAggregation(
            match(Criteria.where("countScore").gt(0).and("release_date").ne(null)),
            project()
                .andExpression("year(release_date)").as("releaseYear")
                .andExpression("sumScore / countScore").as("avgScore"),
            group("releaseYear")
                .avg("avgScore").as("avgScore")
                .count().as("totalGames"),
            sort(org.springframework.data.domain.Sort.Direction.ASC, "_id")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "games", Document.class);

        return results.getMappedResults().stream()
                .map(doc -> new ReleaseYearStatsDTO(
                        doc.getInteger("_id"),
                        doc.getDouble("avgScore"),
                        doc.getInteger("totalGames")
                ))
                .toList();
    }
}

package com.unipi.PlayerHive.config;

import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Map;

@Component
public class MongoDebugRunner implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;
    private final Environment env;
    private final Neo4jClient neo4jClient; // Injected to test Neo4j

    public MongoDebugRunner(MongoTemplate mongoTemplate, Environment env, Neo4jClient neo4jClient) {
        this.mongoTemplate = mongoTemplate;
        this.env = env;
        this.neo4jClient = neo4jClient;
    }
// REMOVE THIS FILE ONCE TESTING IS OVER
// AI generated and completely useless for the project itself
    @Override
    public void run(String... args) {
        System.out.println("\n==============================================");
        System.out.println("====== DB CONNECTION & DATA DEBUG START ======         REMOVE WHEN DONE");
        System.out.println("==============================================");

        try {
            // 1. Check which database Spring actually connected to
            String dbName = mongoTemplate.getDb().getName();
            System.out.println("Connected MongoDB Name: " + dbName);

            // 2. Look for "Undertale" in MongoDB
            String searchName = "Undertale";
            System.out.println("\nSearching MongoDB for game: '" + searchName + "'...");

            Document foundGame = mongoTemplate.getDb().getCollection("games")
                    .find(new Document("name", searchName)).first();

            if (foundGame != null) {
                System.out.println(" -> SUCCESS! Game found in MongoDB.");

                // Extract the ObjectId and convert it to a Hex String
                String mongoId = foundGame.getObjectId("_id").toHexString();
                System.out.println(" -> Extracted Game ID: " + mongoId);

                // 3. Look for the corresponding Node in Neo4j
                System.out.println("\nSearching Neo4j for Node with game_id: '" + mongoId + "'...");

                try {
                    // Executing a raw Cypher query.
                    // Note: This assumes your Neo4j nodes have the label 'Game'.
                    Optional<Map<String, Object>> neo4jResult = neo4jClient
                            .query("MATCH (n:Game {game_id: $mongoId}) RETURN n.name AS name, n.game_id AS id")
                            .bind(mongoId).to("mongoId")
                            .fetch()
                            .first();

                    if (neo4jResult.isPresent()) {
                        System.out.println(" -> SUCCESS! Matching node found in Neo4j.");
                        System.out.println(" -> Node Name: " + neo4jResult.get().get("name"));
                    } else {
                        System.out.println(" -> WARNING: Game found in MongoDB, but NO matching node found in Neo4j!");
                    }
                } catch (Exception neo4jEx) {
                    System.err.println(" -> ERROR querying Neo4j: " + neo4jEx.getMessage());
                }

            } else {
                System.out.println(" -> WARNING: Game '" + searchName + "' was NOT found in MongoDB.");
            }

        } catch (Exception e) {
            System.err.println("Error during debug: " + e.getMessage());
        }

        System.out.println("==============================================");
        System.out.println("======= DB CONNECTION & DATA DEBUG END =======");
        System.out.println("==============================================\n");
    }
}
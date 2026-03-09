package com.unipi.PlayerHive.config; // Adjust package name as needed

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoDebugRunner implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    public MongoDebugRunner(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n==============================================");
        System.out.println("====== MONGODB CONNECTION DEBUG START ========");
        System.out.println("==============================================");

        try {
            // 1. Check which database Spring actually connected to
            String dbName = mongoTemplate.getDb().getName();
            System.out.println("Connected Database Name: " + dbName);

            // 2. List all collections in that database
            System.out.println("\nAvailable Collections in '" + dbName + "':");
            mongoTemplate.getCollectionNames().forEach(name -> System.out.println(" -> " + name));

            // 3. Test a raw query bypassing the Repository completely
            String testId = "68f0ea5db2c0422492358a5b";
            System.out.println("\nTesting raw query for Game ID: " + testId);

            // Try to find it as a strict ObjectId (like Compass shows)
            Document foundAsObjectId = mongoTemplate.getDb().getCollection("games")
                    .find(new Document("_id", new ObjectId(testId))).first();

            // Try to find it as a plain String (just in case)
            Document foundAsString = mongoTemplate.getDb().getCollection("games")
                    .find(new Document("_id", testId)).first();

            System.out.println("Found using ObjectId? " + (foundAsObjectId != null));
            System.out.println("Found using String? " + (foundAsString != null));

            if(foundAsObjectId != null) {
                System.out.println("Game Name found: " + foundAsObjectId.getString("name"));
            }

        } catch (Exception e) {
            System.err.println("Error during MongoDB debug: " + e.getMessage());
        }

        System.out.println("==============================================");
        System.out.println("======= MONGODB CONNECTION DEBUG END =========");
        System.out.println("==============================================\n");
    }
}
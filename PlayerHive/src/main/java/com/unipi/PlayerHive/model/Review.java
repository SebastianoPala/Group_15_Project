package com.unipi.PlayerHive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    @Field("game_id")
    private String gameId;

    @Field("user_id")
    private String userId;
    private String username;
    private String pfpURL;

    @Field("review_text")
    private String reviewText;

    private Float score;

    private LocalDateTime timestamp;
}

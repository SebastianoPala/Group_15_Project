package com.unipi.PlayerHive.DTO.games;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RecentReviewDTO {

    @Id
    private String id;

    @Field("user_id")
    private String userId;
    private String username;
    private String pfpURL;

    @Field("review_text")
    private String reviewText;

    private Float score;

    private LocalDateTime timestamp;
}

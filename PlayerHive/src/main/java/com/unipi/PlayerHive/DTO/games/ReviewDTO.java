package com.unipi.PlayerHive.DTO.games;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ReviewDTO {
    private String user_id;
    private String username;
    private String review_text;

    private Float score;

    private LocalDateTime timestamp;
}

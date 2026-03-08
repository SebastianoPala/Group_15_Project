package com.unipi.PlayerHive.model;

import jakarta.validation.constraints.NotBlank;
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

public class Review {
    private String user_id;
    private String username;
    private String review_text;

    @NotNull
    private Float score;

    private LocalDateTime timestamp;
}

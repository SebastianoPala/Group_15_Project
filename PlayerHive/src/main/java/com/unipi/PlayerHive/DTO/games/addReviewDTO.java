package com.unipi.PlayerHive.DTO.games;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class addReviewDTO {

    @Size(max = 255)
    private String reviewText;

    @NotNull
    @PositiveOrZero
    private Float score;
}

package com.unipi.PlayerHive.DTO.reviews;

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

public class AddReviewDTO {

    @Size(max = 255)
    private String reviewText;

    @NotNull
    @PositiveOrZero
    private Float score;
}

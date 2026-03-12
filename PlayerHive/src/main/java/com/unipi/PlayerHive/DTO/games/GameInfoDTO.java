package com.unipi.PlayerHive.DTO.games;

import com.unipi.PlayerHive.model.Game;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class GameInfoDTO {

    private String name;
    private String releaseDate;
    private Double price;
    private Integer discount;
    private String description;
    private List<ReviewDTO> reviews;
    private String imageURL;
    private List<String> supportedOS;
    private Integer achievements;
    private Float userScore;
    private Float averagePlaytime;
    private List<String> developers;
    private List<String> publishers;
    private List<String> genres;
}

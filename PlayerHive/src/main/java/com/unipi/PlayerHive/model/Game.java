package com.unipi.PlayerHive.model;

import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "games")
public class Game {
    @Id
    private String id;

    private String name;
    @Past
    private LocalDate releaseDate;
    private Integer requiredAge;
    private String price;
    private String discount;
    private String description;
    private List<String> supportedLanguages;
    private List<Review> reviews;
    private String imageURL;
    private List<String> supportedOS;
    private Float userScore;
    private Integer achievements;
    private Float averagePlaytime;
    private List<String> developers;
    private List<String> categories;
    private List<String> genres;

}

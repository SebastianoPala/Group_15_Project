package com.unipi.PlayerHive.DTO.games;

import com.unipi.PlayerHive.model.Review;

import java.time.LocalDate;
import java.util.List;

public class GameInfoDTO {
    private String name;
    private LocalDate releaseDate;
    private Integer requiredAge;
    private String price;
    private String discount;
    private String description;
    private List<String> supportedLanguages;
    private List<Review> reviews; // MANAGE THE REVIEWS???
    private String imageURL;
    private List<String> supportedOS;
    private Float userScore;
    private Integer achievements;
    private Float averagePlaytime;
    private List<String> developers;
    private List<String> categories;
    private List<String> genres;
}

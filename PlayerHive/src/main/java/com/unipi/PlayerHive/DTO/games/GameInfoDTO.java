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
    public GameInfoDTO(Game game){
        this.name = game.getName();
        this.releaseDate = game.getReleaseDate();
        this.price = game.getPrice();
        this.discount = game.getDiscount();
        this.description = game.getDescription();
        this.reviews = game.getReviews();
        this.imageURL = game.getImageURL();
        this.supportedOS = game.getSupportedOS();
        this.achievements = game.getAchievements();
        this.userScore = game.getUserScore();
        this.averagePlaytime = game.getAveragePlaytime();
        this.developers = game.getDevelopers();
        this.publishers = game.getPublishers();
        this.genres = game.getGenres();
    }

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

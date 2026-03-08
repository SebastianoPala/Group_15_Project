package com.unipi.PlayerHive.model;

import com.unipi.PlayerHive.DTO.games.ReviewDTO;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
    @Field("release_date")
    private String releaseDate;
    private Double price;
    private Integer discount;
    private String description;
    private List<ReviewDTO> reviews; //maybe limit the reviews to the M most recent?
    @Field("image")
    private String imageURL;
    private List<String> supportedOS;
    private Integer achievements;
    private Float userScore;
    private Float averagePlaytime;
    private List<String> developers;
    private List<String> publishers;
    private List<String> genres;

}

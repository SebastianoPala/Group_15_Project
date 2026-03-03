package com.unipi.PlayerHive.DTO.games;

import com.unipi.PlayerHive.model.Review;

import java.time.LocalDate;
import java.util.List;

public class GameReducedDTO {
    private String name;
    private String price;
    private String discount;
    private String imageURL;
    private List<String> supportedOS;
    private Float userScore;
    private List<String> genres;
}

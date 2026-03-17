package com.unipi.PlayerHive.DTO.games;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class EditGameDTO {
    private String name;
    private String releaseDate;
    private Double price;
    private Integer discount;
    private String description;
    private String imageURL;
    private List<String> supportedOS;
    private Integer achievements;
    private List<String> developers;
    private List<String> publishers;
    private List<String> genres;
}

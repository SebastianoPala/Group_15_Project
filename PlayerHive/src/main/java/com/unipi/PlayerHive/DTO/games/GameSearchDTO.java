package com.unipi.PlayerHive.DTO.games;

import com.unipi.PlayerHive.model.Game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class GameSearchDTO {

    private String id;
    private String name;
    private Double price;
    private Integer discount;
    private String imageURL;
}

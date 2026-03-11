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
    public GameSearchDTO(Game game){
        this.game_id = game.getId();
        this.name = game.getName();
        this.price = game.getPrice();
        this.discount = game.getDiscount();
        this.imageURL = game.getImageURL();
    }
    private String game_id;
    private String name;
    private Double price;
    private Integer discount;
    private String imageURL;
}

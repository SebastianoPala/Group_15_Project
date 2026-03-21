package com.unipi.PlayerHive.DTO.games;

import com.unipi.PlayerHive.model.Game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class GameSearchDTO {

    @Id
    private String id;
    private String name;
    private Double price;
    private Integer discount;
    private String imageURL;
}

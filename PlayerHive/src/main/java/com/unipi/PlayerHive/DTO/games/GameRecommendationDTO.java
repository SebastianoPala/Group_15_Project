package com.unipi.PlayerHive.DTO.games;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GameRecommendationDTO {
    private String name;
    private int friendsWhoPlay;
}

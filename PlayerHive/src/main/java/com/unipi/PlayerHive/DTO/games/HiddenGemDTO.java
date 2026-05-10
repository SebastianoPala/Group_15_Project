package com.unipi.PlayerHive.DTO.games;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HiddenGemDTO {

    private String name;
    private int friendsPlaying;
    private int globalPopularity;

}

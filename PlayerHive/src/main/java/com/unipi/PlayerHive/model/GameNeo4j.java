package com.unipi.PlayerHive.model;

import org.springframework.data.annotation.Id;

public class GameNeo4j {
    @Id
    private Long id;
    private String gameId;
    private String name;

    private Integer achievements;
    private String image; // change
}

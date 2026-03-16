package com.unipi.PlayerHive.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Node("Game")
@Data
public class GameNeo4j {
    @Id
    private String id;
    private String name;

    private Integer achievements;
    private String image; // change
}

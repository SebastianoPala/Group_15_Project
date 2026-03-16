package com.unipi.PlayerHive.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("User")
@Data
public class UserNeo4j {
    @Id
    private String id;
    private String username;
    private String pfpURL;
}

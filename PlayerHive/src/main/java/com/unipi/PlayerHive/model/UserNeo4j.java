package com.unipi.PlayerHive.model;

import org.springframework.data.annotation.Id;

public class UserNeo4j {
    @Id
    private String id;
    private String user_id; //fix
    private String username;
    private String pfpURL;
}

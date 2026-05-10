package com.unipi.PlayerHive.DTO.users;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GamingTwinDTO {
    private String username;
    private double jaccardSimilarity;
}

package com.unipi.PlayerHive.DTO.users;

import com.unipi.PlayerHive.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OwnProfileDTO {


    private String username;

    private String role;

    private String email;

    private String pfpURL;

    private int numGames;

    private float hoursPlayed;

    private LocalDate birthDate;

    private Integer friends;

    private Integer friendRequestsNumber; //only the number is sent

}

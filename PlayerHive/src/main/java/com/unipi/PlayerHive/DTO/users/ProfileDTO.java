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
public class ProfileDTO {
    public ProfileDTO(User user){
        this.username = user.getUsername();
        this.role = user.getRole();
        this.pfpURL = user.getPfpURL();
        this.numGames = user.getNumGames();
        this.hoursPlayed = user.getHoursPlayed();
        this.friends = user.getFriends();
    }
    private String username;

    private String role;

    private String pfpURL;

    private int numGames;

    private float hoursPlayed;

    private Integer friends;

}

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
        this.birthDate = user.getBirthDate();
        this.friends = user.getFriends();
        this.friendRequests = user.getFriendRequests().size();
    }
    private String username;

    private String role;

    private String pfpURL;

    private int numGames;

    private float hoursPlayed;

    private LocalDate birthDate;

    private Integer friends;

    private Integer friendRequests; //only the number is sent

}

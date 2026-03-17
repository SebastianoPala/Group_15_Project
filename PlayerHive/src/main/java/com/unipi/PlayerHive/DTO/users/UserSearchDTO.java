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
public class UserSearchDTO {

    private String username;

    private String role;

    private String pfpURL;

}

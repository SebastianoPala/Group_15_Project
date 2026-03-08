package com.unipi.PlayerHive.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequest {
    private String user_id;
    private String username;
    private String pfpURL;
    private LocalDateTime timestamp;
}

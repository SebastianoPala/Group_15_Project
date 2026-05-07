package com.unipi.PlayerHive.DTO.users.friends;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FriendRequestContainerDTO {
    List<FriendRequestDTO> friendRequests;

    int numPages;

    boolean isLastPage;
}

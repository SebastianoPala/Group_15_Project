package com.unipi.PlayerHive.DTO.listContainers;

import com.unipi.PlayerHive.DTO.users.friends.FriendRequestMongoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FriendRequestMongoArrayDTO {
    List<FriendRequestMongoDTO> friendRequests;
}

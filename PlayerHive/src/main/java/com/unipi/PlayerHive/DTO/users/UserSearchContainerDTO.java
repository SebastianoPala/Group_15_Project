package com.unipi.PlayerHive.DTO.users;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserSearchContainerDTO {

    private List<UserSearchDTO> searchResult;

    private boolean isLastPage;
}

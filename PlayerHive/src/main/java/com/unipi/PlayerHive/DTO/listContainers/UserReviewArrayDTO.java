package com.unipi.PlayerHive.DTO.listContainers;

import com.unipi.PlayerHive.DTO.reviews.UserReviewDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class UserReviewArrayDTO {
    private List<UserReviewDTO> reviews;
}

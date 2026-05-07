package com.unipi.PlayerHive.DTO.listContainers;

import com.unipi.PlayerHive.DTO.reviews.OldGameReviewDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class OldGameReviewArrayDTO {
    List<OldGameReviewDTO> reviews;
}

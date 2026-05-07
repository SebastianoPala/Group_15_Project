package com.unipi.PlayerHive.DTO.reviews;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewContainerDTO {

    List<ReviewDTO> reviews;

    int numPages;

    boolean isLastPage;
}

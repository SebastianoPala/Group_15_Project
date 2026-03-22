package com.unipi.PlayerHive.utility;

import com.unipi.PlayerHive.DTO.games.RecentReviewDTO;
import com.unipi.PlayerHive.model.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    RecentReviewDTO reviewToRecentReviewDTO(Review review);
}

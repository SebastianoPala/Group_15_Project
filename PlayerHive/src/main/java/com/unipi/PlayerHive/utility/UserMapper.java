package com.unipi.PlayerHive.utility;

import com.unipi.PlayerHive.DTO.users.OwnProfileDTO;
import com.unipi.PlayerHive.DTO.users.ProfileDTO;
import com.unipi.PlayerHive.DTO.users.UserSearchDTO;
import com.unipi.PlayerHive.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    public ProfileDTO userToProfileDTO(User user);

    public OwnProfileDTO userToOwnProfileDTO(User user);

    public UserSearchDTO userToUserSearchDTO(User user);

}

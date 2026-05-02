package com.unipi.PlayerHive.utility.map;

import com.unipi.PlayerHive.DTO.games.*;
import com.unipi.PlayerHive.model.game.Game;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GameMapper {

    GameInfoDTO gameToGameInfoDTO(Game game);

    Game editGameDTOtoGame(AddGameDTO addGameDTO);
}
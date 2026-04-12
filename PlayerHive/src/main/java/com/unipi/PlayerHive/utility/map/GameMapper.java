package com.unipi.PlayerHive.utility.map;

import com.unipi.PlayerHive.DTO.games.AddGameDTO;
import com.unipi.PlayerHive.DTO.games.GameInfoDTO;
import com.unipi.PlayerHive.DTO.games.GameSearchDTO;
import com.unipi.PlayerHive.DTO.games.LightGameDTO;
import com.unipi.PlayerHive.model.Game;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GameMapper {

    GameInfoDTO gameToGameInfoDTO(Game game);

    GameSearchDTO gameToGameSearchDTO(Game game);

    Game editGameDTOtoGame(AddGameDTO addGameDTO);

    GameInfoDTO gameLightDTOToGameInfoDTO(LightGameDTO game);
}
package com.unipi.PlayerHive.utility;

import com.unipi.PlayerHive.DTO.games.EditGameDTO;
import com.unipi.PlayerHive.DTO.games.GameInfoDTO;
import com.unipi.PlayerHive.DTO.games.GameSearchDTO;
import com.unipi.PlayerHive.model.Game;
import com.unipi.PlayerHive.model.GameNeo4j;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GameMapper {

    GameInfoDTO gameToGameInfoDTO(Game game);

    GameSearchDTO gameToGameSearchDTO(Game game);

    Game editGameDTOtoGame(EditGameDTO editGameDTO);

}
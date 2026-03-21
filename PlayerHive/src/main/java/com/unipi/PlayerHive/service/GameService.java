package com.unipi.PlayerHive.service;

import com.unipi.PlayerHive.DTO.games.GameInfoDTO;
import com.unipi.PlayerHive.DTO.games.GameSearchDTO;
import com.unipi.PlayerHive.DTO.games.addReviewDTO;
import com.unipi.PlayerHive.model.Game;
import com.unipi.PlayerHive.repository.games.GameNeo4jRepository;
import com.unipi.PlayerHive.repository.games.GameRepository;
import com.unipi.PlayerHive.utility.GameMapper;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameNeo4jRepository gameNeo4jRepository; // probably removable
    private final GameMapper gameMapper;

    public GameService(GameRepository gameRepository,
                       GameNeo4jRepository gameNeo4jRepository, GameMapper gameMapper
    ){
        this.gameRepository = gameRepository;
        this.gameNeo4jRepository = gameNeo4jRepository;
        this.gameMapper = gameMapper;
    }

    public GameInfoDTO getGameById(String gameId) { // manage high reviews number case
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new NoSuchElementException("Game not found"));

        GameInfoDTO gameInfo = gameMapper.gameToGameInfoDTO(game);

        Float userScore = (game.getCountScore() > 0) ? game.getSumScore() / game.getCountScore() : null;
        gameInfo.setUserScore(userScore);

        Float avgPlay = (game.getNumPlayers() > 0) ? game.getTotalHoursPlayed() / game.getNumPlayers() : 0;
        gameInfo.setAveragePlaytime(avgPlay);

        return gameInfo;
    }

    public List<GameSearchDTO> searchGameByName(String gameName) { // paginate results
        List<Game> searchResult = gameRepository.searchByName(gameName).orElseThrow(() -> new RuntimeException("An error has occurred"));

        if(searchResult.isEmpty())
            throw new NoSuchElementException("No games matching the search parameters were found");

        return searchResult.stream()
                .map(gameMapper::gameToGameSearchDTO)
                .toList();
    }

    public void addReview(@Valid @RequestBody addReviewDTO addReviewDTO) {
    }


    public void deleteReviewFromGame(String gameId) { // where do we get the username from??
    }
}

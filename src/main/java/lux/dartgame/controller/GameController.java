package lux.dartgame.controller;

import lux.dartgame.dto.GameResponse;
import lux.dartgame.dto.PlayedRoundRequest;
import lux.dartgame.service.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("api/session/{id}/games")
public class GameController {
    private final GameService gameService;

    public GameController(final GameService gameServiceParam) {
        this.gameService = gameServiceParam;
    }

    @GetMapping
    public GameResponse startGame(final @RequestParam String gametype,
                                  final @PathVariable long id,
                                  final Principal principal) {
        return gameService.startGame(gametype, id, principal.getName());
    }

    @PutMapping
    public GameResponse playround(final @RequestBody PlayedRoundRequest roundResults,
                                  final @PathVariable long id,
                                  final Principal principal) {
        return gameService.playRound(roundResults, principal.getName(), id);
    }
}

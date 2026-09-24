package lux.dartgame.controller;

import lux.dartgame.dto.PlayerStatResponse;
import lux.dartgame.dto.UserRequest;
import lux.dartgame.service.PlayerStatsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/playerStats")
public class PlayerStatsController {
    private final PlayerStatsService playerStatService;

    public PlayerStatsController(final PlayerStatsService playerStatServiceParam) {
        this.playerStatService = playerStatServiceParam;
    }

    @PostMapping
    public List<PlayerStatResponse> getPlayerStats(final @RequestBody List<UserRequest> players) {
        return playerStatService.getPlayerStats(players);
    }
}

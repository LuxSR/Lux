package lux.dartgame.controller;

import lux.dartgame.dto.GametypeResponse;
import lux.dartgame.service.GametypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/gamemode")
public final class GameModeController {
    private final GametypeService gametypeService;

    public GameModeController(final GametypeService gametypeServiceParam) {
        this.gametypeService = gametypeServiceParam;
    }

    @GetMapping
    public List<GametypeResponse> findAllModes() {
        return gametypeService.findAll();
    }
}

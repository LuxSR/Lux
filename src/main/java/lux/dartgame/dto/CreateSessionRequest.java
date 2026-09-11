package lux.dartgame.dto;

import jakarta.validation.constraints.Size;
import lux.dartgame.constants.Constants;

import java.util.List;
import java.util.Set;

public record CreateSessionRequest(
        @Size(max = Constants.MAX_NR_OF_PLAYERS) Set<UserRequest> players,
        List<GameRequest> games
) { }

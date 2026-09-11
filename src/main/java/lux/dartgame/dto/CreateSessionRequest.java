package lux.dartgame.dto;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record CreateSessionRequest(
        @Size(max = 10) Set<UserRequest> players,
        List<GameRequest> games
) { }

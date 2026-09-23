package lux.dartgame.dto;

public record FinishedGameResponse(String gametype,
                                   long gameId,
                                   String winner) {
}

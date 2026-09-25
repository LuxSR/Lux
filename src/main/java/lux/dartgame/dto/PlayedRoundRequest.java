package lux.dartgame.dto;

public record PlayedRoundRequest(String username,
                                 String score,
                                 long gameId) {
}

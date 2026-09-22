package lux.dartgame.dto;

public record GameResponse(String gametype,
                            String turn,
                            long gameId,
                            boolean isFinished) {
}

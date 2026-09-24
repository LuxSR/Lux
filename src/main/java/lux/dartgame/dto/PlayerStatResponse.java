package lux.dartgame.dto;

public record PlayerStatResponse(String username,
                                 float avgPoints,
                                 int wonGames,
                                 int playedGames,
                                 int triple20s,
                                 int bullseyes,
                                 int highestScore,
                                 int highestCheckout,
                                 float avgCheckoutAccuracy) {
}

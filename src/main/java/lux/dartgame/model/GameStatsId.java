package lux.dartgame.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class GameStatsId implements Serializable {
    private long userId;
    private long gameId;

    public GameStatsId() { }

    public GameStatsId(final long userIdParam, final long gameIdParam) {
        this.userId = userIdParam;
        this.gameId = gameIdParam;
    }
}

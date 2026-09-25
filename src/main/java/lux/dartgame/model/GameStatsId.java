package lux.dartgame.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class GameStatsId implements Serializable {
    private long userId;
    private long gameId;

    public GameStatsId() { }

    public GameStatsId(final long userIdParam, final long gameIdParam) {
        this.userId = userIdParam;
        this.gameId = gameIdParam;
    }
}

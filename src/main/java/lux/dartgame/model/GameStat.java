package lux.dartgame.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "Gamestats")
public class GameStat {
    @EmbeddedId
    private GameStatsId gameStatsId = new GameStatsId();

    // Defines the composite primary key composed of userId and gameId
    @ManyToOne
    @MapsId("userId")
    private User user;

    @ManyToOne
    @MapsId("gameId")
    private Game game;

    private float avgPoints;
    private int triple20s;
    private int bullseyes;
    private int highestScore;
}

package lux.dartgame.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Gamestats")
@Getter
@Setter
public class GameStat {
    @EmbeddedId
    private GameStatsId gameStatsId = new GameStatsId();

    // Defines the composite primary key composed of userId and gameId
    @JoinColumn(name = "user_id")
    @ManyToOne
    @MapsId("userId")
    private User user;

    @JoinColumn(name = "game_id")
    @ManyToOne
    @MapsId("gameId")
    private Game game;

    private float avgPoints;
    private int triple20s;
    private int bullseyes;
    private int highestScore;
    private float checkoutAccuracy;
    private int highestCheckout;
}

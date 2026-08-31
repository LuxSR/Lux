package lux.dartgame.model;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "Games")
public class Game {

    @Id
    @GeneratedValue
    private long gameId;
    private Instant playedAt;

    @Enumerated(EnumType.STRING)
    private Gametype gametype;


    // A session can hold many games
    @ManyToOne
    private Session session;

    // A user can win many games
    @ManyToOne
    private User winner;
}

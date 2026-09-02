package lux.dartgame.model;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Games")
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue
    private long gameId;
    private Instant playedAt;

    @ManyToMany
    @JoinTable(
            name = "game_mm_gametypes",
            joinColumns = @JoinColumn(name = "gameId"),
            inverseJoinColumns = @JoinColumn(name = "gametypeId")
    )
    private List<Gametype> gametypes;

    // A session can hold many games
    @ManyToOne
    private Session session;

    // A user can win many games
    @ManyToOne
    private User winner;
}

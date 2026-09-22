package lux.dartgame.model;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "Games")
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long gameId;

    @CreationTimestamp
    private Instant playedAt;

    @NotNull
    @JoinColumn(name = "gametype_id")
    @ManyToOne
    private Gametype gametype;

    // A session can hold many games
    @NotNull
    @JoinColumn(name = "session_id", nullable = false)
    @ManyToOne
    private Session session;

    // A user can win many games
    @JoinColumn(name = "winner_id")
    @ManyToOne
    private User winner;

    @NotNull
    private int nrOfPlayers;

    // A game can have many gamestats (one per participating user)
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameStat> gameStats = new ArrayList<>();

    // TODO add logic for how a deleted user affects sessions.
    // all players in a session must be deleted for it to delete.
}

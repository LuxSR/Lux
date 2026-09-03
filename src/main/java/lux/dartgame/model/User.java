package lux.dartgame.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue
    private long userId;

    @NotBlank
    @Column(unique = true)
    private String userName;

    @NotBlank
    private String password;

    // A user can have many sessions
    @OneToMany(mappedBy = "owner")
    private List<Session> ownerSessions = new ArrayList<>();

    // A game can only be won by one user
    @OneToMany(mappedBy = "winner")
    private List<Game> games = new ArrayList<>();

    // Each player has its own stats.
    @OneToOne (mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private PlayerStat stats;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameStat> gameStats = new ArrayList<>();

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne
    private Role role;

    @ManyToMany(mappedBy = "players")
    private List<Session> sessionsAsPlayer = new ArrayList<>();
}

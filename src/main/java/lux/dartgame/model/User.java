package lux.dartgame.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

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
    private String userName;

    // A user can have many sessions
    @OneToMany(mappedBy = "user")
    private List<Session> sessions = new ArrayList<>();

    // A game can only be won by one user
    @OneToMany(mappedBy = "winner")
    private List<Game> games = new ArrayList<>();

    // Each player has its own stats.
    @OneToOne (mappedBy = "player")
    private PlayerStat stats;
}

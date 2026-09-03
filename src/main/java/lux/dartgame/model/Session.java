package lux.dartgame.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Sessions")
@Getter
@Setter
public class Session {

    @Id
    @GeneratedValue
    private long sessionId;

    private boolean isActive;

    // A session can be owned by one User
    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne
    private User owner;

    @NotNull
    @ManyToMany
    @JoinTable(
            name = "session_mm_users",
            joinColumns = @JoinColumn(name = "sessionId"),
            inverseJoinColumns = @JoinColumn(name = "userId")
    )
    private List<User> players  = new ArrayList<>();

    // A game can only be part of one Session.
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Game> games = new ArrayList<>();
}

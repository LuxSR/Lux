package lux.dartgame.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

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

    // A session can be owned by one User
    @ManyToOne
    private User user;

    // A game can only be part of one Session.
    @OneToMany(mappedBy = "session")
    private List<Game> games = new ArrayList<>();
}

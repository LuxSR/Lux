package lux.dartgame.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Playerstats")
@Getter
@Setter
public class PlayerStat {

    @Id
    @GeneratedValue
    private long playerStatId;
    private float avgPoints;
    private int wonGames;
    private int playedGames;
    private int triple20s;
    private int bullseyes;
    private int highestScore;
    private int highestCheckout;
    private float avgCheckoutAccuracy;

    @NotNull
    @JoinColumn(nullable = false)
    @OneToOne
    private User player;
}

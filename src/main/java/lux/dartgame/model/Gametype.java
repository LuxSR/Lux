package lux.dartgame.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table (name = "Gametypes")
@Getter
@Setter
public class Gametype {
    @Id
    @GeneratedValue()
    private long gametypeId;
    private String gametype;

    @ManyToMany(mappedBy = "gametypes")
    private List<Game> games;
}

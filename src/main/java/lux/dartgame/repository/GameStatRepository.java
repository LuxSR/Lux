package lux.dartgame.repository;

import lux.dartgame.model.GameStat;
import lux.dartgame.model.GameStatsId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameStatRepository
        extends JpaRepository<GameStat, GameStatsId> {
}

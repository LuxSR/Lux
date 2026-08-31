package lux.dartgame.repository;

import lux.dartgame.model.PlayerStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerStatRepository extends JpaRepository<PlayerStat, Long> {
}

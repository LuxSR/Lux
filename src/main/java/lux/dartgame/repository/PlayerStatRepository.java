package lux.dartgame.repository;

import lux.dartgame.model.PlayerStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerStatRepository extends JpaRepository<PlayerStat, Long> {
    List<PlayerStat> findByPlayerUserNameIn(List<String> usernames);
}

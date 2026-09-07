package lux.dartgame.repository;

import lux.dartgame.model.Gametype;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GametypeRepository extends JpaRepository<Gametype, Long> {

    Gametype findByGametype(String gametype);
}

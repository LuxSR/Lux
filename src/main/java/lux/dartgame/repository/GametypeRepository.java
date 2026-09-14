package lux.dartgame.repository;

import lux.dartgame.model.Gametype;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GametypeRepository extends JpaRepository<Gametype, Long> {

    Optional<Gametype> findByGametype(String gametype);
}

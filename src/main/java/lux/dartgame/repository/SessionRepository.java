package lux.dartgame.repository;

import lux.dartgame.dto.SessionResponse;
import lux.dartgame.model.Session;
import lux.dartgame.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByOwner(User owner);
}

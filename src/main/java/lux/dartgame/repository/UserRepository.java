package lux.dartgame.repository;

import lux.dartgame.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String username);

    boolean existsByUserName(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u.role.role FROM User u WHERE u.userName = :username")
    Optional<String> findRoleByUserName(@Param("username") String username);
}

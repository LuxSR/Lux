package lux.dartgame.service;

import lombok.extern.slf4j.Slf4j;
import lux.dartgame.dto.GameRequest;
import lux.dartgame.dto.SessionResponse;
import lux.dartgame.dto.UserRequest;
import lux.dartgame.model.Game;
import lux.dartgame.model.Gametype;
import lux.dartgame.model.Session;
import lux.dartgame.model.User;
import lux.dartgame.repository.GameRepository;
import lux.dartgame.repository.SessionRepository;
import lux.dartgame.repository.UserRepository;
import lux.dartgame.repository.GametypeRepository;
import lux.dartgame.exception.UsernameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public final class SessionService {

    private final SessionRepository sessionRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final GametypeRepository gametypeRepository;

    private final JwtService jwtService;

    @Autowired
    public SessionService(final SessionRepository sessionRepositoryParam,
                          final GameRepository gameRepositoryParam,
                          final UserRepository userRepositoryParam,
                          final GametypeRepository gametypeRepositoryParam,
                          final JwtService jwtServiceParam) {
        this.sessionRepository = sessionRepositoryParam;
        this.gameRepository = gameRepositoryParam;
        this.userRepository = userRepositoryParam;
        this.gametypeRepository = gametypeRepositoryParam;
        this.jwtService = jwtServiceParam;
    }

    public SessionResponse startSession(final Optional<List<GameRequest>> games,
                                         final Optional<Set<UserRequest>> players,
                                         final String authHeaderParam) {

        String authHeader = authHeaderParam.replace("Bearer ", "");
        String username = jwtService.extractUsername(authHeader);

        log.info("Attempting to create session for {}", username);

        User owner = userRepository.findByUserName(username)
                                   .orElseThrow(UsernameNotFoundException::new);

        Session session = new Session();
        session.setOwner(owner);

        if (players.isEmpty()) {
            session.addPlayers(owner);
        } else {
            session.setPlayers(players.get().stream()
                    .map(player -> userRepository.findByUserName(player.username())
                            .orElseThrow(UsernameNotFoundException::new))
                    .collect(Collectors.toSet()));

            // Users should not be able to add themselves to the session.
            // Make sure the owner is also a player.
            session.addPlayers(owner);
        }

        if (games.isPresent()) {
            for (GameRequest gamemode : games.get()) {
                Gametype gametype = gametypeRepository.findByGametype(gamemode.gameType());
                Game game = new Game();

                game.setGametype(gametype);
                session.addGame(game);
            }
        }

        sessionRepository.save(session);
        log.info("Session created succesfully for {}", username);
        return null;
    }
}

package lux.dartgame.service;

import lombok.extern.slf4j.Slf4j;
import lux.dartgame.dto.GameRequest;
import lux.dartgame.dto.SessionResponse;
import lux.dartgame.dto.UserRequest;
import lux.dartgame.exception.AccessDeniedException;
import lux.dartgame.exception.NoSessionsForThisUserException;
import lux.dartgame.exception.SessionNotFoundException;
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

import static org.apache.commons.lang3.math.NumberUtils.toLong;

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

    private String getUserFromAuthHeader(final String authHeaderParam) {
        String authHeader = authHeaderParam.replace("Bearer ", "");
        return jwtService.extractUsername(authHeader);
    }

    public List<SessionResponse> getSession(final String username) {
        log.info("Looking for all sessions owned by {}", username);

        User owner = userRepository.findByUserName(username)
                                   .orElseThrow(UsernameNotFoundException::new);

        List<Session> sessions = sessionRepository.findByOwner(owner);

        if (sessions.isEmpty()) {
            throw new NoSessionsForThisUserException(owner.getUserName());
        }

        return sessions.stream()
                .map(s -> new SessionResponse(s.getSessionId(), owner.getUserName()))
                .collect(Collectors.toList());
    }

    public SessionResponse startSession(final Optional<List<GameRequest>> games,
                                         final Optional<Set<UserRequest>> players,
                                         final String authHeader) {

        String username = getUserFromAuthHeader(authHeader);
        log.info("Attempting to create session for {}", username);

        User owner = userRepository.findByUserName(username)
                                   .orElseThrow(UsernameNotFoundException::new);

        Session session = new Session();
        session.setOwner(owner);
        session.setActive(true);

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
        log.info("Session created successfully for {}", username);
        return new SessionResponse(session.getSessionId(), owner.getUserName());
    }

    public void deleteSession(final String sessionId, final String authHeader) {

        String username = getUserFromAuthHeader(authHeader);
        log.info("{} wants to delete session {}", username, sessionId);

        Session session = sessionRepository.findById(toLong(sessionId))
                .orElseThrow(SessionNotFoundException::new);

        boolean isOwner = session.getOwner().getUserName().equals(username);
        boolean isAdmin = "ADMIN".equals(userRepository
                .findRoleByUserName(username)
                .orElseThrow(UsernameNotFoundException::new));

        // If session is not active and user is not the owner, or user is not admin
        boolean canDelete = (isOwner && session.isActive())
                || (isAdmin && !isOwner && !session.isActive());

        if (!canDelete) {
            throw new AccessDeniedException();
        }

        sessionRepository.deleteById(toLong(sessionId));
        log.info("Successfully deleted session {}", sessionId);
    }
}

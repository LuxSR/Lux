package lux.dartgame.service;

import lombok.extern.slf4j.Slf4j;
import lux.dartgame.dto.GameRequest;
import lux.dartgame.dto.GametypeResponse;
import lux.dartgame.dto.SessionResponse;
import lux.dartgame.dto.UserRequest;
import lux.dartgame.exception.AccessDeniedException;
import lux.dartgame.exception.GameModeNotFoundException;
import lux.dartgame.exception.NoSessionsForThisUserException;
import lux.dartgame.exception.SessionNotFoundException;
import lux.dartgame.model.Game;
import lux.dartgame.model.Gametype;
import lux.dartgame.model.Session;
import lux.dartgame.model.User;
import lux.dartgame.repository.SessionRepository;
import lux.dartgame.repository.UserRepository;
import lux.dartgame.repository.GametypeRepository;
import lux.dartgame.exception.UsernameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.math.NumberUtils.toLong;

@Slf4j
@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final GametypeRepository gametypeRepository;

    @Autowired
    public SessionService(final SessionRepository sessionRepositoryParam,
                          final UserRepository userRepositoryParam,
                          final GametypeRepository gametypeRepositoryParam) {
        this.sessionRepository = sessionRepositoryParam;
        this.userRepository = userRepositoryParam;
        this.gametypeRepository = gametypeRepositoryParam;
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getSession(final String username) {
        log.info("Looking for all sessions owned by {}", username);

        User owner = userRepository.findByUserName(username)
                                   .orElseThrow(UsernameNotFoundException::new);

        List<Session> sessions = sessionRepository.findByOwner(owner);

        if (sessions.isEmpty()) {
            throw new NoSessionsForThisUserException(owner.getUserName());
        }

        return sessions.stream()
                .map(s -> new SessionResponse(s.getSessionId(),
                                                      s.getPlayedAt().toString(),
                                                      s.getGames()
                                                          .stream()
                                                          .map(Game::getGametype)
                                                          .map(g -> new GametypeResponse(
                                                                  g.getGametype()))
                                                          .toList(),
                                                      s.isActive()))
                .collect(Collectors.toList());
    }

    @Transactional
    public SessionResponse createSession(final Optional<List<GameRequest>> games,
                                         final Optional<Set<UserRequest>> players,
                                         final String username) {
        log.info("Attempting to create session for {}", username);

        User owner = userRepository.findByUserName(username)
                                   .orElseThrow(UsernameNotFoundException::new);

        Session session = new Session();
        session.setOwner(owner);
        session.setActive(true);

        if (players.isEmpty()) {
            session.addPlayers(owner);
        } else {
            log.info("Attempting to add players to session");
            session.setPlayers(players.get().stream()
                    .map(player -> userRepository.findByUserName(player.username())
                            .orElseThrow(UsernameNotFoundException::new))
                    .peek(player -> log.info("Adding player {} to session", player))
                    .collect(Collectors.toSet()));

            // The owner is always a player in their own session.
            session.addPlayers(owner);
        }

        if (games.isPresent()) {
            for (GameRequest gamemode : games.get()) {
                Gametype gametype = gametypeRepository.findByGametype(gamemode.gameType())
                        .orElseThrow(() -> new GameModeNotFoundException(gamemode.gameType()));
                Game game = new Game();
                game.setGametype(gametype);
                session.addGame(game);
            }
        }

        sessionRepository.save(session);
        log.info("Session created successfully for {}", username);
        return new SessionResponse(session.getSessionId(),
                                    session.getPlayedAt().toString(),
                                    session.getGames().stream()
                                            .map(Game::getGametype)
                                            .map(g -> new GametypeResponse(
                                                    g.getGametype()))
                                            .toList(),
                                    session.isActive());
    }

    public void deleteSession(final String sessionId, final String username) {
        log.info("{} wants to delete session {}", username, sessionId);

        Session session = sessionRepository.findById(toLong(sessionId))
                .orElseThrow(SessionNotFoundException::new);

        boolean isOwner = session.getOwner().getUserName().equals(username);
        boolean isAdmin = "ADMIN".equals(userRepository
                .findRoleByUserName(username)
                .orElseThrow(UsernameNotFoundException::new));

        // If session is not active and user is not the owner, or user is not admin
        boolean canDelete = (isOwner && session.isActive())
                || isAdmin;

        if (!canDelete) {
            throw new AccessDeniedException();
        }

        sessionRepository.delete(session);
        log.info("Successfully deleted session {}", sessionId);
    }
}

package lux.dartgame.service;

import lombok.extern.slf4j.Slf4j;
import lux.dartgame.dto.GameRequest;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.math.NumberUtils.toLong;

@Slf4j
@Service
public final class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final GametypeRepository gametypeRepository;

    private final JwtService jwtService;

    @Autowired
    public SessionService(final SessionRepository sessionRepositoryParam,
                          final UserRepository userRepositoryParam,
                          final GametypeRepository gametypeRepositoryParam,
                          final JwtService jwtServiceParam) {
        this.sessionRepository = sessionRepositoryParam;
        this.userRepository = userRepositoryParam;
        this.gametypeRepository = gametypeRepositoryParam;
        this.jwtService = jwtServiceParam;
    }

    // REVIEW(sec): .replace("Bearer ", "") removes that substring everywhere in the string, not just from the front. Harmless for a well-formed header, but it is the sort of shortcut that becomes a bug the day a token legitimately contains those characters. If you keep this at all, use header.substring(7) after checking startsWith.
    // REVIEW(noob): this whole method should not exist. The JWT was already verified by JwtAuthenticationFilter and the username is in the SecurityContext. Parse it once, at the edge, and pass the authenticated user down.
    private String getUserFromAuthHeader(final String authHeaderParam) {
        String authHeader = authHeaderParam.replace("Bearer ", "");
        return jwtService.extractUsername(authHeader);
    }

    // REVIEW(sec): this method takes a username from its caller and returns that user's sessions with no check that the caller is that user. The controller passes a query parameter straight in (see SessionController line 42), so this is a working horizontal privilege escalation. Change the signature to take the authenticated principal.
    public List<SessionResponse> getSession(final String username) {
        log.info("Looking for all sessions owned by {}", username);

        User owner = userRepository.findByUserName(username)
                                   .orElseThrow(UsernameNotFoundException::new);

        List<Session> sessions = sessionRepository.findByOwner(owner);

        // REVIEW(api): an empty result is not an error. Throwing here turns 'this user has no sessions yet' into a 404, so the frontend has to treat a normal empty state as a failure. Return the empty list.
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

        // REVIEW(sec): the client sends a set of usernames and they are added to the session with no consent and no check on who they are. Anyone can create a session that claims to include any other user. Whether that matters depends on the product, but decide it deliberately rather than by accident, and at minimum verify each username exists (which you do) and that the owner is allowed to invite them.
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
                // REVIEW(efficiency): one query per requested gametype inside the loop. There are five gametypes in the whole system; load them once with findAll and match in memory, or use findByGametypeIn.
                Gametype gametype = gametypeRepository.findByGametype(gamemode.gameType())
                        .orElseThrow(() -> new GameModeNotFoundException(gamemode.gameType()));
                Game game = new Game();
                game.setGametype(gametype);
                session.addGame(game);
            }
        }

        // REVIEW(noob): this method writes several entities and is not @Transactional. If the save fails halfway you get a partially built session. Any service method that mutates more than one row should carry @Transactional.
        sessionRepository.save(session);
        log.info("Session created successfully for {}", username);
        return new SessionResponse(session.getSessionId(), owner.getUserName());
    }

    public void deleteSession(final String sessionId, final String authHeader) {

        String username = getUserFromAuthHeader(authHeader);
        log.info("{} wants to delete session {}", username, sessionId);

        // REVIEW(bug): NumberUtils.toLong returns 0 when the string does not parse, it does not throw. So DELETE /session?sessionId=haha quietly looks up session 0 and reports 'session not found', and a typo is indistinguishable from a missing row. Make the parameter a long (or a @PathVariable long) and let Spring reject a non-numeric value with a 400.
        // REVIEW(noob): org.apache.commons.lang3 is not declared in pom.xml. It is on the classpath only because some other dependency drags it in. The day that dependency changes, this stops compiling. Declare what you use, or just use Long.parseLong.
        Session session = sessionRepository.findById(toLong(sessionId))
                .orElseThrow(SessionNotFoundException::new);

        boolean isOwner = session.getOwner().getUserName().equals(username);
        // REVIEW(noob): comparing the role to the string literal "ADMIN" in the service, while Spring Security already has the authorities on the principal. @PreAuthorize("hasRole('ADMIN')") or authentication.getAuthorities() keeps one source of truth for roles.
        boolean isAdmin = "ADMIN".equals(userRepository
                .findRoleByUserName(username)
                .orElseThrow(UsernameNotFoundException::new));

        // If session is not active and user is not the owner, or user is not admin
        // REVIEW(bug): read this rule out loud: an owner can delete their session only while it is active, and an admin can delete only sessions that are inactive and not their own. So nobody can delete a finished session they own, and an admin cannot touch an active one. That is almost certainly not what you meant. Write the rule as two explicit cases with names (canOwnerDelete, canAdminDelete) and a test for each.
        // REVIEW(good): the ownership check itself is done server-side against the token identity rather than something the client sent. That part is right.
        boolean canDelete = (isOwner && session.isActive())
                || (isAdmin && !isOwner && !session.isActive());

        if (!canDelete) {
            throw new AccessDeniedException();
        }

        // REVIEW(efficiency): you already have the loaded Session two lines up. sessionRepository.delete(session) saves a second lookup.
        sessionRepository.deleteById(toLong(sessionId));
        log.info("Successfully deleted session {}", sessionId);
    }
}

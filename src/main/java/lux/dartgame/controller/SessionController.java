package lux.dartgame.controller;

import jakarta.validation.constraints.Size;
import lux.dartgame.dto.GameRequest;
import lux.dartgame.dto.SessionResponse;
import lux.dartgame.dto.UserRequest;
import lux.dartgame.service.JwtService;
import lux.dartgame.service.SessionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/session")
public final class SessionController {
    // TODO make constants.java file
    private static final int MAX_NR_OF_PLAYERS = 10;
    private final SessionService sessionService;
    private final JwtService jwtService;

    public SessionController(final SessionService sessionServiceParam,
                             final JwtService jwtServiceParam) {
        this.sessionService = sessionServiceParam;
        this.jwtService = jwtServiceParam;
    }

    @PostMapping
    public SessionResponse createSession(final @RequestParam(required = false)
                                             List<GameRequest> games,
                                         final @RequestParam(required = false)
                                               @Size(max = MAX_NR_OF_PLAYERS)
                                                    Set<UserRequest> players,
                                         final @RequestHeader("Authorization")
                                                String authHeader) {
        return sessionService.startSession(Optional.ofNullable(games),
                                           Optional.ofNullable(players),
                                           authHeader);
    }

    @GetMapping
    public List<SessionResponse> getSession(final @RequestParam String username,
                                       final @RequestHeader("Authorization")
                                                String authHeader) {
        return sessionService.getSession(username);
    }

    @DeleteMapping
    public void deleteSession(final @RequestParam String sessionId,
                              final @RequestHeader("Authorization")
                                   String authHeader) {
        sessionService.deleteSession(sessionId, authHeader);
    }
}

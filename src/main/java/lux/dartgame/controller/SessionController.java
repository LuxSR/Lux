package lux.dartgame.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import lux.dartgame.dto.GameRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lux.dartgame.dto.CreateSessionRequest;
import lux.dartgame.dto.FinishedGameResponse;
import lux.dartgame.dto.SessionResponse;
import lux.dartgame.service.SessionService;

@RestController
@RequestMapping("api/session")
public final class SessionController {
    private final SessionService sessionService;

    public SessionController(final SessionService sessionServiceParam) {
        this.sessionService = sessionServiceParam;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public SessionResponse createSession(final @RequestBody(required = false) @Valid
                                                CreateSessionRequest request,
                                         final Principal principal) {
        return sessionService.createSession(
                Optional.ofNullable(request != null ? request.games() : null),
                Optional.ofNullable(request != null ? request.players() : null),
                principal.getName());
    }

    @GetMapping
    public List<SessionResponse> getSession(final @RequestParam String username,
                                            final @RequestParam(required = false) GameRequest
                                                    gameRequest) {
        return sessionService.getSession(username, Optional.ofNullable(gameRequest));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{sessionId}")
    public void deleteSession(final @PathVariable long sessionId,
                              final Principal principal) {
        sessionService.deleteSession(sessionId, principal.getName());
    }

    @PutMapping("{sessionId}")
    public void finishSession(final @PathVariable long sessionId,
                              final Principal principal) {
        sessionService.finishSession(sessionId, principal.getName());
    }

    @PostMapping("{sessionId}")
    public SessionResponse addGames(final @PathVariable long sessionId,
                                    final @RequestBody @Valid List<@Valid GameRequest> games,
                                    final Principal principal) {
        return sessionService.addGames(sessionId, games, principal.getName());
    }

    @GetMapping("{id}")
    public SessionResponse getSession(final @PathVariable long id) {
        return sessionService.displaySession(id);
    }

    @GetMapping("{id}/finished-games")
    public List<FinishedGameResponse> getAllFinishedGames(final @PathVariable long id) {
        return sessionService.getAllFinishedGames(id);
    }
}

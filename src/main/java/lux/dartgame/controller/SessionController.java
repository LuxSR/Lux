package lux.dartgame.controller;

import lux.dartgame.dto.CreateSessionRequest;
import lux.dartgame.dto.SessionResponse;
import lux.dartgame.service.JwtService;
import lux.dartgame.service.SessionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/session")
public final class SessionController {
    private final SessionService sessionService;
    private final JwtService jwtService;

    public SessionController(final SessionService sessionServiceParam,
                             final JwtService jwtServiceParam) {
        this.sessionService = sessionServiceParam;
        this.jwtService = jwtServiceParam;
    }

    // REVIEW(api): @RequestBody(required = false) plus Optional.ofNullable plus a null check is three layers of defence against a missing body. Make the body required and give CreateSessionRequest sensible defaults, or add a separate no-body POST. Optional is designed as a return type, not a parameter type.
    @PostMapping
    public SessionResponse createSession(final @RequestBody(required = false)
                                                CreateSessionRequest request,
                                         // REVIEW(noob): passing the raw Authorization header down into the service is the wrong layering. Spring Security has already parsed and verified that token by the time this method runs; the principal is sitting in the SecurityContext. Taking the header again means SessionService re-parses the JWT (see getUserFromAuthHeader), which makes the service impossible to unit test without minting a token, and means the service silently trusts a string the controller did not check.
                                         final @RequestHeader("Authorization")
                                                String authHeader) {
        return sessionService.startSession(
                Optional.ofNullable(request != null ? request.games() : null),
                Optional.ofNullable(request != null ? request.players() : null),
                authHeader);
    }

    // REVIEW(sec): this is the most serious bug in the repo. The endpoint takes the username as a query parameter and hands it straight to the service, and the authHeader parameter below is accepted and then never used. So any logged-in user can read anybody else's sessions by changing ?username=. The identity of the caller must come from the token, never from the request: drop the parameter and take Authentication (or @AuthenticationPrincipal UserDetails) instead. If you later want an admin to read someone else's sessions, that is a separate, role-checked endpoint.
    // REVIEW(api): GET /session?username=x is also the wrong shape for the resource. The caller's own sessions are GET /sessions; another user's are GET /users/{id}/sessions behind an admin check.
    @GetMapping
    // REVIEW(noob): authHeader is declared and never read. The compiler will not tell you, but it makes the method look protected when it is not.
    public List<SessionResponse> getSession(final @RequestParam String username,
                                       final @RequestHeader("Authorization")
                                                String authHeader) {
        return sessionService.getSession(username);
    }

    // REVIEW(api): the id of the thing you are deleting belongs in the path, not a query parameter: DELETE /sessions/{sessionId}. As written, DELETE /session with no parameter is a 400 from the framework rather than a 404, and the endpoint does not identify a resource.
    @DeleteMapping
    public void deleteSession(final @RequestParam String sessionId,
                              final @RequestHeader("Authorization")
                                   String authHeader) {
        sessionService.deleteSession(sessionId, authHeader);
    }
}

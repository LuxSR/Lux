package lux.dartgame.controller;

import lux.dartgame.dto.CreateSessionRequest;
import lux.dartgame.dto.GameRequest;
import lux.dartgame.dto.GametypeResponse;
import lux.dartgame.dto.SessionResponse;
import lux.dartgame.dto.UserRequest;
import lux.dartgame.service.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    private static final String USERNAME = "alice";
    private static final String DATE = "2026-09-01T10:00:00Z";

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private SessionController sessionController;

    private Principal createPrincipal(final String username) {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(username);
        return principal;
    }

    private GametypeResponse gametype(final String name) {
        return new GametypeResponse(name);
    }

    @Test
    void createSession_delegatesToServiceAndReturnsResponse() {
        Principal principal = createPrincipal(USERNAME);
        SessionResponse response = new SessionResponse(42L, DATE,
                List.of(gametype("501")), true);
        List<GameRequest> games = List.of(new GameRequest("501"));
        Set<UserRequest> players = Set.of(new UserRequest("bob"));

        when(sessionService.createSession(Optional.of(games), Optional.of(players), USERNAME))
                .thenReturn(response);

        SessionResponse result = sessionController.createSession(
                new CreateSessionRequest(players, games), principal);

        assertThat(result).isEqualTo(response);
        verify(sessionService).createSession(Optional.of(games), Optional.of(players), USERNAME);
    }

    @Test
    void createSession_withoutGamesOrPlayersPassesEmptyOptionals() {
        Principal principal = createPrincipal(USERNAME);
        when(sessionService.createSession(Optional.empty(), Optional.empty(), USERNAME))
                .thenReturn(new SessionResponse(1L, DATE, List.of(), false));

        SessionResponse result = sessionController.createSession(null, principal);

        assertThat(result.id()).isEqualTo(1L);
        verify(sessionService).createSession(Optional.empty(), Optional.empty(), USERNAME);
    }

    @Test
    void getSession_delegatesToServiceAndReturnsAllSessions() {
        SessionResponse first = new SessionResponse(1L, DATE,
                List.of(gametype("501")), true);
        SessionResponse second = new SessionResponse(2L, DATE,
                List.of(gametype("301")), false);
        List<SessionResponse> responses = List.of(first, second);

        when(sessionService.getSession(USERNAME)).thenReturn(responses);

        List<SessionResponse> result = sessionController.getSession(USERNAME);

        assertThat(result).containsExactly(first, second);
        verify(sessionService).getSession(USERNAME);
    }

    @Test
    void deleteSession_delegatesToService() {
        Principal principal = createPrincipal(USERNAME);
        sessionController.deleteSession("7", principal);

        verify(sessionService).deleteSession("7", USERNAME);
    }
}

package lux.dartgame.controller;

import lux.dartgame.dto.GameRequest;
import lux.dartgame.dto.SessionResponse;
import lux.dartgame.dto.UserRequest;
import lux.dartgame.service.JwtService;
import lux.dartgame.service.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    private static final String AUTH_HEADER = "Bearer some-token";

    @Mock
    private SessionService sessionService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private SessionController sessionController;

    @Test
    void createSession_delegatesToServiceAndReturnsResponse() {
        SessionResponse response = new SessionResponse(42L, "alice");
        List<GameRequest> games = List.of(new GameRequest("501"));
        Set<UserRequest> players = Set.of(new UserRequest("bob"));

        when(sessionService.startSession(Optional.of(games), Optional.of(players), AUTH_HEADER))
                .thenReturn(response);

        SessionResponse result = sessionController.createSession(games, players, AUTH_HEADER);

        assertThat(result).isEqualTo(response);
        verify(sessionService).startSession(Optional.of(games), Optional.of(players), AUTH_HEADER);
    }

    @Test
    void createSession_withoutGamesOrPlayersPassesEmptyOptionals() {
        when(sessionService.startSession(Optional.empty(), Optional.empty(), AUTH_HEADER))
                .thenReturn(new SessionResponse(1L, "alice"));

        SessionResponse result = sessionController.createSession(null, null, AUTH_HEADER);

        assertThat(result.id()).isEqualTo(1L);
        verify(sessionService).startSession(Optional.empty(), Optional.empty(), AUTH_HEADER);
    }

    @Test
    void getSession_delegatesToServiceAndReturnsAllSessions() {
        List<SessionResponse> responses = List.of(
                new SessionResponse(1L, "alice"),
                new SessionResponse(2L, "alice"));

        when(sessionService.getSession("alice")).thenReturn(responses);

        List<SessionResponse> result = sessionController.getSession("alice", AUTH_HEADER);

        assertThat(result).containsExactly(
                new SessionResponse(1L, "alice"),
                new SessionResponse(2L, "alice"));
        verify(sessionService).getSession("alice");
    }

    @Test
    void deleteSession_delegatesToService() {
        sessionController.deleteSession("7", AUTH_HEADER);

        verify(sessionService).deleteSession("7", AUTH_HEADER);
    }
}

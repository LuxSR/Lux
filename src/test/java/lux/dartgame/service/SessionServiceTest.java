package lux.dartgame.service;

import lux.dartgame.dto.GameRequest;
import lux.dartgame.dto.SessionResponse;
import lux.dartgame.dto.UserRequest;
import lux.dartgame.exception.AccessDeniedException;
import lux.dartgame.exception.GameModeNotFoundException;
import lux.dartgame.exception.NoSessionsForThisUserException;
import lux.dartgame.exception.SessionNotFoundException;
import lux.dartgame.exception.UsernameNotFoundException;
import lux.dartgame.model.Gametype;
import lux.dartgame.model.Session;
import lux.dartgame.model.User;
import lux.dartgame.repository.GametypeRepository;
import lux.dartgame.repository.SessionRepository;
import lux.dartgame.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    private static final String OWNER_USERNAME = "alice";
    private static final String PLAYER_USERNAME = "bob";

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GametypeRepository gametypeRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private SessionService sessionService;

    private User user(final String username) {
        User user = new User();
        user.setUserName(username);
        return user;
    }

    private Session session(final long id, final boolean active, final User owner) {
        Session session = new Session();
        session.setSessionId(id);
        session.setActive(active);
        session.setOwner(owner);
        return session;
    }

    @Test
    void startSession_withoutGamesOrPlayers_addsOwnerAndSaves() {
        User owner = user(OWNER_USERNAME);
        when(userRepository.findByUserName(OWNER_USERNAME)).thenReturn(Optional.of(owner));

        SessionResponse result = sessionService.startSession(
                Optional.empty(), Optional.empty(), OWNER_USERNAME);

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        Session saved = captor.getValue();
        assertThat(saved.getOwner()).isEqualTo(owner);
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getPlayers()).containsExactly(owner);
        assertThat(result).isEqualTo(new SessionResponse(saved.getSessionId(), OWNER_USERNAME));
    }

    @Test
    void startSession_withPlayers_addsOwnerAutomatically() {
        User owner = user(OWNER_USERNAME);
        User player = user(PLAYER_USERNAME);
        when(userRepository.findByUserName(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(userRepository.findByUserName(PLAYER_USERNAME)).thenReturn(Optional.of(player));

        sessionService.startSession(
                Optional.empty(),
                Optional.of(Set.of(new UserRequest(PLAYER_USERNAME))),
                OWNER_USERNAME);

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        Session saved = captor.getValue();
        assertThat(saved.getPlayers()).containsExactlyInAnyOrder(owner, player);
    }

    @Test
    void startSession_unknownOwner_throwsAndDoesNotSave() {
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.startSession(
                Optional.empty(), Optional.empty(), "ghost"))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void startSession_unknownPlayer_throwsAndDoesNotSave() {
        User owner = user(OWNER_USERNAME);
        when(userRepository.findByUserName(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.startSession(
                Optional.empty(),
                Optional.of(Set.of(new UserRequest("ghost"))),
                OWNER_USERNAME))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void startSession_withValidGametype_attachesGameToSession() {
        User owner = user(OWNER_USERNAME);
        Gametype gametype = new Gametype();
        gametype.setGametype("501");
        when(userRepository.findByUserName(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(gametypeRepository.findByGametype("501")).thenReturn(Optional.of(gametype));

        sessionService.startSession(
                Optional.of(List.of(new GameRequest("501"))),
                Optional.empty(),
                OWNER_USERNAME);

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        Session saved = captor.getValue();
        assertThat(saved.getGames()).hasSize(1);
        assertThat(saved.getGames().get(0).getGametype()).isEqualTo(gametype);
        assertThat(saved.getGames().get(0).getSession()).isEqualTo(saved);
    }

    @Test
    void startSession_unknownGametype_throwsAndDoesNotSave() {
        User owner = user(OWNER_USERNAME);
        when(userRepository.findByUserName(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(gametypeRepository.findByGametype("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.startSession(
                Optional.of(List.of(new GameRequest("999"))),
                Optional.empty(),
                OWNER_USERNAME))
                .isInstanceOf(GameModeNotFoundException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void getSession_returnsAllSessionsForUser() {
        User owner = user(OWNER_USERNAME);
        when(userRepository.findByUserName(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(sessionRepository.findByOwner(owner)).thenReturn(
                List.of(session(1L, true, owner), session(2L, true, owner)));

        List<SessionResponse> result = sessionService.getSession(OWNER_USERNAME);

        assertThat(result).containsExactly(
                new SessionResponse(1L, OWNER_USERNAME),
                new SessionResponse(2L, OWNER_USERNAME));
    }

    @Test
    void getSession_unknownUser_throws() {
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.getSession("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(sessionRepository, never()).findByOwner(any());
    }

    @Test
    void getSession_noSessions_throws() {
        User owner = user(OWNER_USERNAME);
        when(userRepository.findByUserName(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(sessionRepository.findByOwner(owner)).thenReturn(List.of());

        assertThatThrownBy(() -> sessionService.getSession(OWNER_USERNAME))
                .isInstanceOf(NoSessionsForThisUserException.class);
    }

    @Test
    void deleteSession_ownerDeletesOwnActiveSession() {
        User owner = user(OWNER_USERNAME);
        Session ownedSession = session(7L, true, owner);
        when(sessionRepository.findById(7L)).thenReturn(Optional.of(ownedSession));
        when(userRepository.findRoleByUserName(OWNER_USERNAME)).thenReturn(Optional.of("USER"));

        sessionService.deleteSession("7", OWNER_USERNAME);

        verify(sessionRepository).delete(ownedSession);
    }

    @Test
    void deleteSession_ownerCannotDeleteOwnInactiveSession() {
        User owner = user(OWNER_USERNAME);
        when(sessionRepository.findById(7L)).thenReturn(
                Optional.of(session(7L, false, owner)));
        when(userRepository.findRoleByUserName(OWNER_USERNAME)).thenReturn(Optional.of("USER"));

        assertThatThrownBy(() -> sessionService.deleteSession("7", OWNER_USERNAME))
                .isInstanceOf(AccessDeniedException.class);

        verify(sessionRepository, never()).delete(any());
    }

    @Test
    void deleteSession_adminDeletesOthersInactiveSession() {
        User owner = user(OWNER_USERNAME);
        Session ownedSession = session(7L, false, owner);
        when(sessionRepository.findById(7L)).thenReturn(Optional.of(ownedSession));
        when(userRepository.findRoleByUserName("admin")).thenReturn(Optional.of("ADMIN"));

        sessionService.deleteSession("7", "admin");

        verify(sessionRepository).delete(ownedSession);
    }

    @Test
    void deleteSession_adminDeletesActiveSession() {
        User owner = user(OWNER_USERNAME);
        Session ownedSession = session(7L, true, owner);
        when(sessionRepository.findById(7L)).thenReturn(Optional.of(ownedSession));
        when(userRepository.findRoleByUserName("admin")).thenReturn(Optional.of("ADMIN"));

        sessionService.deleteSession("7", "admin");

        verify(sessionRepository).delete(ownedSession);
    }

    @Test
    void deleteSession_nonOwnerNonAdminCannotDelete() {
        User owner = user(PLAYER_USERNAME);
        when(sessionRepository.findById(7L)).thenReturn(
                Optional.of(session(7L, true, owner)));
        when(userRepository.findRoleByUserName(OWNER_USERNAME)).thenReturn(Optional.of("USER"));

        assertThatThrownBy(() -> sessionService.deleteSession("7", OWNER_USERNAME))
                .isInstanceOf(AccessDeniedException.class);

        verify(sessionRepository, never()).delete(any());
    }

    @Test
    void deleteSession_unknownSession_throws() {
        when(sessionRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.deleteSession("7", OWNER_USERNAME))
                .isInstanceOf(SessionNotFoundException.class);

        verify(sessionRepository, never()).delete(any());
    }
}
package lux.dartgame.service;

import lux.dartgame.dto.GameResponse;
import lux.dartgame.dto.PlayedRoundRequest;
import lux.dartgame.exception.AccessDeniedException;
import lux.dartgame.exception.GameStatNotFoundException;
import lux.dartgame.exception.InvalidScoreException;
import lux.dartgame.exception.InvalidTurnException;
import lux.dartgame.exception.NoAvailableGameException;
import lux.dartgame.exception.SessionNotFoundException;
import lux.dartgame.exception.UsernameNotFoundException;
import lux.dartgame.model.Game;
import lux.dartgame.model.GameStat;
import lux.dartgame.model.GameStatsId;
import lux.dartgame.model.Gametype;
import lux.dartgame.model.Session;
import lux.dartgame.model.User;
import lux.dartgame.repository.GameRepository;
import lux.dartgame.repository.GameStatRepository;
import lux.dartgame.repository.SessionRepository;
import lux.dartgame.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    private static final long SESSION_ID = 5L;
    private static final long GAME_ID = 10L;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameStatRepository gameStatRepository;

    @InjectMocks
    private GameService gameService;

    private User user(final long id, final String name) {
        User user = new User();
        user.setUserId(id);
        user.setUserName(name);
        return user;
    }

    private Gametype gametype(final String name) {
        Gametype gametype = new Gametype();
        gametype.setGametype(name);
        return gametype;
    }

    private Game game(final long id, final Gametype gametype, final User winner) {
        Game game = new Game();
        game.setGameId(id);
        game.setGametype(gametype);
        game.setWinner(winner);
        return game;
    }

    private GameStat stat(final User user, final Game game, final int points,
                          final int turn, final int position) {
        GameStat stat = new GameStat();
        stat.setGameStatsId(new GameStatsId(user.getUserId(), game.getGameId()));
        stat.setUser(user);
        stat.setGame(game);
        stat.setPoints(points);
        stat.setTurn(turn);
        stat.setPosition(position);
        game.getGameStats().add(stat);
        return stat;
    }

    private Session session(final long id, final User owner, final Game... games) {
        Session session = new Session();
        session.setSessionId(id);
        session.setOwner(owner);
        session.addPlayers(owner);
        for (Game game : games) {
            session.addGame(game);
        }
        return session;
    }

    private Session session(final long id, final User owner, final Set<User> players,
                            final Game... games) {
        Session session = session(id, owner, games);
        session.setPlayers(new HashSet<>(players));
        session.addPlayers(owner);
        return session;
    }

    @Test
    void startGame_returnsNextPlayerOfAvailableGame() {
        User alice = user(1L, "alice");
        User bob = user(2L, "bob");
        Game game = game(GAME_ID, gametype("301"), null);
        stat(alice, game, 100, 0, 0);
        stat(bob, game, 50, 0, 1);
        Session session = session(SESSION_ID, alice, game);

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(userRepository.findUserNameById(1L)).thenReturn(Optional.of("alice"));

        GameResponse response = gameService.startGame("301", SESSION_ID, "alice");

        assertThat(response.gametype()).isEqualTo("301");
        assertThat(response.turn()).isEqualTo("alice");
        assertThat(response.gameId()).isEqualTo(GAME_ID);
        assertThat(response.isFinished()).isFalse();
    }

    @Test
    void startGame_notMemberOfSession_throwsAccessDenied() {
        User carol = user(3L, "carol");
        User bob = user(2L, "bob");
        Game game = game(GAME_ID, gametype("301"), null);
        stat(bob, game, 50, 0, 0);
        Session session = session(SESSION_ID, carol, Set.of(bob), game);

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> gameService.startGame("301", SESSION_ID, "alice"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void startGame_noAvailableGame_thrown() {
        User alice = user(1L, "alice");
        User carol = user(3L, "carol");
        Game game = game(GAME_ID, gametype("301"), carol);
        stat(carol, game, 150, 0, 0);
        Session session = session(SESSION_ID, alice, game);

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> gameService.startGame("301", SESSION_ID, "alice"))
                .isInstanceOf(NoAvailableGameException.class);
    }

    @Test
    void startGame_gametypeDoesNotMatchAnyGame_throws() {
        User alice = user(1L, "alice");
        Game game = game(GAME_ID, gametype("501"), null);
        stat(alice, game, 100, 0, 0);
        Session session = session(SESSION_ID, alice, game);

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> gameService.startGame("301", SESSION_ID, "alice"))
                .isInstanceOf(NoAvailableGameException.class);
    }

    @Test
    void startGame_gameWithoutStats_throwsNoAvailableGame() {
        User alice = user(1L, "alice");
        Game game = game(GAME_ID, gametype("301"), null);
        Session session = session(SESSION_ID, alice, game);

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> gameService.startGame("301", SESSION_ID, "alice"))
                .isInstanceOf(NoAvailableGameException.class);
    }

    @Test
    void startGame_unknownSession_throws() {
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.startGame("301", SESSION_ID, "alice"))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    void playRound_unknownUsername_throws() {
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.playRound(
                new PlayedRoundRequest("ghost", "20 3", GAME_ID, SESSION_ID), "ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void playRound_unknownGame_throws() {
        User alice = user(1L, "alice");
        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(alice));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.playRound(
                new PlayedRoundRequest("alice", "20 3", GAME_ID, SESSION_ID), "alice"))
                .isInstanceOf(NoAvailableGameException.class);
    }

    @Test
    void playRound_unknownSession_throws() {
        User alice = user(1L, "alice");
        Game game = game(GAME_ID, gametype("301"), null);

        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(alice));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.playRound(
                new PlayedRoundRequest("alice", "20 3", GAME_ID, SESSION_ID), "alice"))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    void playRound_notMemberOfSession_throwsAccessDenied() {
        User alice = user(1L, "alice");
        User carol = user(3L, "carol");
        User bob = user(2L, "bob");
        Game game = game(GAME_ID, gametype("301"), null);
        stat(bob, game, 50, 0, 0);
        Session session = session(SESSION_ID, carol, Set.of(bob), game);

        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(alice));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> gameService.playRound(
                new PlayedRoundRequest("alice", "20 3", GAME_ID, SESSION_ID), "alice"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void playRound_gameAlreadyFinished_throws() {
        User alice = user(1L, "alice");
        User bob = user(2L, "bob");
        Game game = game(GAME_ID, gametype("301"), bob);
        Session session = session(SESSION_ID, alice, game);

        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(alice));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> gameService.playRound(
                new PlayedRoundRequest("alice", "20 3", GAME_ID, SESSION_ID), "alice"))
                .isInstanceOf(InvalidTurnException.class);
    }

    @Test
    void playRound_outOfTurn_throws() {
        User alice = user(1L, "alice");
        User bob = user(2L, "bob");
        Game game = game(GAME_ID, gametype("301"), null);
        stat(bob, game, 50, 0, 0);
        stat(alice, game, 100, 1, 1);
        Session session = session(SESSION_ID, alice, game);

        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(alice));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> gameService.playRound(
                new PlayedRoundRequest("alice", "20 3", GAME_ID, SESSION_ID), "alice"))
                .isInstanceOf(InvalidTurnException.class);
    }

    @Test
    void playRound_userWithoutStatInGame_throws() {
        User alice = user(1L, "alice");
        Game game = game(GAME_ID, gametype("301"), null);
        stat(alice, game, 100, 0, 0);
        Session session = session(SESSION_ID, alice, game);

        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(alice));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(gameStatRepository.findById(new GameStatsId(1L, GAME_ID)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.playRound(
                new PlayedRoundRequest("alice", "20 3", GAME_ID, SESSION_ID), "alice"))
                .isInstanceOf(GameStatNotFoundException.class);
    }

    @Test
    void playRound_impossibleScore_throws() {
        User alice = user(1L, "alice");
        User bob = user(2L, "bob");
        Game game = game(GAME_ID, gametype("301"), null);
        GameStat aliceStat = stat(alice, game, 100, 0, 0);
        stat(bob, game, 50, 0, 1);
        Session session = session(SESSION_ID, alice, game);

        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(alice));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(gameStatRepository.findById(new GameStatsId(1L, GAME_ID)))
                .thenReturn(Optional.of(aliceStat));

        assertThatThrownBy(() -> gameService.playRound(
                new PlayedRoundRequest("alice", "20 3 20 3 20 1 20 1 3 1", GAME_ID, SESSION_ID),
                "alice"))
                .isInstanceOf(InvalidScoreException.class);

        assertThat(aliceStat.getPoints()).isEqualTo(100);
        assertThat(aliceStat.getTurn()).isZero();
    }

    @Test
    void playRound_validScore_updatesStatAndPassesTurn() {
        User alice = user(1L, "alice");
        User bob = user(2L, "bob");
        Game game = game(GAME_ID, gametype("301"), null);
        GameStat aliceStat = stat(alice, game, 100, 0, 0);
        stat(bob, game, 50, 0, 1);
        Session session = session(SESSION_ID, alice, game);

        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(alice));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(gameStatRepository.findById(new GameStatsId(1L, GAME_ID)))
                .thenReturn(Optional.of(aliceStat));
        when(userRepository.getReferenceById(2L)).thenReturn(bob);

        GameResponse response = gameService.playRound(
                new PlayedRoundRequest("alice", "20 3", GAME_ID, SESSION_ID), "alice");

        assertThat(response.gametype()).isEqualTo("301");
        assertThat(response.turn()).isEqualTo("bob");
        assertThat(response.gameId()).isEqualTo(GAME_ID);
        assertThat(response.isFinished()).isFalse();
        assertThat(aliceStat.getPoints()).isEqualTo(160);
        assertThat(aliceStat.getTurn()).isEqualTo(1);
        assertThat(aliceStat.getHighestScore()).isEqualTo(60);
    }

    @Test
    void playRound_exactScoreWins() {
        User alice = user(1L, "alice");
        User bob = user(2L, "bob");
        Game game = game(GAME_ID, gametype("301"), null);
        GameStat aliceStat = stat(alice, game, 241, 0, 0);
        stat(bob, game, 50, 0, 1);
        Session session = session(SESSION_ID, alice, game);

        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(alice));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(gameStatRepository.findById(new GameStatsId(1L, GAME_ID)))
                .thenReturn(Optional.of(aliceStat));

        GameResponse response = gameService.playRound(
                new PlayedRoundRequest("alice", "20 3", GAME_ID, SESSION_ID), "alice");

        assertThat(response.turn()).isEqualTo("alice");
        assertThat(response.isFinished()).isTrue();
        assertThat(game.getWinner()).isEqualTo(alice);
        assertThat(aliceStat.getPoints()).isEqualTo(301);
    }

    @Test
    void playRound_bustKeepsPointsAndPassesTurn() {
        User alice = user(1L, "alice");
        User bob = user(2L, "bob");
        Game game = game(GAME_ID, gametype("301"), null);
        GameStat aliceStat = stat(alice, game, 290, 0, 0);
        stat(bob, game, 50, 0, 1);
        Session session = session(SESSION_ID, alice, game);

        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(alice));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(gameStatRepository.findById(new GameStatsId(1L, GAME_ID)))
                .thenReturn(Optional.of(aliceStat));
        when(userRepository.getReferenceById(2L)).thenReturn(bob);

        GameResponse response = gameService.playRound(
                new PlayedRoundRequest("alice", "20 3", GAME_ID, SESSION_ID), "alice");

        assertThat(response.turn()).isEqualTo("bob");
        assertThat(response.isFinished()).isFalse();
        assertThat(aliceStat.getPoints()).isEqualTo(290);
        assertThat(aliceStat.getTurn()).isEqualTo(1);
    }
}
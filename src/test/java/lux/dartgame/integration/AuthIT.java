package lux.dartgame.integration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lux.dartgame.dto.CreateSessionRequest;
import lux.dartgame.dto.GameRequest;
import lux.dartgame.dto.LoginRequest;
import lux.dartgame.dto.RegisterRequest;
import lux.dartgame.dto.SessionResponse;
import lux.dartgame.dto.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end coverage of the authentication endpoints: each test issues a real
 * HTTP request and then reads the PostgreSQL rows the request produced.
 */
class AuthIT extends AbstractIT {

    private static final String ALICE = "alice-it";
    private static final String BOB = "bob-it";

    @Test
    void register_persistsUserWithHashedPasswordAndReturnsUsableToken() {
        String token = register(ALICE);

        String storedHash = jdbc().queryForObject(
                "SELECT password FROM users WHERE user_name = ?", String.class, ALICE);
        assertThat(storedHash).isNotBlank().isNotEqualTo(PASSWORD).startsWith("$2");

        Map<String, Object> user = jdbc().queryForMap(
                "SELECT user_name, email, role_id FROM users WHERE user_name = ?", ALICE);
        assertThat(user.get("email")).isEqualTo(ALICE + EMAIL_DOMAIN);
        assertThat(user.get("role_id")).isEqualTo(1L);

        // Registration also creates the cumulative PlayerStat row.
        Integer statCount = jdbc().queryForObject(
                "SELECT COUNT(*) FROM playerstats WHERE player_id = "
                        + "(SELECT user_id FROM users WHERE user_name = ?)",
                Integer.class, ALICE);
        assertThat(statCount).isEqualTo(1);

        // The token the endpoint issued really authenticates: the JWT filter
        // only accepts it because the username resolves to this persisted row.
        ResponseEntity<String> response = rest().exchange(
                GAMEMODE_URL, HttpMethod.GET, new HttpEntity<>(bearer(token)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("301").contains("501");
    }

    @Test
    void login_afterRegister_returnsTokenThatReadsBackTheSessionsInTheDatabase() {
        String registrationToken = register(ALICE);

        // Create a session with the token from registration...
        ResponseEntity<SessionResponse> created = rest().postForEntity(
                SESSION_URL,
                authed(registrationToken, new CreateSessionRequest(
                        Set.of(), List.of(new GameRequest(TARGET_301)))),
                SessionResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // ...then reach the same session with the token from a fresh login.
        String loginToken = login(ALICE, PASSWORD);
        ResponseEntity<List> sessions = rest().exchange(
                SESSION_URL + "?username=" + ALICE, HttpMethod.GET,
                new HttpEntity<>(bearer(loginToken)), List.class);

        assertThat(sessions.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sessions.getBody()).hasSize(1);
        assertThat(sessions.getBody().get(0).toString())
                .contains("id=" + created.getBody().id());
    }

    @Test
    void login_withWrongPassword_isUnauthorizedAndStoresNothingNew() {
        register(ALICE);
        int before = countRows("users");

        ResponseEntity<TokenResponse> response = rest().postForEntity(
                AUTH_URL + "/login", new LoginRequest(ALICE, "not-the-password"),
                TokenResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).extracting(TokenResponse::token).isNull();
        assertThat(countRows("users")).isEqualTo(before);
    }

    @Test
    void register_withDuplicateUsername_isConflictAndKeepsSingleUserRow() {
        register(ALICE);

        ResponseEntity<String> response = rest().postForEntity(
                AUTH_URL + "/register",
                new RegisterRequest(ALICE, PASSWORD, "second" + EMAIL_DOMAIN),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(countRows("users")).isEqualTo(1);
        assertThat(jdbc().queryForObject(
                "SELECT email FROM users WHERE user_name = ?", String.class, ALICE))
                .isEqualTo(ALICE + EMAIL_DOMAIN);
    }

    @Test
    void register_twoUsers_persistsBothAndBothTokensAuthenticate() {
        String aliceToken = register(ALICE);
        String bobToken = register(BOB);

        assertThat(countRows("users")).isEqualTo(2);
        assertThat(countRows("playerstats")).isEqualTo(2);

        assertThat(rest().exchange(GAMEMODE_URL, HttpMethod.GET,
                new HttpEntity<>(bearer(aliceToken)), String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(rest().exchange(GAMEMODE_URL, HttpMethod.GET,
                new HttpEntity<>(bearer(bobToken)), String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void register_withInvalidEmail_isBadRequestAndPersistsNoUser() {
        ResponseEntity<String> response = rest().postForEntity(
                AUTH_URL + "/register",
                new RegisterRequest(ALICE, PASSWORD, "not-an-email"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(countRows("users")).isZero();
    }

    @Test
    void protectedEndpoint_withForgedToken_isUnauthorized() {
        register(ALICE);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("not.a.real.token");

        ResponseEntity<String> response = rest().exchange(
                GAMEMODE_URL, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNull();
    }
}

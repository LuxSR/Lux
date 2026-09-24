package lux.dartgame.controller;

import lombok.extern.slf4j.Slf4j;
import lux.dartgame.exception.AccessDeniedException;
import lux.dartgame.exception.EmailAlreadyExistsException;
import lux.dartgame.exception.GameModeNotFoundException;
import lux.dartgame.exception.GameNotFoundException;
import lux.dartgame.exception.GameStatNotFoundException;
import lux.dartgame.exception.InvalidScoreException;
import lux.dartgame.exception.InvalidTurnException;
import lux.dartgame.exception.NoAvailableGameException;
import lux.dartgame.exception.NoSessionsForThisUserException;
import lux.dartgame.exception.RoleNotFoundException;
import lux.dartgame.exception.SessionNotFoundException;
import lux.dartgame.exception.UsernameAlreadyExistsException;
import lux.dartgame.exception.UsernameNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public final class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public String handleUsernameAlreadyExists(final UsernameAlreadyExistsException e) {
        log.warn("Username already exists: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public String handleEmailAlreadyExists(final EmailAlreadyExistsException e) {
        log.warn("Email already exists: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public String handleBadCredentials(final BadCredentialsException e) {
        log.warn("Bad credentials attempt");
        return "Invalid username or password.";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationErrors(final MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());
        return e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(InvalidScoreException.class)
    public String handleInvalidScore(final InvalidScoreException e) {
        log.error("INVALID SCORE", e);
        return e.getMessage();
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RoleNotFoundException.class)
    public String handleRoleNotFound(final RoleNotFoundException e) {
        log.error("Role not found", e);
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UsernameNotFoundException.class)
    public String handleUsernameNotFound(final UsernameNotFoundException e) {
        log.warn("Username not found: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSessionsForThisUserException.class)
    public String handleSessionNotFoundForUser(final NoSessionsForThisUserException e) {
        log.warn("Session not found: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SessionNotFoundException.class)
    public String handleSessionNotFound(final SessionNotFoundException e) {
        log.warn("Session not found: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(GameModeNotFoundException.class)
    public String handleGameModeNotFound(final GameModeNotFoundException e) {
        log.warn("Gametype not found: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(GameStatNotFoundException.class)
    public String handleGameStatNotFound(final GameStatNotFoundException e) {
        log.warn("Gametype not found: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(GameNotFoundException.class)
    public String handleGameNotFound(final GameNotFoundException e) {
        log.warn("Game not found: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoAvailableGameException.class)
    public String handleGameNotAvailable(final NoAvailableGameException e) {
        log.warn("Game not available in this session: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(final AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(InvalidTurnException.class)
    public String handleInvalidTurn(final InvalidTurnException e) {
        log.warn("Invalid turn: {}", e.getMessage());
        return e.getMessage();
    }
}

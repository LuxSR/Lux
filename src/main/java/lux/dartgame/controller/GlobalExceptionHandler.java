package lux.dartgame.controller;

import lombok.extern.slf4j.Slf4j;
import lux.dartgame.exception.RoleNotFoundException;
import lux.dartgame.exception.UsernameAlreadyExistsException;
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

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RoleNotFoundException.class)
    public String handleRoleNotFound(final RoleNotFoundException e) {
        log.error("Role not found", e);
        return e.getMessage();
    }
}

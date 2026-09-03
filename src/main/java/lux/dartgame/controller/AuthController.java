package lux.dartgame.controller;

import jakarta.validation.Valid;
import lux.dartgame.dto.LoginRequest;
import lux.dartgame.dto.RegisterRequest;
import lux.dartgame.dto.TokenResponse;
import lux.dartgame.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public final class AuthController {

    private final AuthService authService;
    public AuthController(final AuthService authServiceParam) {
        this.authService = authServiceParam;
    }

    @PostMapping("/login")
    public TokenResponse login(final @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public TokenResponse register(final @RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}

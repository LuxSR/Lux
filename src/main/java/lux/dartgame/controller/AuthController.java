package lux.dartgame.controller;

import jakarta.validation.Valid;
import lux.dartgame.dto.LoginRequest;
import lux.dartgame.dto.RegisterRequest;
import lux.dartgame.dto.TokenResponse;
import lux.dartgame.service.AuthService;
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
    public TokenResponse login(final @RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public TokenResponse register(final @RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }
}

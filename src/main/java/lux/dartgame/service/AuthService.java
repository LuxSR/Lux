package lux.dartgame.service;

import lux.dartgame.config.JwtProperties;
import lux.dartgame.dto.LoginRequest;
import lux.dartgame.dto.RegisterRequest;
import lux.dartgame.dto.TokenResponse;
import lux.dartgame.exception.RoleNotFoundException;
import lux.dartgame.exception.UsernameAlreadyExistsException;
import lux.dartgame.model.Role;
import lux.dartgame.model.User;
import lux.dartgame.repository.RoleRepository;
import lux.dartgame.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class AuthService {
    private static final int MINUTE_LENGTH = 60;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(final AuthenticationManager authenticationManagerParam,
                       final JwtService jwtServiceParam,
                       final JwtProperties jwtPropertiesParam,
                       final UserRepository userRepositoryParam,
                       final RoleRepository roleRepositoryParam,
                       final PasswordEncoder passwordEncoderParam) {
        this.authenticationManager = authenticationManagerParam;
        this.jwtService = jwtServiceParam;
        this.jwtProperties = jwtPropertiesParam;
        this.userRepository = userRepositoryParam;
        this.roleRepository = roleRepositoryParam;
        this.passwordEncoder = passwordEncoderParam;
    }

    public TokenResponse login(final LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        var user = (UserDetails) authentication.getPrincipal();
        return TokenResponse.bearer(jwtService.generateToken(user),
                jwtProperties.expirationMinutes() * MINUTE_LENGTH);
    }


    public TokenResponse register(final RegisterRequest request) {
        if (userRepository.existsByUserName(request.username())) {
            throw new UsernameAlreadyExistsException();
        }

        User user = new User();
        user.setUserName(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        Role userRole = roleRepository.findByRole("USER")
                .orElseThrow(RoleNotFoundException::new);
        user.setRole(userRole);
        userRepository.save(user);

        return TokenResponse.bearer(
                jwtService.generateToken(asUserDetails(user)),
                jwtProperties.expirationMinutes() * MINUTE_LENGTH
        );
    }

    private UserDetails asUserDetails(final User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserName())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().getRole())))
                .build();
    }
}

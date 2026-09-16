package lux.dartgame.service;

import lombok.extern.slf4j.Slf4j;
import lux.dartgame.config.JwtProperties;
import lux.dartgame.constants.Constants;
import lux.dartgame.dto.LoginRequest;
import lux.dartgame.dto.RegisterRequest;
import lux.dartgame.dto.TokenResponse;
import lux.dartgame.exception.EmailAlreadyExistsException;
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

@Slf4j
@Service
public final class AuthService {
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

    // REVIEW(good): delegating to AuthenticationManager rather than comparing hashes by hand. This is the idiomatic version and it picks up the account-status checks for free.
    public TokenResponse login(final LoginRequest request) {
        log.info("Login attempt for user: {}", request.username());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        var user = (UserDetails) authentication.getPrincipal();
        log.info("Login successful for user: {}", request.username());
        return TokenResponse.bearer(jwtService.generateToken(user),
                jwtProperties.expirationMinutes() * Constants.SECONDS_PER_MINUTE);
    }


    // REVIEW(noob): register does two existence checks and then a save, with no @Transactional and no unique-constraint fallback. Two requests racing with the same username both pass the check and one dies on the database constraint with a 500. The schema does have the UNIQUE, so catch DataIntegrityViolationException and turn it into the same 409.
    public TokenResponse register(final RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.username());
        if (userRepository.existsByUserName(request.username())) {
            throw new UsernameAlreadyExistsException();
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setUserName(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        Role userRole = roleRepository.findByRole("USER")
                .orElseThrow(RoleNotFoundException::new);
        user.setRole(userRole);
        userRepository.save(user);
        log.info("Registration successful for user: {}", request.username());

        return TokenResponse.bearer(
                jwtService.generateToken(asUserDetails(user)),
                jwtProperties.expirationMinutes() * Constants.SECONDS_PER_MINUTE
        );
    }

    // REVIEW(noob): this builds a second, parallel UserDetails from the entity while AppUserDetailsService already knows how to do exactly that. Two ways to build the same object drift apart; call the UserDetailsService, or extract one mapper both use.
    private UserDetails asUserDetails(final User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserName())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().getRole())))
                .build();
    }
}

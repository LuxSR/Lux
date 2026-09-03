package lux.dartgame.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    /**
     * Provides the {@link PasswordEncoder} used for password hashing.
     * <p>
     * Overriding this method is supported, but implementers must return a
     * fully-configured {@link PasswordEncoder} that satisfies the application's
     * password security requirements (e.g. {@link BCryptPasswordEncoder} or
     * another closed-loop encoder that can verify previously stored hashes).
     * If you only need the default behaviour, do not override this method.
     *
     * @return the password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

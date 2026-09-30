package lux.dartgame.config;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

public final class RequiredEnvironmentValidator
        implements EnvironmentPostProcessor, Ordered {

    private static final String RULE = "=".repeat(60);

    private static final List<RequiredVariable> REQUIRED = List.of(
            new RequiredVariable("POSTGRES_DB", "PostgreSQL database name"),
            new RequiredVariable("POSTGRES_USER", "PostgreSQL user and backend username"),
            new RequiredVariable("POSTGRES_PASSWORD", "Password for the PostgreSQL user"),
            new RequiredVariable("JWT_SECRET", "Secret used to sign and validate JSON Web Tokens"));

    private record RequiredVariable(String name, String purpose) {
    }

    @Override
    public void postProcessEnvironment(
            final ConfigurableEnvironment environment, final SpringApplication application) {
        final List<RequiredVariable> missing = REQUIRED.stream()
                .filter(variable -> isMissing(environment.getProperty(variable.name())))
                .toList();

        if (!missing.isEmpty()) {
            throw new IllegalStateException(buildMessage(missing));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static boolean isMissing(final String value) {
        return value == null || value.isBlank();
    }

    private static String buildMessage(final List<RequiredVariable> missing) {
        final String eol = System.lineSeparator();
        final String verb = missing.size() == 1 ? " variable is" : " variables are";
        final StringBuilder message = new StringBuilder(eol)
                .append(RULE).append(eol)
                .append(" Lux failed to start: ").append(missing.size())
                .append(" required environment").append(verb)
                .append(" missing or blank").append(eol)
                .append(RULE).append(eol);

        for (final RequiredVariable variable : missing) {
            message.append("  ")
                    .append(String.format("%-20s", variable.name()))
                    .append(' ').append(variable.purpose())
                    .append(eol);
        }

        message.append(eol)
                .append(" Set them in the .env file in the repository root (see README.md):")
                .append(eol)
                .append(eol);

        for (final RequiredVariable variable : REQUIRED) {
            message.append("   ").append(variable.name()).append('=').append(eol);
        }

        return message.append(RULE).append(eol).toString();
    }
}

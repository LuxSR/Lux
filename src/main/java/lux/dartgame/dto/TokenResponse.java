package lux.dartgame.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenResponse(@NotBlank String token,
                            @NotBlank String type,
                            @NotBlank long expiresInSeconds) {

    public static TokenResponse bearer(final @NotBlank String token,
                                       final @NotBlank long expiresInSeconds) {
        return new TokenResponse(token, "Bearer", expiresInSeconds);
    }
}

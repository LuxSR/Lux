package lux.dartgame.dto;

public record TokenResponse(String token,
                            String type,
                            long expiresInSeconds) {

    public static TokenResponse bearer(final String token,
                                       final long expiresInSeconds) {
        return new TokenResponse(token, "Bearer", expiresInSeconds);
    }
}

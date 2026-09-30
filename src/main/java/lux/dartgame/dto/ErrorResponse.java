package lux.dartgame.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Uniform error payload returned by the global exception handler.
 * Every failing endpoint responds with this shape, so clients can rely on
 * {@code status} and {@code message} regardless of what went wrong.
 *
 * @param timestamp moment the error response was produced
 * @param status    HTTP status code of the response
 * @param error     HTTP reason phrase matching the status
 * @param message   client-safe description, never a stack trace
 */
public record ErrorResponse(LocalDateTime timestamp,
                            int status,
                            String error,
                            String message) {

    /**
     * Builds an error response for the given status and client-safe message.
     *
     * @param status  the HTTP status to report
     * @param message the client-safe description
     * @return a populated {@link ErrorResponse}
     */
    public static ErrorResponse of(final HttpStatus status, final String message) {
        return new ErrorResponse(LocalDateTime.now(),
                                 status.value(),
                                 status.getReasonPhrase(),
                                 message);
    }
}

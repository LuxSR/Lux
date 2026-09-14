package lux.dartgame.exception;

public class NoSessionsForThisUserException extends RuntimeException {
    public NoSessionsForThisUserException(final String owner) {
        super("No sessions found with owner " + owner);
    }
}

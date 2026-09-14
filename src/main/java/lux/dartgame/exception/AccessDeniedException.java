package lux.dartgame.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException() {
        super("Access denied for this operation");
    }
}

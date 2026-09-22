package lux.dartgame.exception;

public class NoAvailableGameException extends RuntimeException {
    public NoAvailableGameException() {
        super("Game not available in this session");
    }
}

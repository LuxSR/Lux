package lux.dartgame.exception;

public class InvalidTurnException extends RuntimeException {
    public InvalidTurnException() {
        super("Invalid Turn");
    }
}

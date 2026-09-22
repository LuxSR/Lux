package lux.dartgame.exception;

public class InvalidScoreException extends IllegalArgumentException {
    public InvalidScoreException() {
        super("Cannot score more than 180!");
    }
}

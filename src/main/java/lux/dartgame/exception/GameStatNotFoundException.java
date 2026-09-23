package lux.dartgame.exception;

public class GameStatNotFoundException extends RuntimeException {
    public GameStatNotFoundException() {
        super("GameStat not found");
    }
}

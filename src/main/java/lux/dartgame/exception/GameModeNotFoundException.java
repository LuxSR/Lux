package lux.dartgame.exception;

public class GameModeNotFoundException extends RuntimeException {
    public GameModeNotFoundException(final String gametype) {
        super("Could not find gametype:" + gametype);
    }
}

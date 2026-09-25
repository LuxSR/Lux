package lux.dartgame.exception;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(final long gameid) {
        super("Could not find game:" + gameid);
    }
}

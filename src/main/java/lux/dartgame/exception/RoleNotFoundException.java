package lux.dartgame.exception;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException() {
        super("Required role not found in database.");
    }
}

package exceptions;

public class UsernameAlreadyTakenException extends Exception {
    public UsernameAlreadyTakenException() {
        super("Username already taken.");
    }
}

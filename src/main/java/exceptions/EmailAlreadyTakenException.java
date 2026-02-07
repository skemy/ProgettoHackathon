package exceptions;

public class EmailAlreadyTakenException extends Exception {
    public EmailAlreadyTakenException() {
        super("Email already taken.");
    }
}

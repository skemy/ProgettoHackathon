package exceptions;

public class InvalidTimeWindowException extends Exception {
    public InvalidTimeWindowException() {
        super("Chosen time window is invalid.");
    }
}

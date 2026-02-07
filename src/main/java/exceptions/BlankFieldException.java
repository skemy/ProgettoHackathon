package exceptions;

public class BlankFieldException extends Exception {
    public BlankFieldException() {
        super("One or more required fields are blank.");
    }
}

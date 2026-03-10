package exceptions;

public class BlankFieldException extends Exception {
    public BlankFieldException(String s) {
        super("One or more required fields are blank.");
    }
}

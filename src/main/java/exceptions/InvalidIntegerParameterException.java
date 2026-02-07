package exceptions;

public class InvalidIntegerParameterException extends Exception {
    public InvalidIntegerParameterException() {
        super("The value for max number of participants and max team size should be higher then 1.");
    }
}

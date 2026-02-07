package exceptions;

public class CannotRegisterToEventException extends Exception {
    public CannotRegisterToEventException() {
        super("Cannot register to the event as it is currently closed.");
    }
}

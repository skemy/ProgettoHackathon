package exceptions;

/**
 * Eccezione lanciata quando un utente tenta di registrarsi a un evento che è attualmente chiuso.
 */
public class CannotRegisterToEventException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio predefinito.
     */
    public CannotRegisterToEventException() {
        super("Cannot register to the event as it is currently closed.");
    }
}

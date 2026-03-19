package exceptions;

/**
 * Eccezione lanciata quando le password inserite non corrispondono.
 */
public class PasswordsDoNotMatchException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio predefinito.
     */
    public PasswordsDoNotMatchException() {
        super("Passwords do not match.");
    }
}

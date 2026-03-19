package exceptions;

/**
 * Eccezione lanciata quando la finestra temporale scelta per un evento è invalida.
 */
public class InvalidTimeWindowException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio predefinito.
     */
    public InvalidTimeWindowException() {
        super("Chosen time window is invalid.");
    }
}

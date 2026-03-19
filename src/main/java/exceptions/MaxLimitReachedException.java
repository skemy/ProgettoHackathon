package exceptions;

/**
 * Eccezione lanciata quando viene raggiunto il limite massimo consentito per una risorsa o un'operazione.
 */
public class MaxLimitReachedException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio predefinito.
     */
    public MaxLimitReachedException() {
        super("Max limit reached.");
    }
}

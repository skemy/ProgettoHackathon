package exceptions;

/**
 * Eccezione lanciata quando un'email è già registrata nel sistema.
 */
public class EmailAlreadyTakenException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio personalizzato.
     * @param message Il messaggio di errore da visualizzare.
     */
    public EmailAlreadyTakenException(String message) {
        super(message);
    }
}
package exceptions;

/**
 * Eccezione lanciata quando un utente non viene trovato nel sistema.
 */
public class UserNotFoundException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio personalizzato.
     * @param message Il messaggio di errore da visualizzare.
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}

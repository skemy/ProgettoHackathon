package exceptions;

/**
 * Eccezione lanciata quando un nome utente è già registrato nel sistema.
 */
public class UsernameAlreadyTakenException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio personalizzato.
     * @param message Il messaggio di errore da visualizzare.
     */
    public UsernameAlreadyTakenException(String message) {
        super(message);
    }
}
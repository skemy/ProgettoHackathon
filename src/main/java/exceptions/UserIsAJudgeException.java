package exceptions;

/**
 * Eccezione lanciata quando un'operazione non è consentita perché il ruolo dell'utente è impostato a Giudice.
 */
public class UserIsAJudgeException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio predefinito.
     */
    public UserIsAJudgeException() {
        super("User's role is currently set to Judge.");
    }
}

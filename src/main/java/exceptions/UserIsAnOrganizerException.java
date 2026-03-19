package exceptions;

/**
 * Eccezione lanciata quando un'operazione non è consentita perché il ruolo dell'utente è impostato a Organizzatore.
 */
public class UserIsAnOrganizerException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio predefinito.
     */
    public UserIsAnOrganizerException() {
        super("User's role is currently set to Organizer.");
    }
}

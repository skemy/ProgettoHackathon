package exceptions;

/**
 * Eccezione lanciata quando un'operazione non è consentita perché il ruolo dell'utente è impostato a Partecipante.
 */
public class UserIsAParticipantException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio predefinito.
     */
    public UserIsAParticipantException() {
        super("User's role is currently set to Participant.'");
    }
}

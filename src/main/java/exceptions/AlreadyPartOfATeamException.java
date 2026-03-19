package exceptions;

/**
 * Eccezione lanciata quando un utente tenta di unirsi a un team ma è già membro di un altro team.
 */
public class AlreadyPartOfATeamException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio predefinito.
     */
    public AlreadyPartOfATeamException() {
        super("User is already part of a team.");
    }
}

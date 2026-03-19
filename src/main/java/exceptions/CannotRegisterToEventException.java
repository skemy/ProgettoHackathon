package exceptions;

/**
 * Eccezione lanciata quando un utente tenta di registrarsi a un evento,
 * ma l'operazione viene negata per motivi di stato (chiuso) o permessi (ruoli esistenti).
 */
public class CannotRegisterToEventException extends Exception {

    /**
     * Costruttore di default che inizializza l'eccezione con un messaggio predefinito.
     */
    public CannotRegisterToEventException() {
        super("Cannot register to the event as it is currently closed.");
    }

    /**
     * Costruttore parametrico che permette di specificare la causa esatta del blocco.
     * È la "porta d'ingresso" che risolve l'errore "Expected no arguments but found 1".
     * * @param message Il messaggio specifico che verrà poi catturato e mostrato dalla GUI.
     */
    public CannotRegisterToEventException(String message) {
        super(message); // Passa il nostro messaggio personalizzato alla superclasse Exception
    }
}
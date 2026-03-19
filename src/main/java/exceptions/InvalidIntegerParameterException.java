package exceptions;

/**
 * Eccezione lanciata quando i valori per il numero massimo di partecipanti o la dimensione massima del team sono inferiori a 1.
 */
public class InvalidIntegerParameterException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio predefinito.
     */
    public InvalidIntegerParameterException() {
        super("The value for max number of participants and max team size should be higher then 1.");
    }
}

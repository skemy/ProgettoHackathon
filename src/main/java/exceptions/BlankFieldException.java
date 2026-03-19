package exceptions;

/**
 * Eccezione lanciata quando uno o più campi obbligatori sono vuoti o nulli.
 */
public class BlankFieldException extends Exception {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio predefinito.
     * @param s Parametro non utilizzato, ma richiesto per compatibilità.
     */
    public BlankFieldException(String s) {
        super("One or more required fields are blank.");
    }
}

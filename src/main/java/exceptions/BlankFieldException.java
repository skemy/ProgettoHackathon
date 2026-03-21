package exceptions;

/**
 * Eccezione lanciata quando uno o più campi obbligatori sono vuoti o nulli.
 * Estende RuntimeException per evitare l'obbligo di dichiarare 'throws' in ogni metodo (Clean Code).
 */
public class BlankFieldException extends RuntimeException {
    /**
     * Costruttore che inizializza l'eccezione con un messaggio specifico.
     * @param message Il messaggio di errore dettagliato da visualizzare nel popup.
     */
    public BlankFieldException(String message) {
        super(message);
    }
}
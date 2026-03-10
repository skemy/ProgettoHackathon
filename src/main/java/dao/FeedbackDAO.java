package dao;

import model.Feedback;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei Feedback (commenti testuali).
 * Definisce i contratti per il salvataggio, il recupero e la validazione
 * dei commenti lasciati dai giudici sui documenti dei team.
 */
public interface FeedbackDAO {

    /**
     * Salva in modo permanente il feedback testuale di un giudice relativo a un documento.
     * * @param feedback L'oggetto Feedback contenente il testo, il judgeId e il documentId.
     */
    void saveFeedback(Feedback feedback);

    /**
     * Recupera l'elenco di tutti i feedback associati a uno specifico documento.
     * Utilizzato dalla dashboard del Team per visualizzare le correzioni e i commenti della giuria.
     * * @param documentId L'identificativo univoco del documento (progetto caricato).
     * @return Una lista di oggetti Feedback appartenenti a quel documento.
     */
    List<Feedback> getFeedbackByDocument(int documentId);

    /**
     * Verifica se un giudice ha già lasciato un feedback per uno specifico documento.
     * Agisce da controllo preventivo (Guardia) per la GUI, disabilitando l'opzione di
     * commento multiplo e prevenendo violazioni del vincolo UNIQUE sul database.
     * * @param judgeId L'ID del giudice.
     * @param documentId L'ID del documento valutato.
     * @return true se il giudice ha già commentato questo documento, false altrimenti.
     */
    boolean hasJudgeAlreadyCommented(int judgeId, int documentId);
}
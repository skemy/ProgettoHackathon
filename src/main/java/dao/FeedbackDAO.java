package dao;

import exceptions.BlankFieldException;
import model.Feedback;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia per la gestione dei feedback nel database.
 * Fornisce metodi per salvare, aggiornare, recuperare e ottenere feedback associati a giudici e documenti.
 */
public interface FeedbackDAO {
    /**
     * Salva o aggiorna un feedback per un giudice e un documento specifico.
     * @param judgeId L'ID del giudice.
     * @param documentId L'ID del documento.
     * @param text Il testo del feedback.
     * @return true se l'operazione è riuscita, false altrimenti.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     * @throws BlankFieldException Se il testo del feedback è vuoto o nullo.
     */
    boolean saveOrUpdateFeedback(int judgeId, int documentId, String text) throws SQLException, BlankFieldException;

    /**
     * Recupera il testo del feedback per un giudice e un documento specifico.
     * @param judgeId L'ID del giudice.
     * @param documentId L'ID del documento.
     * @return Il testo del feedback, o una stringa vuota se non trovato.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    String getFeedbackText(int judgeId, int documentId) throws SQLException;

    /**
     * Recupera tutti i feedback per un documento specifico.
     * @param documentId L'ID del documento.
     * @return Una lista di oggetti Feedback.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    List<Feedback> getAllFeedbacksForDocument(int documentId) throws SQLException;
}
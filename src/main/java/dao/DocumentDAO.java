package dao;

import model.Document;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei documenti (Progetti caricati dai Team).
 * Gestisce l'upload, il recupero e la consultazione dei materiali di gara.
 */
public interface DocumentDAO {

    /**
     * Carica (salva) le informazioni di un documento nel database.
     * Corrisponde all'azione "Upload Document" del Partecipante.
     *
     * @param document L'oggetto Document da salvare.
     */
    void uploadDocument(Document document);

    /**
     * Recupera tutti i documenti caricati da uno specifico team.
     * Utile per la dashboard del team e per i giudici.
     *
     * @param teamId L'ID del team.
     * @return Lista di documenti (solitamente 1 per hackathon, ma la lista è flessibile).
     */
    List<Document> getDocumentsByTeam(int teamId);

    /**
     * Recupera un documento specifico tramite ID.
     * Utile quando un Giudice clicca su "Vedi Dettaglio".
     *
     * @param documentId L'ID del documento.
     * @return L'oggetto Document trovato.
     */
    Document getDocumentById(int documentId);

    /**
     * Elimina un documento (es. caricato per errore).
     * @param documentId L'ID del documento da rimuovere.
     */
    void deleteDocument(int documentId);
}
package dao;

import model.Document;
import java.util.List;

/**
 * Interfaccia DAO per la gestione della persistenza dei documenti.
 * Definisce i contratti per l'upload, il recupero e l'eliminazione dei progetti caricati dai Team.
 * <p>
 * Nota Architetturale: Isola il layer Control dai dettagli di implementazione del database,
 * rispettando il principio di Inversione delle Dipendenze (DIP) prescritto dai principi SOLID.
 */
public interface DocumentDAO {

    /**
     * Salva le informazioni di un nuovo documento nel database.
     * Corrisponde all'azione di upload effettuata da un Partecipante.
     *
     * @param document L'entità Document da persistere.
     */
    void uploadDocument(Document document);

    /**
     * Recupera tutti i documenti caricati da uno specifico team.
     *
     * @param teamId L'identificativo univoco del team.
     * @return Una lista di entità Document. Restituisce una lista vuota se non sono presenti documenti.
     */
    List<Document> getDocumentsByTeam(int teamId);

    /**
     * Recupera un documento specifico tramite il suo identificativo univoco.
     *
     * @param documentId L'identificativo univoco del documento da cercare.
     * @return L'entità Document mappata, oppure null se il record non esiste.
     */
    Document getDocumentById(int documentId);

    /**
     * Elimina un documento in modo permanente dal sistema.
     *
     * @param documentId L'identificativo univoco del documento da rimuovere.
     */
    void deleteDocument(int documentId);
}
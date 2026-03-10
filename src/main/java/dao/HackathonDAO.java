package dao;

import model.Hackathon;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dell'entità Hackathon.
 * Definisce le operazioni CRUD (Create, Read, Update, Delete)
 * e le query specifiche per gli eventi.
 */
public interface HackathonDAO {

    /**
     * Salva un nuovo hackathon nel database.
     * Utilizzato dall'Organizer per creare l'evento.
     *
     * @param hackathon L'oggetto Hackathon da salvare.
     */
    public abstract void createHackathon(Hackathon hackathon);

    /**
     * Recupera un hackathon specifico tramite il suo ID.
     * Utile per visualizzare i dettagli dell'evento.
     *
     * @param id L'identificativo univoco dell'hackathon.
     * @return L'oggetto Hackathon trovato, oppure null se non esiste.
     */
    public abstract Hackathon getHackathonById(int id);

    /**
     * Recupera la lista di tutti gli hackathon presenti nel sistema.
     * Utile per la schermata di selezione evento o dashboard.
     *
     * @return Una lista di oggetti Hackathon.
     */
    public abstract List<Hackathon> getAllHackathons();

    /**
     * Aggiorna la descrizione del problema di un hackathon.
     * Corrisponde alla funzionalità "publishProblem" dell'Organizer/Judge.
     *
     * @param hackathonId L'ID dell'evento.
     * @param description Il testo della nuova traccia/problema.
     */
    public abstract void updateProblemDescription(int hackathonId, String description);
}
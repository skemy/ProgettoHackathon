package dao;

import model.Hackathon;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dell'entità Hackathon.
 * Definisce le operazioni CRUD e le query specifiche necessarie per la gestione degli eventi.
 * <p>
 * Nota Architetturale: Questa interfaccia permette al Controller di interagire con i dati
 * degli Hackathon senza conoscere i dettagli implementativi di PostgreSQL, rispettando
 * il principio di Dependency Inversion.
 */
public interface HackathonDAO {

    /**
     * Salva un nuovo hackathon nel database.
     * Utilizzato dal layer Control per formalizzare la creazione di un evento.
     *
     * @param hackathon L'oggetto Hackathon da persistere.
     */
    void createHackathon(Hackathon hackathon);

    /**
     * Recupera un hackathon specifico tramite il suo identificativo univoco.
     *
     * @param id L'identificativo univoco dell'hackathon.
     * @return L'oggetto Hackathon trovato, oppure null se non esiste alcun record.
     */
    Hackathon getHackathonById(int id);

    /**
     * Recupera la lista di tutti gli hackathon presenti nel sistema.
     *
     * @return Una lista di oggetti Hackathon.
     */
    List<Hackathon> getAllHackathons();

    /**
     * Aggiorna la descrizione del problema associata a un hackathon.
     *
     * @param hackathonId L'identificativo univoco dell'evento.
     * @param description Il nuovo testo della traccia o del problema.
     */
    void updateProblemDescription(int hackathonId, String description);

    /**
     * Verifica se un utente specifico riveste il ruolo di organizzatore per un hackathon
     * e ne restituisce l'identificativo associato.
     * <p>
     * Nota Architetturale: Supporta il layer Control nella risoluzione dinamica del ruolo
     * dell'utente (RBAC) durante l'autenticazione.
     *
     * @param userId L'identificativo univoco dell'utente da verificare.
     * @return L'ID dell'hackathon organizzato dall'utente, o -1 se non ne organizza alcuno.
     */
    int getHackathonIdWhereUserIsOrganizer(int userId);

    /**
     * Recupera il nome utente dell'organizzatore associato a uno specifico hackathon.
     * <p>
     * Nota Architetturale: Sfrutta le capacità relazionali (JOIN) per restituire
     * direttamente l'informazione alla Boundary, evitando query multiple.
     *
     * @param hackathonId L'identificativo univoco dell'evento.
     * @return Il nome dell'organizzatore o "Unknown Organizer" come fallback.
     */
    String getOrganizerUsernameByHackathonId(int hackathonId);
}
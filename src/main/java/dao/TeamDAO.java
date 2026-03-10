package dao;

import model.Team;
import model.Participant;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei team e delle iscrizioni dei partecipanti.
 * Gestisce la creazione di gruppi e le logiche di accesso tramite codice univoco.
 */
public interface TeamDAO {

    /**
     * Crea un nuovo team nel database e ne registra i dati iniziali.
     * * @param team L'oggetto Team da salvare.
     * @return true se la creazione ha successo, false altrimenti.
     */
    public abstract boolean createTeam(Team team);

    /**
     * Permette a un partecipante di unirsi a un team esistente verificando il codice di accesso.
     * Questa operazione solitamente aggiorna il riferimento del team nell'entità Participant.
     * * @param participant Il partecipante che richiede l'adesione.
     * @param accessCode Il codice segreto alfanumerico del team.
     * @return true se il codice è corretto e l'inserimento ha successo, false altrimenti.
     */
    public abstract boolean joinTeam(Participant participant, String accessCode);

    /**
     * Recupera i dati di un team specifico tramite il suo identificativo univoco.
     * * @param teamId L'ID del team da ricercare.
     * @return L'oggetto Team trovato, oppure null se non esistente.
     */
    public abstract Team getTeamById(int teamId);

    /**
     * Recupera l'elenco di tutti i team che partecipano a un hackathon specifico.
     * * @param hackathonId L'ID dell'evento di riferimento.
     * @return Una lista di oggetti Team associati all'evento.
     */
    public abstract List<Team> getTeamsByHackathon(int hackathonId);

    /**
     * Recupera la lista dei partecipanti che compongono un team specifico.
     * * @param teamId L'ID del team di cui si vogliono conoscere i membri.
     * @return Una lista di oggetti Participant appartenenti al team.
     */
    public abstract List<Participant> getTeamMembers(int teamId);
}
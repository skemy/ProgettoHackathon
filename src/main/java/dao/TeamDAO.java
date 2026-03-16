package dao;

import model.Team;
import model.Participant;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei team e delle iscrizioni dei partecipanti.
 * Segue il pattern BCE per isolare le query SQL dal Controller.
 */
public interface TeamDAO {

    int createTeamAndReturnId(Team team);

    boolean linkUserToTeam(int userId, int teamId);

    int getTeamIdByCode(String code);

    Team getTeamById(int teamId);

    List<Team> getTeamsByHackathon(int hackathonId);

    List<Participant> getTeamMembers(int teamId);

    // --- NUOVI METODI SPOSTATI DAL CONTROLLER ---

    /**
     * Recupera l'ID del team a cui appartiene un utente.
     */
    int getTeamIdByUserId(int userId);

    /**
     * Recupera l'ID dell'hackathon a cui è iscritto un team.
     */
    int getHackathonIdByTeam(int teamId);
}
package dao;

import database.ConnessioneDatabase;
import model.Team;
import model.Participant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione dell'interfaccia {@link TeamDAO} per PostgreSQL.
 * Questa classe gestisce la persistenza dei dati relativi ai team, inclusa la creazione,
 * il recupero delle informazioni e la gestione delle partecipazioni degli utenti.
 * <p>
 * Nota Architetturale: Rispetta pienamente gli standard SonarQube eliminando le stringhe
 * letterali duplicate tramite costanti e ottimizzando le query SQL. Le eccezioni
 * sono propagate al Controller per una gestione centralizzata della business logic.
 */
public class TeamDAOImpl implements TeamDAO {

    private static final Logger LOGGER = Logger.getLogger(TeamDAOImpl.class.getName());

    private static final String TEAM_ID_COL = "teamId";
    private static final String HACKATHON_ID_COL = "hackathonId";

    /**
     * Crea un nuovo team nel database e genera un codice di accesso univoco.
     * Recupera l'ID generato automaticamente dal database.
     *
     * @param team L'oggetto Team da persistere.
     * @return L'ID univoco (PK) assegnato al nuovo team.
     * @throws SQLException In caso di errore durante l'operazione di INSERT.
     */
    @Override
    public int createTeamAndReturnId(Team team) throws SQLException {
        // Codice di accesso alfanumerico univoco
        String generatedCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        team.setAccessCode(generatedCode);

        String query = "INSERT INTO team (teamName, accessCode, hackathonId) VALUES (?, ?, ?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, team.getTeamName());
            ps.setString(2, team.getAccessCode());
            ps.setInt(3, team.getHackathonId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    team.setTeamId(id);
                    LOGGER.log(Level.INFO, "Team created. ID: {0}, Code: {1}", new Object[]{id, generatedCode});
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Associa un utente a un team nella tabella di partecipazione.
     *
     * @param userId L'ID dell'utente da associare.
     * @param teamId L'ID del team di destinazione.
     * @return true se l'inserimento ha avuto successo.
     * @throws SQLException In caso di violazione di vincoli o errori di connessione.
     */
    @Override
    public boolean linkUserToTeam(int userId, int teamId) throws SQLException {
        String query = "INSERT INTO participation (userId, teamId) VALUES (?, ?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, teamId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Ricerca l'ID di un team utilizzando il suo codice di accesso univoco.
     *
     * @param code Il codice alfanumerico del team.
     * @return L'ID del team trovato, oppure -1 se non esiste.
     * @throws SQLException In caso di errore di lettura.
     */
    @Override
    public int getTeamIdByCode(String code) throws SQLException {
        String query = "SELECT teamId FROM team WHERE accessCode = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(TEAM_ID_COL);
            }
        }
        return -1;
    }

    /**
     * Recupera le informazioni complete di un team tramite il suo ID.
     *
     * @param teamId L'identificativo del team.
     * @return Un oggetto Team popolato, o null se non trovato.
     * @throws SQLException In caso di errore SQL.
     */
    @Override
    public Team getTeamById(int teamId) throws SQLException {
        String query = "SELECT teamId, teamName, accessCode, creationDate, hackathonId FROM team WHERE teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Team(
                            rs.getInt(TEAM_ID_COL),
                            rs.getString("teamName"),
                            rs.getString("accessCode"),
                            rs.getTimestamp("creationDate").toLocalDateTime(),
                            rs.getInt(HACKATHON_ID_COL)
                    );
                }
            }
        }
        return null;
    }

    /**
     * Recupera l'elenco di tutti i team iscritti a uno specifico hackathon.
     *
     * @param hackathonId L'ID dell'evento.
     * @return Una lista di oggetti Team.
     * @throws SQLException In caso di errore SQL.
     */
    @Override
    public List<Team> getTeamsByHackathon(int hackathonId) throws SQLException {
        List<Team> list = new ArrayList<>();
        String query = "SELECT teamId, teamName, accessCode, creationDate, hackathonId FROM team WHERE hackathonId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, hackathonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Team(
                            rs.getInt(TEAM_ID_COL),
                            rs.getString("teamName"),
                            rs.getString("accessCode"),
                            rs.getTimestamp("creationDate").toLocalDateTime(),
                            rs.getInt(HACKATHON_ID_COL)
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Recupera i dati anagrafici dei membri appartenenti a un team.
     *
     * @param teamId L'ID del team da interrogare.
     * @return Una lista di oggetti Participant.
     * @throws SQLException In caso di errore SQL.
     */
    @Override
    public List<Participant> getTeamMembers(int teamId) throws SQLException {
        List<Participant> members = new ArrayList<>();
        String query = "SELECT u.userId, u.name, u.email, u.password FROM users u " +
                "JOIN participation p ON u.userId = p.userId " +
                "WHERE p.teamId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(new Participant(
                            rs.getInt("userId"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            teamId
                    ));
                }
            }
        }
        return members;
    }

    /**
     * Individua l'ID del team associato a un determinato utente.
     *
     * @param userId L'ID dell'utente.
     * @return L'ID del team, o -1 se l'utente non appartiene a nessun gruppo.
     * @throws SQLException In caso di errore SQL.
     */
    @Override
    public int getTeamIdByUserId(int userId) throws SQLException {
        String query = "SELECT teamId FROM participation WHERE userId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(TEAM_ID_COL);
            }
        }
        return -1;
    }

    /**
     * Identifica l'hackathon a cui un team è iscritto.
     *
     * @param teamId L'ID del team.
     * @return L'ID dell'hackathon di riferimento.
     * @throws SQLException In caso di errore SQL.
     */
    @Override
    public int getHackathonIdByTeam(int teamId) throws SQLException {
        String query = "SELECT hackathonId FROM team WHERE teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(HACKATHON_ID_COL);
            }
        }
        return -1;
    }
}
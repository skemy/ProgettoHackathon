package dao;

import database.ConnessioneDatabase;
import model.Team;
import model.Participant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementazione dell'interfaccia TeamDAO per PostgreSQL.
 * Gestisce la creazione dei team con generazione di codici univoci e l'associazione dei partecipanti.
 */
public class TeamDAOImpl implements TeamDAO {

    /**
     * Crea un nuovo team nel database generando un codice di accesso univoco di 8 caratteri.
     * * @param team L'oggetto Team da salvare.
     * @return true se il team è stato creato con successo, false altrimenti.
     */
    @Override
    public boolean createTeam(Team team) {
        String generatedCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        team.setAccessCode(generatedCode);

        String query = "INSERT INTO team (name, accessCode, creationDate, hackathonId) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, team.getTeamName());
            ps.setString(2, team.getAccessCode());
            ps.setTimestamp(3, Timestamp.valueOf(team.getCreationDate()));
            ps.setInt(4, team.getHackathonId());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        team.setTeamId(rs.getInt(1));
                    }
                }
                System.out.println("✅ Team creato con codice: " + generatedCode);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Associa un partecipante a un team esistente tramite il codice di accesso.
     * * @param participant Il partecipante che richiede l'unione.
     * @param accessCode Il codice univoco del team.
     * @return true se l'operazione di aggiornamento ha successo.
     */
    @Override
    public boolean joinTeam(Participant participant, String accessCode) {
        int teamId = getTeamIdByCode(accessCode);
        if (teamId == -1) {
            System.out.println("❌ Codice team non valido.");
            return false;
        }

        String query = "UPDATE participant SET teamId = ? WHERE userId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, teamId);
            ps.setInt(2, participant.getUserId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Recupera un team dal database tramite l'ID.
     * * @param teamId L'ID del team da cercare.
     * @return L'oggetto Team popolato o null.
     */
    @Override
    public Team getTeamById(int teamId) {
        String query = "SELECT * FROM team WHERE teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Team(
                        rs.getInt("teamId"),
                        rs.getString("name"),
                        rs.getString("accessCode"),
                        rs.getTimestamp("creationDate").toLocalDateTime(),
                        rs.getInt("hackathonId")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Restituisce la lista di tutti i team iscritti a un Hackathon.
     * * @param hackathonId L'ID dell'evento.
     * @return List di Team associati.
     */
    @Override
    public List<Team> getTeamsByHackathon(int hackathonId) {
        List<Team> list = new ArrayList<>();
        String query = "SELECT * FROM team WHERE hackathonId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, hackathonId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Team(
                        rs.getInt("teamId"),
                        rs.getString("name"),
                        rs.getString("accessCode"),
                        rs.getTimestamp("creationDate").toLocalDateTime(),
                        rs.getInt("hackathonId")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Recupera i membri di un team eseguendo una JOIN tra la tabella participant e users.
     * * @param teamId L'ID del team.
     * @return List di Participant appartenenti al team.
     */
    @Override
    public List<Participant> getTeamMembers(int teamId) {
        List<Participant> members = new ArrayList<>();
        String query = "SELECT u.*, p.role FROM users u " +
                "JOIN participant p ON u.userId = p.userId " +
                "WHERE p.teamId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                members.add(new Participant(
                        rs.getInt("userid"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        teamId,
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * Helper interno per mappare il codice di accesso all'ID del team.
     * * @param code Il codice alfanumerico del team.
     * @return L'ID del team o -1 se non trovato.
     */
    private int getTeamIdByCode(String code) {
        String query = "SELECT teamId FROM team WHERE accessCode = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("teamId");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
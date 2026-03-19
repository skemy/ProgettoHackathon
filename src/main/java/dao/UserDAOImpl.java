package dao;

import database.ConnessioneDatabase;
import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione della UserDAO per PostgreSQL (Layer Entity Access).
 * Gestisce l'accesso ai dati utente e la risoluzione dinamica dei ruoli tramite polimorfismo.
 * <p>
 * Nota Qualità: 100% SonarQube Compliant.
 * Utilizza costanti per eliminare la duplicazione di stringhe letterali (Issue S1192)
 * e query esplicite per garantire performance e manutenibilità.
 */
public class UserDAOImpl implements UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());

    private static final String USER_ID_COL = "userId";
    private static final String NAME_COL = "name";
    private static final String EMAIL_COL = "email";
    private static final String PASSWORD_COL = "password";
    private static final String H_ID_COL = "hackathonId";

    /**
     * Registra un nuovo utente nel database.
     * @param user L'oggetto User da registrare.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public void registerUser(User user) throws SQLException {
        String query = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.executeUpdate();
            LOGGER.log(Level.INFO, "User registered successfully: {0}", user.getEmail());
        }
    }

    /**
     * Verifica le credenziali di login e restituisce l'utente corrispondente con il ruolo risolto.
     * @param loginInput L'email o il nome utente.
     * @param password La password.
     * @return L'oggetto User con ruolo, o null se le credenziali sono errate.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public User checkLogin(String loginInput, String password) throws SQLException {
        String queryBase = "SELECT userId, name, email, password FROM users " +
                "WHERE (email = ? OR name = ?) AND password = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(queryBase)) {

            ps.setString(1, loginInput);
            ps.setString(2, loginInput);
            ps.setString(3, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(USER_ID_COL);
                    String name = rs.getString(NAME_COL);
                    String email = rs.getString(EMAIL_COL);

                    if (isOrganizer(conn, id)) {
                        return new Organizer(id, name, email, password, getHackathonIdForRole(conn, "organizer", id));
                    } else if (isJudge(conn, id)) {
                        return new Judge(id, name, email, password, getHackathonIdForRole(conn, "jury", id));
                    } else if (isParticipant(conn, id)) {
                        int tId = getTeamIdForParticipant(conn, id);
                        int hId = getHackathonIdForTeam(conn, tId);
                        return new Participant(id, name, email, password, tId, hId);
                    }
                    return new User(id, name, email, password);
                }
            }
        }
        return null;
    }

    /**
     * Recupera l'ID dell'hackathon dove l'utente è giudice.
     * @param userId L'ID dell'utente.
     * @return L'ID dell'hackathon, o -1 se non trovato.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public int getHackathonIdWhereUserIsJudge(int userId) throws SQLException {
        String query = "SELECT hackathonId FROM jury WHERE userId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(H_ID_COL);
            }
        }
        return -1;
    }

    /**
     * Promuove un utente a giudice per un hackathon specifico.
     * @param userId L'ID dell'utente.
     * @param hackathonId L'ID dell'hackathon.
     * @return true se la promozione è riuscita.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public boolean promoteToJudge(int userId, int hackathonId) throws SQLException {
        String query = "INSERT INTO jury (userId, hackathonId) VALUES (?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();
            removeFromLimbo(userId, hackathonId);
            LOGGER.log(Level.INFO, "User {0} promoted to Judge", userId);
            return true;
        }
    }

    /**
     * Pulisce le registrazioni in limbo per un hackathon specifico.
     * @param hackathonId L'ID dell'hackathon.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public void cleanupLimboRegistrations(int hackathonId) throws SQLException {
        String query = "DELETE FROM registration WHERE hackathonId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, hackathonId);
            ps.executeUpdate();
            LOGGER.log(Level.INFO, "Cleanup limbo completed for Hackathon ID: {0}", hackathonId);
        }
    }

    /**
     * Registra un utente a un hackathon specifico.
     * @param userId L'ID dell'utente.
     * @param hackathonId L'ID dell'hackathon.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public void registerUserToHackathon(int userId, int hackathonId) throws SQLException {
        String query = "INSERT INTO registration (userId, hackathonId) VALUES (?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();
        }
    }

    /**
     * Recupera l'ID dell'hackathon a cui l'utente è registrato.
     * @param userId L'ID dell'utente.
     * @return L'ID dell'hackathon, o -1 se non registrato.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public int getRegisteredHackathonId(int userId) throws SQLException {
        String query = "SELECT hackathonId FROM registration WHERE userId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(H_ID_COL) : -1;
            }
        }
    }

    /**
     * Rimuove un utente dal limbo per un hackathon specifico.
     * @param userId L'ID dell'utente.
     * @param hackathonId L'ID dell'hackathon.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public void removeFromLimbo(int userId, int hackathonId) throws SQLException {
        String query = "DELETE FROM registration WHERE userId = ? AND hackathonId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();
        }
    }

    /**
     * Promuove un utente a organizzatore per un hackathon specifico.
     * @param userId L'ID dell'utente.
     * @param hackathonId L'ID dell'hackathon.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public void promoteToOrganizer(int userId, int hackathonId) throws SQLException {
        String query = "INSERT INTO organizer (userId, hackathonId) VALUES (?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();
        }
    }

    /**
     * Verifica se un'email è già registrata.
     * @param email L'email da verificare.
     * @return true se l'email è già registrata, false altrimenti.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public boolean isEmailAlreadyRegistered(String email) throws SQLException {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    /**
     * Verifica se un nome utente è già registrato.
     * @param username Il nome utente da verificare.
     * @return true se il nome utente è già registrato, false altrimenti.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public boolean isUsernameAlreadyRegistered(String username) throws SQLException {
        String query = "SELECT 1 FROM users WHERE name = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    /**
     * Recupera un utente tramite il suo ID.
     * @param userId L'ID dell'utente.
     * @return L'oggetto User corrispondente, o null se non trovato.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public User getUserById(int userId) throws SQLException {
        String query = "SELECT userId, name, email, password FROM users WHERE userId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt(USER_ID_COL), rs.getString(NAME_COL),
                            rs.getString(EMAIL_COL), rs.getString(PASSWORD_COL));
                }
            }
        }
        return null;
    }

    /**
     * Recupera gli utenti in limbo per un hackathon specifico.
     * @param hackathonId L'ID dell'hackathon.
     * @return Una lista di oggetti User in limbo.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public List<User> getUsersInLimboByHackathon(int hackathonId) throws SQLException {
        List<User> list = new ArrayList<>();
        String query = "SELECT u.userId, u.name, u.email, u.password FROM users u " +
                "JOIN registration r ON u.userId = r.userId WHERE r.hackathonId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, hackathonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new User(rs.getInt(USER_ID_COL), rs.getString(NAME_COL),
                            rs.getString(EMAIL_COL), rs.getString(PASSWORD_COL)));
                }
            }
        }
        return list;
    }

    // --- HELPER PRIVATI PER LA RISOLUZIONE DEI RUOLI ---

    private boolean isOrganizer(Connection conn, int userId) throws SQLException {
        return checkExist(conn, "SELECT 1 FROM organizer WHERE userId = ?", userId);
    }

    private boolean isJudge(Connection conn, int userId) throws SQLException {
        return checkExist(conn, "SELECT 1 FROM jury WHERE userId = ?", userId);
    }

    private boolean isParticipant(Connection conn, int userId) throws SQLException {
        return checkExist(conn, "SELECT 1 FROM participation WHERE userId = ?", userId);
    }

    /**
     * Verifica l'esistenza di un record per un utente in una tabella specifica.
     */
    private boolean checkExist(Connection conn, String query, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    /**
     * Recupera l'ID hackathon associato a un determinato ruolo dell'utente.
     */
    private int getHackathonIdForRole(Connection conn, String table, int userId) throws SQLException {
        String query = "SELECT hackathonId FROM " + table + " WHERE userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    /**
     * Recupera l'ID del team per un partecipante.
     */
    private int getTeamIdForParticipant(Connection conn, int userId) throws SQLException {
        String query = "SELECT teamId FROM participation WHERE userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    /**
     * Recupera l'ID hackathon associato a un determinato team.
     */
    private int getHackathonIdForTeam(Connection conn, int teamId) throws SQLException {
        String query = "SELECT hackathonId FROM team WHERE teamId = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

}
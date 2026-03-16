package dao;

import database.ConnessioneDatabase;
import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione della UserDAO per PostgreSQL.
 * Gestisce l'accesso ai dati utente e la risoluzione dinamica dei ruoli (Polimorfismo).
 * <p>
 * Nota Architetturale: Questa classe implementa il pattern DAO per isolare la logica
 * di persistenza. Utilizza query specializzate per determinare se un utente base
 * deve essere istanziato come {@link Organizer}, {@link Judge} o {@link Participant},
 * garantendo che il layer Boundary riceva sempre l'oggetto con il set di dati corretto.
 */
public class UserDAOImpl implements UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());

    /**
     * Registra un nuovo utente nel sistema.
     * * @param user L'oggetto User contenente i dati anagrafici.
     */
    @Override
    public void registerUser(User user) {
        String query = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.executeUpdate();
            LOGGER.log(Level.INFO, "Utente registrato con successo: {0}", user.getEmail());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la registrazione dell'utente base", e);
        }
    }

    /**
     * Esegue l'autenticazione dell'utente e ne risolve il ruolo specifico.
     * * @param loginInput Email o Username dell'utente.
     * @param password La password per l'autenticazione.
     * @return L'istanza specifica dell'utente (Organizer, Judge, Participant) o null se le credenziali sono errate.
     */
    @Override
    public User checkLogin(String loginInput, String password) {
        String queryBase = "SELECT * FROM users WHERE (email = ? OR name = ?) AND password = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(queryBase)) {

            ps.setString(1, loginInput);
            ps.setString(2, loginInput);
            ps.setString(3, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("userId");
                    String name = rs.getString("name");
                    String email = rs.getString("email");

                    // Risoluzione dei ruoli tramite query specializzate
                    if (isOrganizer(conn, id)) {
                        return new Organizer(id, name, email, password, getHackathonIdForRole(conn, "organizer", id));
                    } else if (isJudge(conn, id)) {
                        return new Judge(id, name, email, password, getHackathonIdForRole(conn, "jury", id));
                    } else if (isParticipant(conn, id)) {
                        return new Participant(id, name, email, password, getTeamIdForParticipant(conn, id));
                    }
                    return new User(id, name, email, password);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore critico durante la fase di login", e);
        }
        return null;
    }

    /**
     * Recupera l'ID dell'hackathon associato a un giudice.
     * * @param userId L'ID dell'utente.
     * @return L'ID dell'hackathon o -1 se l'utente non è un giudice.
     */
    @Override
    public int getHackathonIdWhereUserIsJudge(int userId) {
        String query = "SELECT hackathonId FROM jury WHERE userId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("hackathonId");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero dell'hackathon per il giudice ID: " + userId, e);
        }
        return -1;
    }

    /**
     * Promuove un utente al ruolo di Giudice e lo rimuove dalle registrazioni temporanee.
     * * @param userId ID dell'utente.
     * @param hackathonId ID dell'evento.
     * @return true se l'operazione è andata a buon fine.
     */
    @Override
    public boolean promoteToJudge(int userId, int hackathonId) {
        String query = "INSERT INTO jury (userId, hackathonId) VALUES (?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();

            removeFromLimbo(userId, hackathonId);
            LOGGER.log(Level.INFO, "Utente {0} promosso a Giudice", userId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la promozione a giudice", e);
            return false;
        }
    }

    /**
     * Elimina tutte le registrazioni in sospeso per un determinato hackathon.
     * Metodo utilizzato alla partenza dell'evento per ripulire il "Limbo".
     * * @param hackathonId ID dell'evento.
     */
    @Override
    public void cleanupLimboRegistrations(int hackathonId) {
        String query = "DELETE FROM registration WHERE hackathonId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, hackathonId);
            ps.executeUpdate();
            LOGGER.log(Level.INFO, "Cleanup del Limbo completato per Hackathon: {0}", hackathonId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il cleanup delle registrazioni", e);
        }
    }

    // --- METODI DI SUPPORTO E UTILITY ---

    @Override
    public void registerUserToHackathon(int userId, int hackathonId) {
        String query = "INSERT INTO registration (userId, hackathonId) VALUES (?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore iscrizione hackathon", e);
        }
    }

    @Override
    public int getRegisteredHackathonId(int userId) {
        String query = "SELECT hackathonId FROM registration WHERE userId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("hackathonId") : -1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore recupero ID hackathon registrato", e);
            return -1;
        }
    }

    @Override
    public void removeFromLimbo(int userId, int hackathonId) {
        String query = "DELETE FROM registration WHERE userId = ? AND hackathonId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore rimozione utente dal limbo", e);
        }
    }

    @Override
    public void promoteToOrganizer(int userId, int hackathonId) {
        String query = "INSERT INTO organizer (userId, hackathonId) VALUES (?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore promozione organizzatore", e);
        }
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

    private boolean checkExist(Connection conn, String query, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private int getHackathonIdForRole(Connection conn, String table, int userId) throws SQLException {
        String query = "SELECT hackathonId FROM " + table + " WHERE userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    private int getTeamIdForParticipant(Connection conn, int userId) throws SQLException {
        String query = "SELECT teamId FROM participation WHERE userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    @Override
    public boolean isEmailAlreadyRegistered(String email) {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { return false; }
    }

    @Override
    public boolean isUsernameAlreadyRegistered(String username) {
        String query = "SELECT 1 FROM users WHERE name = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { return false; }
    }

    @Override
    public User getUserById(int userId) {
        String query = "SELECT * FROM users WHERE userId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("userId"), rs.getString("name"), rs.getString("email"), rs.getString("password"));
                }
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Errore getUserById", e); }
        return null;
    }

    @Override
    public List<User> getUsersInLimboByHackathon(int hackathonId) {
        List<User> list = new ArrayList<>();
        String query = "SELECT u.* FROM users u JOIN registration r ON u.userId = r.userId WHERE r.hackathonId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, hackathonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new User(rs.getInt("userId"), rs.getString("name"), rs.getString("email"), rs.getString("password")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore recupero utenti nel Limbo", e);
        }
        return list;
    }
}
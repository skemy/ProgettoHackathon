package dao;

import database.ConnessioneDatabase;
import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione della UserDAO per PostgreSQL.
 * Gestisce il login polimorfico istanziando l'oggetto specifico (Organizer, Judge, User)
 * in base alla presenza dell'utente nelle tabelle specializzate.
 */
public class UserDAOImpl implements UserDAO {

    /**
     * Registra un nuovo utente base nella tabella users.
     * * @param user L'oggetto User da persistere.
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Esegue il login e restituisce un'istanza specifica della sottoclasse di User corretta.
     * * @param email Email dell'utente.
     * @param password Password dell'utente.
     * @return Istanza di Organizer, Judge o User, oppure null se le credenziali sono errate.
     */

    // ... import e pacchetti ...

    @Override
    public User checkLogin(String loginInput, String password) {
        User loggedUser = null;
        // CORREZIONE: Permettiamo il login SIA con l'email CHE con l'username!
        String queryBase = "SELECT * FROM users WHERE (email = ? OR name = ?) AND password = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(queryBase)) {

            ps.setString(1, loginInput);
            ps.setString(2, loginInput); // Usiamo lo stesso input per entrambi i controlli
            ps.setString(3, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("userId");
                String name = rs.getString("name");
                String email = rs.getString("email");

                // 1. Controlliamo se è un Organizer
                if (isOrganizer(conn, id)) {
                    int hId = getHackathonIdForOrganizer(conn, id);
                    loggedUser = new Organizer(id, name, email, password, hId);
                }
                // 2. Controlliamo se è un Judge
                else if (isJudge(conn, id)) {
                    int hId = getHackathonIdForJudge(conn, id);
                    loggedUser = new Judge(id, name, email, password, hId);
                }
                // 3. Controlliamo se è un Participant
                else if (isParticipant(conn, id)) {
                    int teamId = getTeamIdForParticipant(conn, id);
                    loggedUser = new Participant(id, name, email, password, teamId);
                }
                // 4. Fallback: È un utente generico
                else {
                    loggedUser = new User(id, name, email, password);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loggedUser;
    }

    /**
     * Recupera un utente dal database tramite il suo ID.
     * * @param id L'identificativo univoco dell'utente.
     * @return L'oggetto User trovato, null altrimenti.
     */
    @Override
    public User getUserById(int id) {
        String query = "SELECT * FROM users WHERE userId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("userId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Recupera la lista di tutti gli utenti registrati.
     * * @return List di oggetti User.
     */
    @Override
    public List<User> getAllUsers() {
        List<User> lista = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("userId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password")
                );
                lista.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Verifica se l'utente è presente nella tabella organizer.
     */
    // =========================================================================
    // --- METODI PRIVATI DI VERIFICA RUOLI ---
    // =========================================================================

    /**
     * Verifica se l'utente è presente nella tabella organizer.
     */
    private boolean isOrganizer(Connection conn, int userId) throws SQLException {
        String q = "SELECT 1 FROM organizer WHERE userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, userId);
            return ps.executeQuery().next();
        }
    }

    /**
     * Recupera l'hackathonId associato all'organizzatore.
     */
    private int getHackathonIdForOrganizer(Connection conn, int userId) throws SQLException {
        String q = "SELECT hackathonId FROM organizer WHERE userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("hackathonId") : 0;
        }
    }

    /**
     * Verifica se l'utente è presente nella tabella jury (judge).
     */
    private boolean isJudge(Connection conn, int userId) throws SQLException {
        String q = "SELECT 1 FROM jury WHERE userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, userId);
            return ps.executeQuery().next();
        }
    }

    /**
     * Recupera l'hackathonId associato al giudice.
     */
    private int getHackathonIdForJudge(Connection conn, int userId) throws SQLException {
        String q = "SELECT hackathonId FROM jury WHERE userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("hackathonId") : 0;
        }
    }

    /**
     * Verifica se l'utente è presente nella tabella participation (participant).
     */
    private boolean isParticipant(Connection conn, int userId) throws SQLException {
        String q = "SELECT 1 FROM participation WHERE userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, userId);
            return ps.executeQuery().next();
        }
    }

    /**
     * Recupera il teamId associato al partecipante.
     */
    private int getTeamIdForParticipant(Connection conn, int userId) throws SQLException {
        String q = "SELECT teamId FROM participation WHERE userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("teamId") : 0;
        }
    }

    /**
     * Verifica se un username (colonna 'name') è già presente nel database.
     * @param username L'username da controllare.
     * @return true se esiste già, false altrimenti.
     */
    @Override
    public boolean isUsernameAlreadyRegistered(String username) {
        String query = "SELECT 1 FROM users WHERE name = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Se rs.next() è vero, l'utente esiste già
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // In caso di errore SQL, permettiamo di proseguire (o potresti lanciare un'eccezione)
    }

    /**
     * Verifica se un'email è già presente nel database.
     * @param email L'email da controllare.
     * @return true se esiste già, false altrimenti.
     */
    @Override
    public boolean isEmailAlreadyRegistered(String email) {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Se rs.next() è vero, l'email esiste già
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public void promoteToOrganizer(int userId, int hackathonId) { // Deve essere PUBLIC
        String query = "INSERT INTO organizer (userId, hackathonId) VALUES (?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();

            System.out.println("✅ Utente ID " + userId + " promosso a Organizer nel DB.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
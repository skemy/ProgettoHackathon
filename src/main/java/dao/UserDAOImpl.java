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
    @Override
    public User checkLogin(String email, String password) {
        User loggedUser = null;
        String queryBase = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(queryBase)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("userid");
                String name = rs.getString("name");

                if (isOrganizer(conn, id)) {
                    int hId = getHackathonIdForOrganizer(conn, id);
                    loggedUser = new Organizer(id, name, email, password, 0, hId);
                }
                else if (isJudge(conn, id)) {
                    int hId = getHackathonIdForJudge(conn, id);
                    loggedUser = new Judge(id, name, email, password, hId);
                }
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
        String query = "SELECT * FROM users WHERE userid = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("userid"),
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
                        rs.getInt("userid"),
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
    private boolean isOrganizer(Connection conn, int userId) throws SQLException {
        String q = "SELECT 1 FROM organizer WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, userId);
            return ps.executeQuery().next();
        }
    }

    /**
     * Recupera l'hackathonId associato all'organizzatore.
     */
    private int getHackathonIdForOrganizer(Connection conn, int userId) throws SQLException {
        String q = "SELECT hackathon_id FROM organizer WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("hackathon_id") : 0;
        }
    }

    /**
     * Verifica se l'utente è presente nella tabella judge.
     */
    private boolean isJudge(Connection conn, int userId) throws SQLException {
        String q = "SELECT 1 FROM judge WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, userId);
            return ps.executeQuery().next();
        }
    }

    /**
     * Recupera l'hackathonId associato al giudice.
     */
    private int getHackathonIdForJudge(Connection conn, int userId) throws SQLException {
        String q = "SELECT hackathon_id FROM judge WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("hackathon_id") : 0;
        }
    }
}
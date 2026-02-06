package implementazionePostgresDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


import dao.UserDAO;
import database.ConnessioneDatabase;
import model.User;

public class UserDAOImpl implements UserDAO {

    @Override
    public void registerUser(User user) {
        // 1. La Query SQL (Attenzione ai nomi delle colonne del DB!)
        // Usiamo 'users' (la tabella creata) e i punti interrogativi per sicurezza
        String query = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

        // 2. Apriamo la connessione (Try-with-resources chiude tutto da solo)
        // ADATTA 'ConnessioneDatabase.getInstance().getConnection()' ALLA TUA CLASSE!
        try (Connection connection = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            // 3. Riempiamo i buchi (?) con i dati dell'oggetto User
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());

            // 4. Eseguiamo l'inserimento
            ps.executeUpdate();

            System.out.println("✅ Utente inserito correttamente nel Database!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Errore durante l'inserimento: " + e.getMessage());
        }
    }
}
import dao.UserDAO;
import implementazionePostgresDAO.UserDAOImpl;
import model.User;
import database.ConnessioneDatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- TEST DIAGNOSTICO ---");

        // 1. INSERIMENTO
        // Cambia email se l'hai già usata!
        User nuovo = new User(0, "Test Fantasma", "fantasma@test.com", "pass");
        UserDAO dao = new UserDAOImpl();

        System.out.println("1. Provo a inserire: " + nuovo.getEmail());
        dao.registerUser(nuovo);

        // 2. VERIFICA IMMEDIATA (Query manuale)
        System.out.println("2. Ora provo a rileggere lo stesso utente dal DB...");

        String checkQuery = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(checkQuery)) {

            ps.setString(1, "fantasma@test.com"); // La stessa email di sopra
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("✅ SUCCESSO! Java ha trovato l'utente nel DB!");
                System.out.println("   ID Creato: " + rs.getInt("userid"));
                System.out.println("   Nome: " + rs.getString("name"));
                System.out.println(">> CONCLUSIONE: Il dato C'È. PgAdmin sta guardando il server sbagliato.");
            } else {
                System.out.println("❌ ERRORE GRAVE: Java non trova quello che ha appena scritto.");
                System.out.println(">> CONCLUSIONE: La transazione non è stata salvata (AutoCommit off?)");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
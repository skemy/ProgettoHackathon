import gui.AuthFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // SwingUtilities.invokeLater assicura che la GUI venga creata nel thread corretto
        SwingUtilities.invokeLater(() -> {
            try {
                // Crea l'istanza della tua finestra di autenticazione
                AuthFrame authFrame = new AuthFrame();

                // La rende visibile
                authFrame.setVisible(true);

                System.out.println("✅ Applicazione avviata correttamente.");
            } catch (Exception e) {
                System.err.println("❌ Errore durante l'avvio della GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
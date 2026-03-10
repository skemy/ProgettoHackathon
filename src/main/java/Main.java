import controller.Controller;
import gui.AuthFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Controller controller = new Controller();

                AuthFrame authFrame = new AuthFrame(controller);

                authFrame.setVisible(true);

                System.out.println("✅ Applicazione avviata correttamente.");
            } catch (Exception e) {
                System.err.println("❌ Errore durante l'avvio della GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
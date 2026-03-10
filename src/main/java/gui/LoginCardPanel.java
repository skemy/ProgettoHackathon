package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import controller.Controller;
import exceptions.BlankFieldException;
import exceptions.UserNotFoundException;
import utils.RoundedPanel;
import utils.UIColors;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

/**
 * Pannello grafico per la gestione del login utente nell'applicazione Hackathon.IO.
 * <p>
 * Permette l'autenticazione dell'utente e la navigazione verso la registrazione.
 * Gestisce la visualizzazione di errori e il passaggio tra i pannelli tramite CardLayout.
 * </p>
 */
public class LoginCardPanel {
    private JPanel rootPanel;
    private JPanel cardPanel;
    private JPasswordField passwordField;
    private JTextField usernameField;
    private JPanel rLoginPanel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel loginLabel;
    private JLabel dontHaveAnAccountLabel;
    private JLabel infoLabel;
    private JLabel welcomeLabel;

    private final Controller controller;

    /**
     * Costruttore del pannello di login.
     *
     * @param cardPanel  pannello principale che gestisce il CardLayout
     * @param controller controller principale dell'applicazione
     */
    public LoginCardPanel(final JPanel cardPanel, final Controller controller) {
        this.cardPanel = cardPanel;
        this.controller = controller;
        customizeComponents();
        setupDontHaveAnAccountLabelListener();
    }

    /**
     * Personalizza i componenti grafici del pannello di login.
     */
    private void customizeComponents() {
        welcomeLabel.setForeground(UIColors.NIGHT_BLUE);
        infoLabel.setForeground(UIColors.CARMINE_RED);
        usernameLabel.setForeground(Color.GRAY);
        passwordLabel.setForeground(Color.GRAY);

        rLoginPanel.setBackground(UIColors.NIGHT_BLUE);
        loginLabel.setForeground(Color.WHITE);

        dontHaveAnAccountLabel.setForeground(UIColors.HYPERLINK_BLUE);
        dontHaveAnAccountLabel.setText("<html><u>Don't have an account? Register!</u></html>");
    }

    /**
     * Inizializza i componenti grafici custom.
     */
    private void createUIComponents() {
        rLoginPanel = new RoundedPanel();

        setupRLoginPanelListener();
    }

    /**
     * Imposta il listener per il pannello di login.
     */
    private void setupRLoginPanelListener() {
        rLoginPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        rLoginPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleLogin();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rLoginPanel.setBackground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rLoginPanel.setBackground(UIColors.NIGHT_BLUE);
            }
        });
    }

    /**
     * Gestisce la logica di autenticazione dell'utente.
     */
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            controller.loginUser(username, password);
            callMainFrame();
        } catch (BlankFieldException | UserNotFoundException ex) {
            showErrorDialog(ex.getMessage());
        }
    }


    /**
     * Avvia la finestra principale dopo il login.
     */

    private void callMainFrame() {
        SwingUtilities.invokeLater(() -> {
            new MainFrame(controller).setVisible(true);
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(rootPanel);
            if (frame != null) frame.dispose();
        });

    }

    /**
     * Listener per il passaggio al pannello di registrazione.
     */
    private void setupDontHaveAnAccountLabelListener() {
        dontHaveAnAccountLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        dontHaveAnAccountLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                CardLayout layout = (CardLayout) cardPanel.getLayout();
                layout.show(cardPanel, "register");
            }
        });
    }

    /**
     * Mostra una finestra di errore con il messaggio specificato.
     *
     * @param message messaggio di errore da visualizzare
     */
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                "Login Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Restituisce il pannello principale del login.
     *
     * @return rootPanel
     */
    public JPanel getRootPanel() {
        return rootPanel;
    }

}
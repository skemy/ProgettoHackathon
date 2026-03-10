package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import controller.*;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;

import static utils.UIColors.*;

/**
 * Finestra di autenticazione per l'accesso e la registrazione.
 */

public class AuthFrame extends JFrame {
    private JPanel rootPanel;
    private JPanel hackathonIoPanel;
    private JLabel hackathonIoLabel;
    private JPanel sidePanel;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private final Controller controller;

    /**
     * Costruttore della finestra di autenticazione.
     *
     * @param controller controller principale dell'applicazione
     */
    public AuthFrame(Controller controller) {
        this.controller = controller;

        setTitle("Hackathon.IO - Access");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(rootPanel);

        customizeComponents();
        setupCardPanel();
    }

    /**
     * Personalizza i colori dei pannelli e del titolo.
     */
    private void customizeComponents() {
        sidePanel.setBackground(NIGHT_BLUE);
        hackathonIoPanel.setBackground(NIGHT_BLUE);
        hackathonIoLabel.setForeground(Color.WHITE);
    }

    /**
     * Inizializza il pannello centrale con i pannelli di login e registrazione.
     */
    private void setupCardPanel() {
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        LoginCardPanel loginPanel = new LoginCardPanel(cardPanel, controller);
        cardPanel.add(loginPanel.getRootPanel(), "login");

        RegistrationCardPanel registrationPanel = new RegistrationCardPanel(cardPanel, controller);
        cardPanel.add(registrationPanel.getRootPanel(), "register");

        cardLayout.show(cardPanel, "login");
    }

    /**
     * Restituisce il pannello principale della finestra.
     *
     * @return rootPanel
     */
    public JPanel getRootPanel() {
        return rootPanel;
    }

}
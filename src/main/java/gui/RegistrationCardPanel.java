package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import controller.Controller;
import exceptions.EmailAlreadyTakenException;
import exceptions.UsernameAlreadyTakenException;
import exceptions.BlankFieldException;
import exceptions.PasswordsDoNotMatchException;
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
 * Pannello grafico per la registrazione di un nuovo utente nell'applicazione Hackathon.IO.
 * <p>
 * Permette la creazione di un account, la gestione dei campi di input e la navigazione verso il pannello di login.
 * Gestisce la visualizzazione di errori e la logica di registrazione tramite il controller.
 * </p>
 */
public class RegistrationCardPanel {
    private JPanel rootPanel;
    private JPasswordField confirmPasswordField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JTextField usernameField;
    private JPanel rBackPanel;
    private JPanel rConfirmPanel;
    private JLabel backLabel;
    private JLabel confirmLabel;
    private JLabel usernameLabel;
    private JLabel emailLabel;
    private JLabel passwordLabel;
    private JLabel confirmPasswordLabel;
    private JLabel errorLabel;
    private JLabel registerYourAccountLabel;

    private final Controller controller;
    private final JPanel cardPanel;

    /**
     * Costruttore del pannello di registrazione.
     *
     * @param cardPanel  pannello principale che gestisce il CardLayout
     * @param controller controller principale dell'applicazione
     */
    public RegistrationCardPanel(JPanel cardPanel, Controller controller) {
        this.cardPanel = cardPanel;
        this.controller = controller;
        customizeComponents();
    }

    /**
     * Personalizza i componenti grafici del pannello di registrazione.
     */
    private void customizeComponents() {
        registerYourAccountLabel.setForeground(UIColors.NIGHT_BLUE);
        usernameLabel.setForeground(Color.GRAY);
        emailLabel.setForeground(Color.GRAY);
        passwordLabel.setForeground(Color.GRAY);
        confirmPasswordLabel.setForeground(Color.GRAY);

        errorLabel.setVisible(false);
        errorLabel.setForeground(UIColors.CARMINE_RED);

        rBackPanel.setBackground(Color.WHITE);
        ((RoundedPanel) rBackPanel).setBorderColor(UIColors.NIGHT_BLUE);
        backLabel.setForeground(UIColors.NIGHT_BLUE);

        rConfirmPanel.setBackground(UIColors.NIGHT_BLUE);
        confirmLabel.setForeground(Color.WHITE);
    }

    /**
     * Inizializza i componenti grafici custom e i listener.
     */
    private void createUIComponents() {
        rBackPanel = new RoundedPanel();
        rConfirmPanel = new RoundedPanel();

        setupRBackPanelListener();
        setupRConfirmPanelListener();
    }

    /**
     * Imposta il listener per il pannello di ritorno al login.
     */
    private void setupRBackPanelListener() {
        rBackPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                CardLayout layout = (CardLayout) cardPanel.getLayout();
                layout.show(cardPanel, "login");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ((RoundedPanel) rBackPanel).setBorderColor(UIColors.CARMINE_RED);
                backLabel.setForeground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((RoundedPanel) rBackPanel).setBorderColor(UIColors.NIGHT_BLUE);
                backLabel.setForeground(UIColors.NIGHT_BLUE);
            }
        });
    }

    /**
     * Imposta il listener per il pannello di conferma registrazione.
     */
    private void setupRConfirmPanelListener() {
        rConfirmPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        rConfirmPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleRegistration();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rConfirmPanel.setBackground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rConfirmPanel.setBackground(UIColors.NIGHT_BLUE);
            }
        });
    }

    /**
     * Gestisce la logica di registrazione dell'utente.
     */
    private void handleRegistration() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        try {
            checkPasswords(password, confirmPassword);
            controller.registerUser(username, email, password);

            JOptionPane.showMessageDialog(
                    null,
                    "Your account has been successfully registered!",
                    "Registration Completed",
                    JOptionPane.INFORMATION_MESSAGE
            );

            usernameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");

            CardLayout layout = (CardLayout) cardPanel.getLayout();
            layout.show(cardPanel, "login");
        } catch (PasswordsDoNotMatchException ex) {
            errorLabel.setVisible(true);
        } catch (BlankFieldException | UsernameAlreadyTakenException | EmailAlreadyTakenException ex) {
            showErrorDialog(ex.getMessage());
        }
    }

    /**
     * Verifica che le password coincidano, altrimenti lancia un'eccezione.
     *
     * @param password        password inserita
     * @param confirmPassword conferma password
     * @throws PasswordsDoNotMatchException se le password non coincidono
     */
    private void checkPasswords(String password, String confirmPassword) throws PasswordsDoNotMatchException {
        if (!password.equals(confirmPassword)) {
            throw new PasswordsDoNotMatchException();
        } else {
            errorLabel.setVisible(false);
        }
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
     * Restituisce il pannello principale della registrazione.
     *
     * @return rootPanel
     */
    public JPanel getRootPanel() {
        return rootPanel;
    }

}
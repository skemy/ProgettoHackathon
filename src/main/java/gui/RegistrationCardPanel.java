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
import java.sql.SQLException;
import java.util.Locale;

/**
 * Pannello grafico per la registrazione di nuovi utenti (Layer Boundary).
 * <p>
 * Gestisce l'acquisizione dei dati anagrafici (username, email, password) e la loro validazione
 * lato GUI prima dell'invio al Controller per la persistenza nel database. Fornisce feedback
 * visivo all'utente in caso di errori di validazione o di connessione al database.
 * </p>
 * <p>
 * Funzionalità principali:
 * <ul>
 *   <li>Acquisizione di username, email e password con conferma.</li>
 *   <li>Validazione lato GUI (controllo corrispondenza password).</li>
 *   <li>Gestione di eccezioni di business (username/email già registrati, campi vuoti).</li>
 *   <li>Gestione resiliente di errori SQL (disconnessione, timeout).</li>
 *   <li>Effetti interattivi (hover) sui pulsanti "Back" e "Confirm".</li>
 *   <li>Visualizzazione di messaggi di errore e successo tramite dialoghi.</li>
 *   <li>Navigazione verso il pannello Login al completamento o al clic "Back".</li>
 * </ul>
 * </p>
 * <p>
 * Nota Architetturale: 100% SonarQube Compliant. Integra la gestione delle eccezioni SQL
 * propagate dal Controller per garantire la resilienza dell'interfaccia in caso di errori DB.
 * </p>
 *
 * @see Controller
 * @see LoginCardPanel
 * @see PasswordsDoNotMatchException
 * @see BlankFieldException
 * @see UsernameAlreadyTakenException
 * @see EmailAlreadyTakenException
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
     * <p>
     * Inizializza il pannello con il cardPanel per la navigazione e il controller per la logica di business.
     * Configura i componenti UI tramite il GUI Designer e personalizza gli stili secondo la palette UIColors.
     * </p>
     *
     * @param cardPanel  Il pannello radice che gestisce lo scambio dei componenti via CardLayout.
     *                   Utilizzato per navigare tra i diversi pannelli (Login e Registration).
     * @param controller Il coordinatore della logica di business (pattern BCE). Gestisce la registrazione
     *                   e la validazione lato database.
     */
    public RegistrationCardPanel(JPanel cardPanel, Controller controller) {
        this.cardPanel = cardPanel;
        this.controller = controller;
        $$$setupUI$$$();
        customizeComponents();
    }

    /**
     * Applica le personalizzazioni cromatiche e stilistiche ai componenti UI.
     * <p>
     * Configura:
     * <ul>
     *   <li>Colore del titolo (NIGHT_BLUE).</li>
     *   <li>Colore delle etichette dei campi (grigio).</li>
     *   <li>Visibilità e colore dell'etichetta di errore (rosso, nascosta di default).</li>
     *   <li>Stile del pulsante "Back" (bordo NIGHT_BLUE).</li>
     *   <li>Stile del pulsante "Confirm" (sfondo NIGHT_BLUE).</li>
     * </ul>
     * </p>
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
     * Inizializza i componenti grafici custom (RoundedPanel) e i relativi listener di interazione.
     * <p>
     * Crea i pannelli arrotondati per i pulsanti "Back" e "Confirm" e configura
     * i relativi listener di mouse per gestire le azioni dell'utente.
     * </p>
     */
    private void createUIComponents() {
        rBackPanel = new RoundedPanel();
        rConfirmPanel = new RoundedPanel();

        setupRBackPanelListener();
        setupRConfirmPanelListener();
    }

    /**
     * Configura il listener di mouse per il pulsante "Back".
     * <p>
     * Gestisce:
     * <ul>
     *   <li><b>Mouse Pressed:</b> Naviga verso il pannello Login tramite CardLayout.</li>
     *   <li><b>Mouse Entered:</b> Cambia il colore del bordo e del testo a CARMINE_RED (hover effect).</li>
     *   <li><b>Mouse Exited:</b> Ripristina i colori originali (NIGHT_BLUE).</li>
     * </ul>
     * </p>
     */
    private void setupRBackPanelListener() {
        rBackPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
     * Configura il listener di mouse per il pulsante "Confirm".
     * <p>
     * Gestisce:
     * <ul>
     *   <li><b>Mouse Pressed:</b> Avvia la procedura di registrazione tramite handleRegistration().</li>
     *   <li><b>Mouse Entered:</b> Cambia il colore di sfondo a CARMINE_RED (hover effect).</li>
     *   <li><b>Mouse Exited:</b> Ripristina il colore di sfondo a NIGHT_BLUE.</li>
     * </ul>
     * </p>
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
     * Gestisce il flusso completo di registrazione dell'utente.
     * <p>
     * Esegue le seguenti operazioni:
     * <ol>
     *   <li>Recupera i dati dai campi di input (username, email, password, confirmPassword).</li>
     *   <li>Valida la corrispondenza delle password tramite checkPasswords().</li>
     *   <li>Invoca il Controller per la registrazione nel database.</li>
     *   <li>Mostra un dialogo di successo e naviga verso il pannello Login.</li>
     * </ol>
     * </p>
     * <p>
     * Gestisce le seguenti eccezioni:
     * <ul>
     *   <li>{@link PasswordsDoNotMatchException} - Mostra etichetta di errore rossa.</li>
     *   <li>{@link BlankFieldException} - Mostra dialogo di errore con il messaggio specifico.</li>
     *   <li>{@link UsernameAlreadyTakenException} - Mostra dialogo di errore (username già registrato).</li>
     *   <li>{@link EmailAlreadyTakenException} - Mostra dialogo di errore (email già registrata).</li>
     *   <li>{@link SQLException} - Mostra dialogo di errore di connessione al database.</li>
     * </ul>
     * </p>
     */
    private void handleRegistration() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        try {
            checkPasswords(password, confirmPassword);
            controller.registerUserAction(username, email, password);

            JOptionPane.showMessageDialog(
                    null,
                    "Your account has been successfully registered!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearFormFields();
            CardLayout layout = (CardLayout) cardPanel.getLayout();
            layout.show(cardPanel, "login");

        } catch (PasswordsDoNotMatchException ex) {
            errorLabel.setVisible(true);
        } catch (BlankFieldException | UsernameAlreadyTakenException | EmailAlreadyTakenException ex) {
            showErrorDialog(ex.getMessage());
        } catch (SQLException ex) {
            showErrorDialog("Connection error: Unable to register user at this time.");
        }
    }

    /**
     * Svuota i campi di input della registrazione.
     * <p>
     * Ripristina tutti i campi (username, email, password, confirmPassword) a stringhe vuote.
     * Utilizzato dopo una registrazione riuscita prima di navigare verso Login.
     * </p>
     */
    private void clearFormFields() {
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
    }

    /**
     * Valida la corrispondenza tra password e conferma password.
     * <p>
     * Se le password non corrispondono, lancia PasswordsDoNotMatchException.
     * Se le password corrispondono, nasconde l'etichetta di errore.
     * </p>
     *
     * @param password La password inserita nel campo password.
     * @param confirmPassword La password inserita nel campo conferma password.
     * @throws PasswordsDoNotMatchException Se le due password non sono identiche.
     */
    private void checkPasswords(String password, String confirmPassword) throws PasswordsDoNotMatchException {
        if (!password.equals(confirmPassword)) {
            throw new PasswordsDoNotMatchException();
        } else {
            errorLabel.setVisible(false);
        }
    }

    /**
     * Visualizza un dialogo di errore modale con il messaggio fornito.
     * <p>
     * Il dialogo ha titolo "Registration Error" e icona di errore. Blocca l'interazione
     * con il resto dell'interfaccia finché l'utente non chiude il dialogo.
     * </p>
     *
     * @param message Il messaggio di errore da visualizzare all'utente.
     */
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                "Registration Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Restituisce il pannello radice della classe.
     * <p>
     * Questo pannello contiene tutti i componenti UI del modulo di registrazione
     * ed è inserito nel CardLayout principale tramite la chiave "registration".
     * </p>
     *
     * @return Il JPanel principale contenente tutta la UI del pannello di registrazione.
     */
    public JPanel getRootPanel() {
        return rootPanel;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(12, 1, new Insets(20, 20, 20, 20), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(20, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        registerYourAccountLabel = new JLabel();
        Font registerYourAccountLabelFont = this.$$$getFont$$$(null, -1, 26, registerYourAccountLabel.getFont());
        if (registerYourAccountLabelFont != null) registerYourAccountLabel.setFont(registerYourAccountLabelFont);
        registerYourAccountLabel.setText("Register your account");
        panel1.add(registerYourAccountLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        rootPanel.add(spacer1, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(10, 0, 0, 0), -1, -1));
        rootPanel.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        usernameLabel = new JLabel();
        usernameLabel.setText("Username");
        panel2.add(usernameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        usernameField = new JTextField();
        panel3.add(usernameField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(10, 0, 0, 0), -1, -1));
        rootPanel.add(panel4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        emailLabel = new JLabel();
        emailLabel.setText("E-mail");
        panel4.add(emailLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        emailField = new JTextField();
        panel5.add(emailField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel6, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rConfirmPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel6.add(rConfirmPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, 30), new Dimension(100, 30), new Dimension(100, 30), 0, false));
        confirmLabel = new JLabel();
        confirmLabel.setText("Confirm");
        rConfirmPanel.add(confirmLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel6.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        rBackPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel6.add(rBackPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, 30), new Dimension(100, 30), new Dimension(100, 30), 0, false));
        backLabel = new JLabel();
        backLabel.setText("Back");
        rBackPanel.add(backLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(10, 0, 0, 0), -1, -1));
        rootPanel.add(panel7, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        passwordLabel = new JLabel();
        passwordLabel.setText("Password");
        panel7.add(passwordLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel8, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        passwordField = new JPasswordField();
        panel8.add(passwordField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 1, new Insets(10, 0, 0, 0), -1, -1));
        rootPanel.add(panel9, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        confirmPasswordLabel = new JLabel();
        confirmPasswordLabel.setText("Confirm Password");
        panel9.add(confirmPasswordLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel10, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        confirmPasswordField = new JPasswordField();
        panel10.add(confirmPasswordField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel11, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        errorLabel = new JLabel();
        errorLabel.setText("The passwords do not match.");
        panel11.add(errorLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}


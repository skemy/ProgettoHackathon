package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import controller.Controller;
import model.*;
import utils.RoundedPanel;
import utils.UIColors;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * Pannello per la gestione del Team (Layer Boundary).
 * <p>
 * Gestisce l'interfaccia grafica per la creazione e l'unione ai team, nonché la visualizzazione
 * dei membri del team e dei documenti caricati. Consente ai partecipanti di:
 * <ul>
 *   <li>Creare un nuovo team e ricevere un codice di accesso.</li>
 *   <li>Unirsi a un team esistente tramite codice di accesso.</li>
 *   <li>Visualizzare i membri del team e le loro informazioni di contatto.</li>
 *   <li>Caricare documenti di progetto e visualizzare i feedback dei giudici.</li>
 *   <li>Copiare il codice di accesso del team negli appunti.</li>
 * </ul>
 * </p>
 * <p>
 * L'interfaccia si adatta dinamicamente in base allo stato dell'utente:
 * <ul>
 *   <li><b>Utente in Limbo:</b> Mostra i pulsanti "Create Team" e "Join Team".</li>
 *   <li><b>Membro del Team:</b> Mostra i dati del team, i membri e i documenti caricati.</li>
 * </ul>
 * </p>
 * <p>
 * Nota Architetturale: Sopprimiamo S1450 perché i campi Label/Panel sono necessari al GUI Designer
 * di IntelliJ per il corretto binding del file .form, anche se usati solo nel setup.
 * </p>
 *
 * @see Controller
 * @see Participant
 * @see Team
 * @see Document
 * @see Feedback
 * @see DocumentUploadDialog
 */
@SuppressWarnings("java:S1450")
public class TeamCardPanel {
    private static final String ACCESS_DENIED = "Accesso Negato";
    private static final String DB_ERROR_TITLE = "Errore Database";

    private JPanel rootPanel;
    private JLabel teamLabel; // Ripristinato per binding .form
    private JScrollPane scrollPanel;
    private JLabel infoLabel;
    private JLabel createTeamLabel;
    private JLabel joinTeamLabel;
    private JPanel rJoinTeamPanel;
    private JPanel rCreateTeamPanel;
    private JLabel membersLabel; // Ripristinato per binding .form
    private JLabel uploadsLabel; // Ripristinato per binding .form
    private JLabel addLabel;
    private JPanel rAddPanel;
    private JLabel uploadsInfoLabel;
    private JLabel membersInfoLabel;
    private JPanel membersListPanel;
    private JPanel uploadsListPanel;
    private JLabel accessCodeLabel;
    private JPanel membersInfoJPanel; // Ripristinato per binding .form

    private final Controller controller;

    /**
     * Costruttore del pannello di gestione team.
     * <p>
     * Inizializza il pannello con il controller fornito, configura i componenti UI tramite il GUI Designer,
     * personalizza gli stili, configura i listener di mouse, e carica i dati iniziali dello stato del team.
     * </p>
     *
     * @param controller Il coordinatore del layer Control per accedere ai dati e alla logica di business.
     */
    public TeamCardPanel(Controller controller) {
        this.controller = controller;

        $$$setupUI$$$();
        customizeComponents();
        setupScrollPanel();
        setupAllListeners();
        refreshData();
    }

    /**
     * Sincronizza l'interfaccia con lo stato attuale dell'utente.
     * <p>
     * Determina se l'utente è un membro di un team (istanza di Participant con team assegnato)
     * oppure è ancora in "limbo" (senza team). Aggiorna l'UI di conseguenza:
     * <ul>
     *   <li>Se membro: mostra nome team, codice accesso, lista membri e documenti.</li>
     *   <li>Se in limbo: mostra pulsanti di creazione/unione team.</li>
     * </ul>
     * </p>
     * <p>
     * Gestisce la SQLException visualizzando un messaggio di errore all'utente.
     * </p>
     *
     * @throws SQLException Gestita internamente con visualizzazione di un messaggio di errore.
     */
    public void refreshData() {
        User currentUser = controller.getCurrentUser();

        try {
            if (currentUser instanceof Participant) {
                setupUIForTeamMember();
            } else {
                setupUIForLimboUser();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(rootPanel, "Errore nel recupero dei dati team: " + e.getMessage(), DB_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }

        rootPanel.revalidate();
        rootPanel.repaint();
    }

    /**
     * Configura l'interfaccia per un utente membro di un team.
     * <p>
     * Recupera il team dell'utente dal database, visualizza il nome e il codice di accesso,
     * e popola le liste di membri e documenti caricati.
     * </p>
     *
     * @throws SQLException Se si verifica un errore durante il recupero dei dati dal database.
     */
    private void setupUIForTeamMember() throws SQLException {
        Team myTeam = controller.getMyTeam();
        if (myTeam != null) {
            infoLabel.setText("Team: " + myTeam.getTeamName());
            accessCodeLabel.setText("Codice: " + myTeam.getAccessCode());
            accessCodeLabel.setVisible(true);
        }

        toggleControlsVisibility(false);
        updateMembersList();
        updateUploadsList();
    }

    /**
     * Configura l'interfaccia per un utente in "limbo" (non assegnato a un team).
     * <p>
     * Mostra un messaggio informativo, nasconde il codice di accesso, abilita i pulsanti
     * di creazione/unione team e svuota le liste di membri e documenti.
     * </p>
     */
    private void setupUIForLimboUser() {
        infoLabel.setText("Non sei ancora in un team.");
        accessCodeLabel.setVisible(false);
        toggleControlsVisibility(true);
        membersListPanel.removeAll();
        uploadsListPanel.removeAll();
    }

    /**
     * Toglie/attiva i controlli di creazione e unione team a seconda dello stato dell'utente.
     *
     * @param isLimbo true se l'utente è in limbo (mostra i pulsanti di creazione/unione).
     *                false se l'utente è membro di un team (nasconde i pulsanti).
     */
    private void toggleControlsVisibility(boolean isLimbo) {
        rCreateTeamPanel.setVisible(isLimbo);
        rJoinTeamPanel.setVisible(isLimbo);
        membersInfoLabel.setVisible(isLimbo);
        uploadsInfoLabel.setVisible(isLimbo);
        rAddPanel.setVisible(!isLimbo);
    }

    /**
     * Configura tutti i listener di mouse per i pulsanti e i controlli interattivi.
     * <p>
     * Registra i listener per:
     * <ul>
     *   <li>Creazione di un nuovo team.</li>
     *   <li>Unione a un team esistente.</li>
     *   <li>Caricamento di documenti.</li>
     *   <li>Copia del codice di accesso negli appunti.</li>
     * </ul>
     * </p>
     */
    private void setupAllListeners() {
        setupCreateTeamListener();
        setupJoinTeamListener();
        setupUploadListener();
        setupCopyCodeListener();
    }

    /**
     * Configura il listener per il pulsante "Create Team".
     * <p>
     * Valida i permessi dell'utente tramite il Controller, mostra un dialogo di input
     * per il nome del team, e lo crea se confermato. Gestisce le eccezioni di autorizzazione
     * e di database visualizzando messaggi di errore.
     * </p>
     * <p>
     * Effetti visivi: Cambio colore a CARMINE_RED al passaggio del mouse (hover).
     * </p>
     * <p>
     * Eccezioni gestite:
     * <ul>
     *   <li>{@link IllegalStateException} - Se l'utente non ha i permessi (es. è un Giudice).</li>
     *   <li>{@link SQLException} - Se si verifica un errore di database.</li>
     * </ul>
     * </p>
     */
    private void setupCreateTeamListener() {
        rCreateTeamPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    // 1. Chiediamo al Controller se possiamo procedere
                    controller.validateTeamManagementAccess();

                    // 2. Se non lancia eccezioni, procediamo con la GUI
                    String name = JOptionPane.showInputDialog(rootPanel, "Team Name", "New Team", JOptionPane.PLAIN_MESSAGE);
                    if (name != null && !name.trim().isEmpty()) {
                        controller.createTeamAction(name.trim());
                        refreshData();
                    }
                } catch (IllegalStateException ex) {
                    // 3. Catturiamo il divieto polimorfico e lo mostriamo!
                    JOptionPane.showMessageDialog(rootPanel, ex.getMessage(), ACCESS_DENIED, JOptionPane.WARNING_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(rootPanel, "Creation error: " + ex.getMessage(), DB_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rCreateTeamPanel.setBackground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rCreateTeamPanel.setBackground(UIColors.NIGHT_BLUE);
            }
        });
    }

    /**
     * Configura il listener per il pulsante "Join Team".
     * <p>
     * Valida i permessi dell'utente, mostra un dialogo di input per il codice di accesso,
     * e aggiunge l'utente al team se il codice è valido. Gestisce le eccezioni visualizzando
     * messaggi di errore.
     * </p>
     * <p>
     * Eccezioni gestite:
     * <ul>
     *   <li>{@link IllegalStateException} - Se l'utente non ha i permessi.</li>
     *   <li>{@link SQLException} - Se il codice non è valido o si verifica un errore di database.</li>
     * </ul>
     * </p>
     */
    private void setupJoinTeamListener() {
        rJoinTeamPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    controller.validateTeamManagementAccess();

                    String code = JOptionPane.showInputDialog(rootPanel, "Insert Code:", "Join Team", JOptionPane.PLAIN_MESSAGE);
                    if (code != null && !code.trim().isEmpty()) {
                        controller.joinTeamAction(code.trim().toUpperCase());
                        refreshData();
                    }
                } catch (IllegalStateException ex) {
                    JOptionPane.showMessageDialog(rootPanel, ex.getMessage(), ACCESS_DENIED, JOptionPane.WARNING_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(rootPanel, "Incorrect code or server error: " + ex.getMessage(), DB_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * Configura il listener per il pulsante "Add" (caricamento documento).
     * <p>
     * Apre il dialogo di caricamento documento (DocumentUploadDialog) e aggiorna
     * la lista dei documenti caricati al completamento.
     * </p>
     * <p>
     * Effetti visivi: Cambio colore a CARMINE_RED al passaggio del mouse (hover).
     * </p>
     */
    private void setupUploadListener() {
        rAddPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Window parent = SwingUtilities.getWindowAncestor(rootPanel);
                new DocumentUploadDialog((JFrame) parent, controller).setVisible(true);
                refreshData();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rAddPanel.setBackground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rAddPanel.setBackground(UIColors.NIGHT_BLUE);
            }
        });
    }

    /**
     * Configura il listener per il codice di accesso del team.
     * <p>
     * Al clic, copia il codice di accesso negli appunti di sistema e visualizza
     * temporaneamente il messaggio "Copiato!" prima di ripristinare il testo originale.
     * </p>
     * <p>
     * Gestisce la SQLException silenziosamente (nessun feedback all'utente).
     * </p>
     */
    private void setupCopyCodeListener() {
        accessCodeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    Team myTeam = controller.getMyTeam();
                    if (myTeam != null) {
                        String code = myTeam.getAccessCode();
                        StringSelection ss = new StringSelection(code);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
                        String oldText = accessCodeLabel.getText();
                        accessCodeLabel.setText("Copiato!");
                        new Timer(1000, ev -> accessCodeLabel.setText(oldText)).start();
                    }
                } catch (SQLException ex) {
                }
            }
        });
    }

    /**
     * Recupera e visualizza la lista dei membri del team.
     * <p>
     * Svuota il pannello, recupera tutti i partecipanti del team dal database,
     * e crea una card per ciascun membro visualizzando nome e email.
     * </p>
     *
     * @throws SQLException Se si verifica un errore durante il recupero dei dati dal database.
     */
    private void updateMembersList() throws SQLException {
        membersListPanel.removeAll();
        List<Participant> members = controller.getMyTeamMembers();
        for (Participant m : members) {
            JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT));
            card.setBackground(Color.WHITE);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            card.add(new JLabel("Utente: " + m.getName() + " (" + m.getEmail() + ")"));
            membersListPanel.add(card);
        }
    }

    /**
     * Recupera e visualizza la lista dei documenti caricati dal team.
     * <p>
     * Svuota il pannello, recupera tutti i documenti del team dal database,
     * crea una card per ciascun documento con listener per visualizzare i feedback dei giudici.
     * </p>
     *
     * @throws SQLException Se si verifica un errore durante il recupero dei dati dal database.
     */
    private void updateUploadsList() throws SQLException {
        uploadsListPanel.removeAll();
        List<Document> docs = controller.getMyTeamDocuments();

        for (Document d : docs) {
            RoundedPanel card = new RoundedPanel();
            card.setLayout(new BorderLayout());
            card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            card.setBackground(Color.WHITE);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            card.add(new JLabel("Progetto: " + d.getName()), BorderLayout.CENTER);
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    showFeedbackHistory(d);
                }
            });
            uploadsListPanel.add(card);
            uploadsListPanel.add(Box.createVerticalStrut(5));
        }
    }

    /**
     * Visualizza la cronologia dei feedback ricevuti per un documento.
     * <p>
     * Recupera tutti i feedback dei giudici per il documento fornito e li visualizza
     * in un dialogo formattato in HTML con il nome del giudice e il commento.
     * Se nessun feedback è disponibile, mostra un messaggio informativo.
     * </p>
     *
     * @param d Il documento per il quale visualizzare i feedback.
     * @throws SQLException Gestita internamente con visualizzazione di un messaggio di errore.
     */
    private void showFeedbackHistory(Document d) {
        try {
            List<Feedback> feedbacks = controller.getDocumentFeedbacks(d.getDocumentId());
            StringBuilder html = new StringBuilder("<html><body style='font-family: sans-serif; padding: 10px; width: 350px;'>");
            html.append("<h2 style='color: #D32F2F;'>Feedback Giudici:</h2>");

            if (feedbacks.isEmpty()) {
                html.append("<p>Nessun commento disponibile.</p>");
            } else {
                for (Feedback f : feedbacks) {
                    html.append("<h4 style='color: #1A237E;'>Giudice: ").append(f.getJudgeName()).append("</h4>");
                    html.append("<p><i>").append(f.getComment().replace("\n", "<br>")).append("</i></p><hr>");
                }
            }
            html.append("</body></html>");

            JEditorPane pane = new JEditorPane("text/html", html.toString());
            pane.setEditable(false);
            pane.setOpaque(false);
            JOptionPane.showMessageDialog(rootPanel, new JScrollPane(pane), "Storico Feedback", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(rootPanel, "Errore nel caricamento feedback.", DB_ERROR_TITLE, JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Applica stili e colori personalizzati ai componenti UI.
     * <p>
     * Configura:
     * <ul>
     *   <li>Colori dei pulsanti (background e testo) secondo la palette UIColors.</li>
     *   <li>Cursore HAND_CURSOR su tutti i controlli cliccabili.</li>
     *   <li>Bordi arrotondati per i pannelli.</li>
     * </ul>
     * </p>
     */
    private void customizeComponents() {
        rCreateTeamPanel.setBackground(UIColors.NIGHT_BLUE);
        createTeamLabel.setForeground(Color.WHITE);
        rJoinTeamPanel.setBackground(Color.WHITE);
        ((RoundedPanel) rJoinTeamPanel).setBorderColor(UIColors.NIGHT_BLUE);
        joinTeamLabel.setForeground(UIColors.NIGHT_BLUE);
        rAddPanel.setBackground(UIColors.NIGHT_BLUE);
        addLabel.setForeground(Color.WHITE);

        rCreateTeamPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rJoinTeamPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rAddPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        accessCodeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Crea i componenti UI personalizzati.
     * Inizializza i pannelli arrotondati (RoundedPanel) per i pulsanti
     * "Create Team", "Join Team" e "Add".
     */
    private void createUIComponents() {
        rCreateTeamPanel = new RoundedPanel();
        rJoinTeamPanel = new RoundedPanel();
        rAddPanel = new RoundedPanel();
    }

    /**
     * Configura il pannello di scorrimento con layout verticale.
     * <p>
     * Rimuove il bordo del pannello e imposta il layout dei pannelli interni
     * (membersListPanel e uploadsListPanel) a BoxLayout verticale.
     * </p>
     */
    private void setupScrollPanel() {
        scrollPanel.setBorder(null);
        membersListPanel.setLayout(new BoxLayout(membersListPanel, BoxLayout.Y_AXIS));
        uploadsListPanel.setLayout(new BoxLayout(uploadsListPanel, BoxLayout.Y_AXIS));
    }

    /**
     * Restituisce il pannello radice della classe.
     *
     * @return Il JPanel principale contenente tutta la UI del pannello di gestione Team.
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
        rootPanel.setLayout(new GridLayoutManager(1, 1, new Insets(20, 20, 20, 20), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        rootPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, BorderLayout.NORTH);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        teamLabel = new JLabel();
        Font teamLabelFont = this.$$$getFont$$$(null, -1, 26, teamLabel.getFont());
        if (teamLabelFont != null) teamLabel.setFont(teamLabelFont);
        teamLabel.setText("Team");
        panel4.add(teamLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        infoLabel = new JLabel();
        infoLabel.setText("You're currently not part of a team.");
        panel6.add(infoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        rJoinTeamPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel5.add(rJoinTeamPanel, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, 30), new Dimension(100, 30), new Dimension(100, 30), 0, false));
        joinTeamLabel = new JLabel();
        joinTeamLabel.setText("Join Team");
        rJoinTeamPanel.add(joinTeamLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rCreateTeamPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel5.add(rCreateTeamPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, 30), new Dimension(100, 30), new Dimension(100, 30), 0, false));
        createTeamLabel = new JLabel();
        createTeamLabel.setText("Create Team");
        rCreateTeamPanel.add(createTeamLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel7, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel7.add(separator1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel8, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        accessCodeLabel = new JLabel();
        accessCodeLabel.setText("Access Code:");
        panel8.add(accessCodeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel8.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        scrollPanel = new JScrollPane();
        scrollPanel.setVerticalScrollBarPolicy(20);
        panel1.add(scrollPanel, BorderLayout.CENTER);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(8, 1, new Insets(0, 0, 0, 0), -1, -1));
        scrollPanel.setViewportView(panel9);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 2, new Insets(10, 0, 10, 0), -1, -1));
        panel9.add(panel10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(panel11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        membersLabel = new JLabel();
        Font membersLabelFont = this.$$$getFont$$$(null, -1, 18, membersLabel.getFont());
        if (membersLabelFont != null) membersLabel.setFont(membersLabelFont);
        membersLabel.setText("Members");
        panel11.add(membersLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel10.add(spacer4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel9.add(spacer5, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        membersInfoJPanel = new JPanel();
        membersInfoJPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(membersInfoJPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        membersInfoLabel = new JLabel();
        membersInfoLabel.setText("Create or join a team to view its members!");
        membersInfoJPanel.add(membersInfoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 3, new Insets(10, 0, 10, 0), -1, -1));
        panel9.add(panel12, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel12.add(panel13, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        uploadsLabel = new JLabel();
        Font uploadsLabelFont = this.$$$getFont$$$(null, -1, 18, uploadsLabel.getFont());
        if (uploadsLabelFont != null) uploadsLabel.setFont(uploadsLabelFont);
        uploadsLabel.setText("Uploads");
        panel13.add(uploadsLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel12.add(spacer6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        rAddPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel12.add(rAddPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, 30), new Dimension(100, 30), new Dimension(100, 30), 0, false));
        addLabel = new JLabel();
        addLabel.setText("Add");
        rAddPanel.add(addLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel14, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        uploadsInfoLabel = new JLabel();
        uploadsInfoLabel.setText("Create or join a team to view or add your progress!");
        panel14.add(uploadsInfoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        uploadsListPanel = new JPanel();
        uploadsListPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(uploadsListPanel, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel15, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel15.add(panel16, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        final Spacer spacer7 = new Spacer();
        panel15.add(spacer7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        membersListPanel = new JPanel();
        membersListPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(membersListPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
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


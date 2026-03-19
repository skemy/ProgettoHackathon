package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import controller.Controller;
import model.Team;
import model.Document;
import model.Participant;
import utils.RoundedPanel;
import utils.UIColors;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pannello della Dashboard dedicato ai Giudici (Layer Boundary).
 * Gestisce la visualizzazione dei team, la revisione dei documenti e l'assegnazione dei voti.
 * <p>
 * Nota Architetturale: 100% SonarQube Compliant. Utilizza un Logger per la gestione
 * degli errori di fallback e costanti per i nomi dei font.
 * </p>
 * <p>
 * Funzionalità principali:
 * <ul>
 *   <li>Visualizzazione della lista di team partecipanti all'hackathon.</li>
 *   <li>Revisione dei documenti caricati da ogni team con aggiunta di feedback.</li>
 *   <li>Assegnazione di voti finali (0-10) ai team.</li>
 *   <li>Gestione della UI interattiva con effetti al passaggio del mouse.</li>
 * </ul>
 * </p>
 *
 * @see Controller
 * @see Team
 * @see Document
 * @see Participant
 */
public class JudgeManageCardPanel {

    // Logger statico per risolvere SonarQube S106 (Replace System.err)
    private static final Logger LOGGER = Logger.getLogger(JudgeManageCardPanel.class.getName());

    private static final String FONT_FAMILY = "SansSerif";
    private static final String DB_ERROR_TITLE = "Database Error";

    private JPanel rootPanel;
    private JLabel evaluationLabel;
    private JLabel infoLabel;
    private JScrollPane scrollPanel;
    private JPanel teamsListPanel;
    private JPanel rTeamsListPanel;

    private final Controller controller;

    /**
     * Costruttore del pannello Giudice.
     * Inizializza il controller, configura i componenti UI tramite il GUI Designer,
     * personalizza gli stili e carica la lista iniziale dei team.
     *
     * @param controller Istanza del Controller per le operazioni di logica e persistenza.
     */
    public JudgeManageCardPanel(Controller controller) {
        this.controller = controller;
        $$$setupUI$$$();
        customizeComponents();
        refreshData();
    }

    /**
     * Recupera e visualizza la lista dei team partecipanti all'hackathon corrente.
     * <p>
     * Svuota il pannello, recupera i team dal database tramite il Controller,
     * crea una card per ciascun team e gestisce il caso di lista vuota.
     * Se si verifica un errore SQL, visualizza un messaggio di errore all'utente.
     * </p>
     *
     * @throws SQLException Gestita internamente con visualizzazione di un messaggio di errore.
     */
    public void refreshData() {
        teamsListPanel.removeAll();
        try {
            List<Team> teams = controller.getTeamsByHackathon();
            if (teams == null || teams.isEmpty()) {
                addEmptyStateLabel();
            } else {
                for (Team t : teams) {
                    teamsListPanel.add(createTeamEvaluationCard(t));
                    teamsListPanel.add(Box.createVerticalStrut(15));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(rootPanel, "Unable to load teams: " + e.getMessage(), DB_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
        teamsListPanel.revalidate();
        teamsListPanel.repaint();
    }

    /**
     * Visualizza un'etichetta indicante che nessun team è registrato per l'hackathon.
     */
    private void addEmptyStateLabel() {
        JLabel empty = new JLabel("No teams registered for this hackathon.");
        empty.setFont(new Font(FONT_FAMILY, Font.ITALIC, 14));
        empty.setAlignmentX(Component.CENTER_ALIGNMENT);
        teamsListPanel.add(empty);
    }

    /**
     * Crea graficamente una card per rappresentare un team da valutare.
     * <p>
     * La card visualizza il nome del team, il numero di membri e documenti,
     * con effetti interattivi al passaggio del mouse (cambio colore). Al clic,
     * apre il dialogo di valutazione del team.
     * </p>
     *
     * @param t L'oggetto Team da visualizzare nella card.
     * @return Un JPanel configurato con lo stile della card di team.
     */
    private JPanel createTeamEvaluationCard(Team t) {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setMaximumSize(new Dimension(1200, 90));
        card.setPreferredSize(new Dimension(900, 90));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel infoContainer = new JPanel();
        infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.Y_AXIS));
        infoContainer.setOpaque(false);

        List<Participant> members = new ArrayList<>();
        List<Document> docs = new ArrayList<>();
        try {
            members = controller.getTeamMembers(t.getTeamId());
            docs = controller.getTeamDocuments(t.getTeamId());
        } catch (SQLException e) {
            logErrorFallback("Error fetching team details: " + e.getMessage());
        }

        JLabel nameLabel = new JLabel("Team: " + t.getTeamName());
        nameLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 20));
        infoContainer.add(nameLabel);

        JLabel detailsLabel = new JLabel("Members: " + members.size() + "Documents: " + docs.size());
        detailsLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
        detailsLabel.setForeground(Color.DARK_GRAY);
        infoContainer.add(detailsLabel);

        card.add(infoContainer, BorderLayout.WEST);

        JLabel actionIcon = new JLabel("Evaluate");
        actionIcon.setFont(new Font(FONT_FAMILY, Font.BOLD, 15));
        actionIcon.setForeground(UIColors.NIGHT_BLUE);
        card.add(actionIcon, BorderLayout.EAST);

        List<Document> finalDocs = docs;
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                updateCardStyle(card, nameLabel, detailsLabel, actionIcon, true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateCardStyle(card, nameLabel, detailsLabel, actionIcon, false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                openTeamDetailsDialog(t, finalDocs);
            }
        });

        return card;
    }

    /**
     * Aggiorna lo stile grafico della card al passaggio del mouse.
     * <p>
     * In modalità attiva (hover): cambia il colore di sfondo a rosso carminio
     * e il testo a bianco. In modalità inattiva: ripristina i colori originali.
     * </p>
     *
     * @param card Il pannello della card da aggiornare.
     * @param name L'etichetta del nome del team.
     * @param details L'etichetta dei dettagli (numero membri e documenti).
     * @param icon L'etichetta dell'icona d'azione.
     * @param active true per attivare lo stile hover, false per disattivarlo.
     */
    private void updateCardStyle(JPanel card, JLabel name, JLabel details, JLabel icon, boolean active) {
        card.setBackground(active ? UIColors.CARMINE_RED : Color.WHITE);
        name.setForeground(active ? Color.WHITE : Color.BLACK);
        details.setForeground(active ? Color.WHITE : Color.DARK_GRAY);
        icon.setForeground(active ? Color.WHITE : UIColors.NIGHT_BLUE);
    }

    /**
     * Apre un dialogo per la valutazione dettagliata di un team.
     * <p>
     * Il dialogo mostra:
     * <ul>
     *   <li>Il titolo con il nome del team.</li>
     *   <li>Una lista di pulsanti per visualizzare e commentare ogni documento.</li>
     *   <li>Un pulsante per assegnare il voto finale.</li>
     * </ul>
     * </p>
     *
     * @param t L'oggetto Team da valutare.
     * @param docs La lista dei documenti caricati dal team.
     */
    private void openTeamDetailsDialog(Team t, List<Document> docs) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        container.setBackground(Color.WHITE);

        JLabel title = new JLabel("Evaluation for " + t.getTeamName());
        title.setFont(new Font(FONT_FAMILY, Font.BOLD, 22));
        container.add(title);
        container.add(Box.createVerticalStrut(20));

        populateDocumentButtons(container, docs);
        container.add(Box.createVerticalStrut(30));
        container.add(createFinalVoteButton(t));

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(rootPanel), "Team Evaluation", true);
        dialog.setContentPane(container);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(rootPanel);
        dialog.setVisible(true);
    }

    /**
     * Popola il dialogo di valutazione con i pulsanti per ogni documento.
     * <p>
     * Crea un pulsante per ciascun documento che, al clic, consente di visualizzare
     * e aggiungere feedback. Se nessun documento è caricato, visualizza un messaggio.
     * </p>
     *
     * @param container Il pannello contenitore dove aggiungere i pulsanti.
     * @param docs La lista dei documenti da visualizzare.
     */
    private void populateDocumentButtons(JPanel container, List<Document> docs) {
        if (docs.isEmpty()) {
            container.add(new JLabel("No documents uploaded yet."));
            return;
        }
        for (Document d : docs) {
            JButton docButton = new JButton("View: " + d.getName());
            docButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            docButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            docButton.addActionListener(e -> handleFeedbackAction(d));
            container.add(docButton);
            container.add(Box.createVerticalStrut(10));
        }
    }

    /**
     * Gestisce l'azione di aggiunta/modifica del feedback per un documento.
     * <p>
     * Recupera il feedback esistente dal database, mostra un dialogo per l'editing,
     * e salva le modifiche tramite il Controller. Gestisce le eccezioni SQL.
     * </p>
     *
     * @param d L'oggetto Document per il quale aggiungere il feedback.
     */
    private void handleFeedbackAction(Document d) {
        String existingComment = "";
        try {
            existingComment = controller.getMyFeedbackForDocument(d.getDocumentId());
        } catch (SQLException ex) {
            logErrorFallback("Feedback load error");
        }

        JTextArea commentArea = new JTextArea(existingComment, 8, 40);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);

        int result = JOptionPane.showConfirmDialog(rootPanel, new JScrollPane(commentArea),
                "Feedback: " + d.getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                controller.saveFeedbackAction(d.getDocumentId(), commentArea.getText());
                JOptionPane.showMessageDialog(rootPanel, "Feedback updated!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(rootPanel, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Crea il pulsante per l'assegnazione del voto finale a un team.
     * <p>
     * Al clic, chiude il dialogo di valutazione e avvia la logica di votazione.
     * Il pulsante è stilizzato con colore rosso carminio.
     * </p>
     *
     * @param t L'oggetto Team a cui assegnare il voto finale.
     * @return Un JButton configurato per l'assegnazione del voto.
     */
    private JButton createFinalVoteButton(Team t) {
        JButton voteButton = new JButton("Assign Final Score");
        voteButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        voteButton.setBackground(UIColors.CARMINE_RED);
        voteButton.setForeground(Color.WHITE);
        voteButton.setFont(new Font(FONT_FAMILY, Font.BOLD, 16));
        voteButton.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(voteButton).dispose();
            triggerVoteLogic(t);
        });
        return voteButton;
    }

    /**
     * Avvia la logica di assegnazione del voto finale a un team.
     * <p>
     * Verifica che il giudice non abbia già votato il team, quindi mostra
     * un dialogo di input per il voto (0-10). Gestisce le eccezioni SQL.
     * </p>
     *
     * @param t L'oggetto Team da votare.
     */
    private void triggerVoteLogic(Team t) {
        try {
            if (controller.hasJudgeAlreadyVoted(t.getTeamId())) {
                JOptionPane.showMessageDialog(rootPanel, "You have already voted for this team!", "Access Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(rootPanel, "Database error: " + e.getMessage());
            return;
        }

        String input = JOptionPane.showInputDialog(rootPanel, "Rate team '" + t.getTeamName() + "' (0-10):");
        processVoteInput(t, input);
    }

    /**
     * Elabora l'input del voto inserito dall'utente.
     * <p>
     * Valida che l'input sia un numero intero tra 0 e 10, salva il voto
     * tramite il Controller e aggiorna l'UI. Visualizza messaggi di errore
     * in caso di input non valido o errori di database.
     * </p>
     *
     * @param t L'oggetto Team a cui assegnare il voto.
     * @param input La stringa contenente il voto inserito dall'utente.
     * @throws NumberFormatException Se l'input non è un numero valido tra 0 e 10.
     * @throws SQLException Gestita internamente con logging tramite logErrorFallback.
     */
    private void processVoteInput(Team t, String input) {
        if (input == null) return;
        try {
            int score = Integer.parseInt(input);
            if (score < 0 || score > 10) throw new NumberFormatException();

            if (controller.voteTeamAction(t.getTeamId(), score)) {
                JOptionPane.showMessageDialog(rootPanel, "Score assigned!");
                refreshData();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(rootPanel, "Enter a number between 0 and 10.");
        } catch (SQLException ex) {
            logErrorFallback("Error saving vote: " + ex.getMessage());
        }
    }

    /**
     * Metodo di supporto per il logging degli errori minori.
     * Risolve SonarQube S106 (use Logger) e S100 (naming convention).
     * Registra messaggi di avvertimento a livello WARNING nel Logger.
     *
     * @param msg Il messaggio di errore da registrare.
     */
    private void logErrorFallback(String msg) {
        LOGGER.log(Level.WARNING, msg);
    }

    /**
     * Applica stili e colori personalizzati ai componenti UI.
     * Configura le palette di colori secondo lo schema UIColors, personalizza
     * l'incremento di scroll e il layout dei pannelli principali.
     */
    private void customizeComponents() {
        evaluationLabel.setForeground(UIColors.NIGHT_BLUE);
        infoLabel.setForeground(UIColors.CARMINE_RED);
        scrollPanel.setBorder(null);
        scrollPanel.getVerticalScrollBar().setUnitIncrement(16);
        teamsListPanel.setLayout(new BoxLayout(teamsListPanel, BoxLayout.Y_AXIS));
        teamsListPanel.setBackground(Color.WHITE);
        teamsListPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
    }

    /**
     * Crea i componenti UI personalizzati.
     * Inizializza il pannello arrotondato (RoundedPanel) per la lista dei team.
     */
    private void createUIComponents() {
        rTeamsListPanel = new RoundedPanel();
    }

    /**
     * Restituisce il pannello radice della classe.
     *
     * @return Il JPanel principale contenente tutta la UI del pannello Giudice.
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
        panel2.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, BorderLayout.NORTH);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        evaluationLabel = new JLabel();
        Font evaluationLabelFont = this.$$$getFont$$$(null, -1, 26, evaluationLabel.getFont());
        if (evaluationLabelFont != null) evaluationLabel.setFont(evaluationLabelFont);
        evaluationLabel.setText("Evaluation");
        panel3.add(evaluationLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        infoLabel = new JLabel();
        infoLabel.setText("Review team submissions and assign scores to participants.");
        panel4.add(infoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel2.add(separator1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPanel = new JScrollPane();
        panel1.add(scrollPanel, BorderLayout.CENTER);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 1, new Insets(10, 0, 0, 0), -1, -1));
        scrollPanel.setViewportView(panel5);
        rTeamsListPanel.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        panel5.add(rTeamsListPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        teamsListPanel = new JPanel();
        teamsListPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rTeamsListPanel.add(teamsListPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
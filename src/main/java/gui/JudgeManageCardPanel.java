package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import controller.Controller;
import model.Document;
import model.Hackathon;
import model.Team;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pannello della Dashboard dedicato ai Giudici (Layer Boundary).
 * Gestisce la visualizzazione dei team, la revisione dei documenti e l'assegnazione dei voti.
 * <p>
 * Implementa una logica di blocco temporale e un design estetico fluido,
 * adattando la visualizzazione allo stato dell'evento (attivo vs bloccato).
 * </p>
 *
 * @see Controller
 * @see Team
 * @see Document
 * @see Hackathon
 */
public class JudgeManageCardPanel {

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
     * Costruttore del pannello di gestione per i Giudici.
     *
     * @param controller L'istanza del Controller per le operazioni di logica e persistenza.
     */
    public JudgeManageCardPanel(Controller controller) {
        this.controller = controller;
        $$$setupUI$$$();
        customizeComponents();
        refreshData();
    }

    /**
     * Sincronizza l'interfaccia con i dati del database in base allo stato temporale dell'Hackathon.
     */
    public void refreshData() {
        teamsListPanel.removeAll();
        try {
            Hackathon h = controller.getCurrentHackathon();
            boolean canEvaluate = (h != null && h.isStarted() && !h.isEnded());

            if (!canEvaluate) {
                showLockedState();

                if (h != null && !h.isStarted()) {
                    infoLabel.setText("Evaluation phase starts on: " + h.getStartDate().toLocalDate());
                    infoLabel.setForeground(Color.ORANGE);
                } else {
                    infoLabel.setText("The event is currently closed or unavailable.");
                    infoLabel.setForeground(Color.GRAY);
                }
            } else {
                List<Team> teams = controller.getTeamsByHackathon();
                if (teams == null || teams.isEmpty()) {
                    addEmptyStateLabel();
                } else {
                    for (Team t : teams) {
                        teamsListPanel.add(createTeamEvaluationCard(t));
                        teamsListPanel.add(Box.createVerticalStrut(15));
                    }
                }
                infoLabel.setText("Review team submissions and assign scores.");
                infoLabel.setForeground(UIColors.CARMINE_RED);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(rootPanel, "Unable to load teams: " + e.getMessage(),
                    DB_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
        teamsListPanel.revalidate();
        teamsListPanel.repaint();
    }

    /**
     * Genera e visualizza una singola card di blocco (Lucchetto Giallo).
     * Sfrutta un RoundedPanel bianco su sfondo grigio per risaltare.
     */
    private void showLockedState() {
        RoundedPanel lockedCard = new RoundedPanel();
        lockedCard.setLayout(new BoxLayout(lockedCard, BoxLayout.Y_AXIS));
        lockedCard.setBackground(Color.WHITE);
        lockedCard.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
        lockedCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel lockIcon = new JLabel("🔒");
        lockIcon.setFont(new Font(FONT_FAMILY, Font.PLAIN, 56));
        lockIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lockedMsg = new JLabel("Function not available");
        lockedMsg.setFont(new Font(FONT_FAMILY, Font.BOLD, 22));
        lockedMsg.setForeground(Color.DARK_GRAY);
        lockedMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

        lockedCard.add(lockIcon);
        lockedCard.add(Box.createVerticalStrut(20));
        lockedCard.add(lockedMsg);

        // Centriamo la card all'interno della lista
        teamsListPanel.add(Box.createVerticalGlue());
        teamsListPanel.add(lockedCard);
        teamsListPanel.add(Box.createVerticalGlue());
    }

    /**
     * Visualizza un'etichetta testuale indicante l'assenza di team registrati.
     */
    private void addEmptyStateLabel() {
        JLabel empty = new JLabel("No teams registered for this hackathon.");
        empty.setFont(new Font(FONT_FAMILY, Font.ITALIC, 16));
        empty.setAlignmentX(Component.CENTER_ALIGNMENT);
        teamsListPanel.add(empty);
    }

    /**
     * Crea dinamicamente una card grafica elegante e interattiva per un team specifico.
     * Ora progettata per espandersi per l'intera larghezza disponibile.
     */
    private JPanel createTeamEvaluationCard(Team t) {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(15, 10));
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        card.setBackground(Color.WHITE);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Permettiamo alla card di espandersi in larghezza ma fissiamo l'altezza massima
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(t.getTeamName());
        nameLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 18));
        nameLabel.setForeground(UIColors.NIGHT_BLUE);

        String memberCountTxt = "Members: ?";
        try {
            int members = controller.getTeamMembers(t.getTeamId()).size();
            memberCountTxt = "Members: " + members;
        } catch (Exception e) {
            logErrorFallback("Could not load member count for team " + t.getTeamId());
        }

        JLabel detailsLabel = new JLabel(memberCountTxt);
        detailsLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 14));
        detailsLabel.setForeground(Color.DARK_GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(detailsLabel);
        card.add(infoPanel, BorderLayout.CENTER);

        JLabel actionIcon = new JLabel("Evaluate ➔");
        actionIcon.setFont(new Font(FONT_FAMILY, Font.BOLD, 16));
        actionIcon.setForeground(UIColors.CARMINE_RED);
        card.add(actionIcon, BorderLayout.EAST);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    List<Document> docs = controller.getTeamDocuments(t.getTeamId());
                    openTeamDetailsDialog(t, docs);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(rootPanel, "Error loading docs.", DB_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                updateCardStyle(card, nameLabel, detailsLabel, actionIcon, true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateCardStyle(card, nameLabel, detailsLabel, actionIcon, false);
            }
        });

        return card;
    }

    private void updateCardStyle(JPanel card, JLabel name, JLabel details, JLabel icon, boolean active) {
        card.setBackground(active ? UIColors.CARMINE_RED : Color.WHITE);
        name.setForeground(active ? Color.WHITE : UIColors.NIGHT_BLUE);
        details.setForeground(active ? Color.WHITE : Color.DARK_GRAY);
        icon.setForeground(active ? Color.WHITE : UIColors.CARMINE_RED);
    }

    private void openTeamDetailsDialog(Team t, List<Document> docs) {
        // [Metodo invariato per brevità e sicurezza]
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
     * Gestisce l'interazione con un singolo documento del team.
     * <p>
     * Mostra un pannello contenente l'URL (copiabile negli appunti) e un'area di testo
     * per inserire o modificare il feedback del giudice. In caso di conferma,
     * persiste il commento tramite il {@link Controller}.
     * </p>
     *
     * @param d Il documento oggetto della valutazione.
     */
    private void handleFeedbackAction(Document d) {
        // [Metodo invariato]
        String existingComment = "";
        try {
            existingComment = controller.getMyFeedbackForDocument(d.getDocumentId());
        } catch (SQLException ex) {
            logErrorFallback("Feedback load error");
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel linkLabel = new JLabel("<html>URL: <a href=''>" + d.getUrl() + "</a></html>");
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.setToolTipText("Click on the link");

        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                StringSelection selection = new StringSelection(d.getUrl());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                JOptionPane.showMessageDialog(rootPanel, "Link copied to clipboard!");
            }
        });

        panel.add(linkLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Your Feedback:"));

        JTextArea commentArea = new JTextArea(existingComment, 8, 40);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(commentArea));

        int result = JOptionPane.showConfirmDialog(rootPanel, panel,
                "Evaluation: " + d.getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                controller.saveFeedbackAction(d.getDocumentId(), commentArea.getText());
                JOptionPane.showMessageDialog(rootPanel, "Feedback updated!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(rootPanel, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

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
     * Avvia la procedura di assegnazione del punteggio finale a un team.
     * <p>
     * Verifica preventivamente se il giudice ha già espresso un voto per evitare duplicati.
     * In caso negativo, apre un input dialog per acquisire il valore numerico.
     * </p>
     *
     * @param t Il team da votare.
     */
    private void triggerVoteLogic(Team t) {
        try {
            if (controller.hasJudgeAlreadyVoted(t.getTeamId())) {
                JOptionPane.showMessageDialog(rootPanel, "You have already voted for this team!", "Access Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(rootPanel, "Database error: " + e.getMessage(), DB_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }

        String input = JOptionPane.showInputDialog(rootPanel, "Rate team '" + t.getTeamName() + "' (0-10):");
        processVoteInput(t, input);
    }

    /**
     * Valida e processa l'input testuale ricevuto dal dialogo di voto.
     * <p>
     * Converte la stringa in float e verifica che il range sia compreso tra 0 e 10.
     * Gestisce i feedback visivi in caso di formato numerico errato o errori SQL.
     * </p>
     *
     * @param t     Il team a cui assegnare il voto.
     * @param input La stringa inserita dall'utente nel {@link JOptionPane}.
     */
    private void processVoteInput(Team t, String input) {
        if (input == null) return;
        try {
            float score = Float.parseFloat(input);
            if (score < 0 || score > 10) throw new NumberFormatException();

            if (controller.voteTeamAction(t.getTeamId(), score)) {
                JOptionPane.showMessageDialog(rootPanel, "Score assigned!");
                refreshData();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(rootPanel, "Enter a number between 0 and 10");
        } catch (SQLException ex) {
            logErrorFallback("Error saving vote: " + ex.getMessage());
        }
    }

    private void logErrorFallback(String msg) {
        LOGGER.log(Level.WARNING, msg);
    }

    /**
     * Applica stili e colori. Bypassa i limiti del GUI Designer per forzare
     * l'estensione orizzontale e rimuovere gli sfondi bianchi superflui.
     */
    private void customizeComponents() {
        evaluationLabel.setForeground(UIColors.NIGHT_BLUE);
        infoLabel.setForeground(UIColors.CARMINE_RED);

        scrollPanel.setBorder(null);
        scrollPanel.getVerticalScrollBar().setUnitIncrement(16);

        // --- RIMOZIONE DEGLI SFONDI BIANCHI ---
        // Rendiamo trasparenti i contenitori così ereditano il grigio del MainFrame
        scrollPanel.getViewport().setOpaque(false);
        teamsListPanel.setOpaque(false);
        rTeamsListPanel.setOpaque(false);

        // --- FORZATURA ESTENSIONE LARGHEZZA ---
        // Scavalchiamo il GridConstraints sostituendo il layout programmaticamente
        rTeamsListPanel.setLayout(new BorderLayout());
        rTeamsListPanel.add(teamsListPanel, BorderLayout.CENTER);

        teamsListPanel.setLayout(new BoxLayout(teamsListPanel, BoxLayout.Y_AXIS));

        // Riduciamo il padding destro/sinistro per permettere alle card di espandersi
        teamsListPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 20, 5));
    }

    private void createUIComponents() {
        rTeamsListPanel = new RoundedPanel();
    }

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
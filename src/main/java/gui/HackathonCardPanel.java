package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import controller.Controller;
import model.Hackathon;
import utils.RoundedPanel;
import utils.UIColors;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Pannello della Dashboard dedicato alla visualizzazione dei dettagli di un Hackathon.
 * Permette agli organizzatori di modificare il Problem Statement e pubblicare le classifiche.
 * <p>
 * Nota Architetturale: Gestisce internamente le SQLException propagate dal Layer Control,
 * garantendo un feedback visivo all'utente e rispettando il pattern Boundary.
 */
@SuppressWarnings("java:S1450") // Sopprime il warning per i campi generati dal GUI Designer
public class HackathonCardPanel {

    private static final String ERROR_TITLE = "Database Error";

    private JPanel rootPanel;
    private JLabel hackathonLabel;
    private JLabel infoLabel;
    private JScrollPane scrollPanel;
    private JLabel overviewLabel;
    private JPanel rTitlePanel;
    private JLabel titleLabel;
    private JPanel rTitleContentPanel;
    private JPanel rLocationPanel;
    private JLabel locationLabel;
    private JPanel rLocationContentPanel;
    private JLabel titleContentLabel;
    private JLabel locationContentLabel;
    private JPanel rStartDatePanel;
    private JLabel startDateLabel;
    private JPanel rStartDateContentPanel;
    private JLabel startDateContentLabel;
    private JPanel rEndDatePanel;
    private JLabel endDateLabel;
    private JPanel rEndDateContentPanel;
    private JLabel endDateContentLabel;
    private JPanel rDeadlinePanel;
    private JLabel deadlineLabel;
    private JPanel rDeadlineContentPanel;
    private JLabel deadlineContentLabel;
    private JPanel rOrganizerPanel;
    private JLabel organizerLabel;
    private JPanel rOrganizerContentPanel;
    private JLabel organizerContentLabel;
    private JLabel problemStatementLabel;
    private JPanel rEditPanel;
    private JLabel editLabel;
    private JTextArea problemStatementTextArea;
    private JLabel rankingLabel;
    private JLabel publishLabel;
    private JPanel rPublishPanel;
    private JLabel rankingInfoLabel;
    private JPanel rankingListPanel;
    private JLabel maxParticipantsLabel;
    private JLabel maxTeamSizeLabel;
    private JPanel rMaxParticipantsPanel;
    private JPanel rMaxTeamSizePanel;
    private JPanel rMaxParticipantsContentPanel;
    private JPanel rMaxTeamSizeContentPanel;
    private JLabel maxParticipantsContentLabel;
    private JLabel maxTeamSizeContentLabel;
    private static final String SAMPLE_TEXT = "sample_text";

    private final Controller controller;
    private boolean isEditingMode = false;

    /**
     * Costruttore del pannello.
     *
     * @param controller Istanza del Controller per le operazioni di logica.
     */
    public HackathonCardPanel(Controller controller) {
        this.controller = controller;
        $$$setupUI$$$();
        customizeComponents();
        setupEditLogic();
        setupRankingLogic();
        refreshData();
    }

    private String getRankingRulesHtml() {
        return "<html><div style='text-align: left; color: gray; font-size: 10px; margin-bottom: 5px; width: 100%;'>" +
                "<b>Ranking Criteria:</b><br>" +
                "1. Highest Average Score.<br>" +
                "2. Total Documents Uploaded (Tie-breaker).<br>" +
                "3. Alphabetical Order.<br>" +
                "<i>Note: Teams without uploaded documents receive a score of 0.</i></div></html>";
    }

    /**
     * Aggiorna i dati mostrati a schermo recuperando lo stato attuale dell'Hackathon.
     * Gestisce la 'SQLException' del Controller.
     */
    public void refreshData() { // Rimosso parametro inutilizzato (Sonar S1172)
        try {
            Hackathon current = controller.getCurrentHackathon();
            if (current == null) {
                infoLabel.setText("You are currently not registered for an event.");
                clearFields();
                rPublishPanel.setVisible(false);
                rankingListPanel.removeAll();
                return;
            }

            infoLabel.setText("Active Event: " + current.getTitle());
            populateFields(current);
            problemStatementTextArea.setText((current.getProblemDescription() == null || current.getProblemDescription().isBlank()) ?
                    "The problem statement is currently not available." : current.getProblemDescription());

            rankingListPanel.removeAll();
            if (rankingInfoLabel != null) rankingInfoLabel.setVisible(false);

            boolean isEventOver = LocalDateTime.now().isAfter(current.getEndDate());
            if (isEventOver) {
                rPublishPanel.setVisible(false);
                loadRanking(true);
            } else if (controller.isCurrentUserOrganizer()) {
                rPublishPanel.setVisible(true);
                publishLabel.setText("Refresh Live Ranking");
                loadRanking(false);
            } else {
                rPublishPanel.setVisible(false);
                JLabel pendingLabel = new JLabel("<html><i>The event is still ongoing. Results pending.</i></html>");
                pendingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                rankingListPanel.add(pendingLabel);
            }

            disableEditingUI();
            rankingListPanel.revalidate();
            rankingListPanel.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(rootPanel, "Unable to load hackathon data: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void customizeComponents() {
        scrollPanel.setBorder(null);
        scrollPanel.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));

        rEditPanel.setBackground(UIColors.NIGHT_BLUE);
        editLabel.setForeground(Color.WHITE);
        rPublishPanel.setBackground(UIColors.NIGHT_BLUE);
        publishLabel.setForeground(Color.WHITE);

        problemStatementTextArea.setLineWrap(true);
        problemStatementTextArea.setWrapStyleWord(true);
        problemStatementTextArea.setOpaque(false);

        hackathonLabel.setForeground(UIColors.NIGHT_BLUE);
        infoLabel.setForeground(UIColors.CARMINE_RED);
        overviewLabel.setForeground(UIColors.CARMINE_RED);
        rankingLabel.setForeground(UIColors.CARMINE_RED);

        rankingListPanel.setLayout(new BoxLayout(rankingListPanel, BoxLayout.Y_AXIS));
        rankingListPanel.setBackground(Color.WHITE);
    }

    private void disableEditingUI() {
        isEditingMode = false;
        problemStatementTextArea.setEditable(false);
        editLabel.setText("Edit");
        rEditPanel.setBackground(UIColors.NIGHT_BLUE);
    }

    private void setupEditLogic() {
        rEditPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!controller.isCurrentUserOrganizer()) {
                    JOptionPane.showMessageDialog(rootPanel, "Only the Organizer can modify the Problem Statement.", "Privilege Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                handleEditToggle();
            }
        });
    }

    private void handleEditToggle() {
        if (!isEditingMode) {
            // Entra in modalità modifica
            isEditingMode = true;
            problemStatementTextArea.setEditable(true);
            problemStatementTextArea.setBackground(Color.WHITE);
            problemStatementTextArea.setOpaque(true);
            editLabel.setText("Save");
            rEditPanel.setBackground(new Color(46, 204, 113)); // Verde
        } else {
            // Tenta di salvare le modifiche
            if (JOptionPane.showConfirmDialog(rootPanel, "Save new Problem Statement?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    if (controller.updateHackathonProblem(problemStatementTextArea.getText())) {
                        disableEditingUI();
                        problemStatementTextArea.setOpaque(false);
                        JOptionPane.showMessageDialog(rootPanel, "Statement updated successfully!");
                    }
                } catch (SQLException ex) {
                    // Errore tecnico del database
                    JOptionPane.showMessageDialog(rootPanel, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalStateException ex) {
                    // ERRORE DI LOGICA (Evento terminato) - Ora gestito correttamente!
                    JOptionPane.showMessageDialog(rootPanel, ex.getMessage(), "Action Denied", JOptionPane.WARNING_MESSAGE);
                    disableEditingUI(); // Esci dalla modalità edit poiché non permessa
                }
            }
        }
    }

    private void setupRankingLogic() {
        rPublishPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!controller.isCurrentUserOrganizer()) return;
                rankingListPanel.removeAll();
                loadRanking(false);
                rankingListPanel.revalidate();
                rankingListPanel.repaint();
            }
        });
    }

    private void loadRanking(boolean isFinal) {
        JLabel rulesLabel = new JLabel(getRankingRulesHtml());
        rulesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rankingListPanel.add(rulesLabel);
        rankingListPanel.add(Box.createVerticalStrut(10));

        try {
            List<String> rankedTeams = isFinal ? controller.getFinalRanking() : controller.getLiveRankingForOrganizer();
            if (rankedTeams != null && !rankedTeams.isEmpty()) {
                for (String rankRow : rankedTeams) {
                    RoundedPanel card = createRankingCard(rankRow);
                    card.setAlignmentX(Component.LEFT_ALIGNMENT);
                    rankingListPanel.add(card);
                    rankingListPanel.add(Box.createVerticalStrut(5));
                }
            }
        } catch (SQLException ex) {
            rankingListPanel.add(new JLabel("<html><i>Database Error: " + ex.getMessage() + "</i></html>"));
        } catch (IllegalStateException ex) {
            rankingListPanel.add(new JLabel("<html><i>" + ex.getMessage() + "</i></html>"));
        }
    }

    private void populateFields(Hackathon h) throws SQLException {
        titleContentLabel.setText(h.getTitle());
        locationContentLabel.setText(h.getLocation());
        startDateContentLabel.setText(h.getStartDate().toLocalDate().toString());
        endDateContentLabel.setText(h.getEndDate().toLocalDate().toString());
        deadlineContentLabel.setText(h.getRegistrationEndDate().toLocalDate().toString());
        maxParticipantsContentLabel.setText(String.valueOf(h.getMaxParticipants()));
        maxTeamSizeContentLabel.setText(String.valueOf(h.getMaxTeamSize()));
        // Gestione della SQLException per getOrganizerNameForHackathon
        organizerContentLabel.setText("@" + controller.getOrganizerNameForHackathon(h.getHackathonId()));
    }

    private void clearFields() {
        titleContentLabel.setText("-");
        locationContentLabel.setText("-");
        startDateContentLabel.setText("-");
        endDateContentLabel.setText("-");
        deadlineContentLabel.setText("-");
        maxParticipantsContentLabel.setText("-");
        maxTeamSizeContentLabel.setText("-");
        organizerContentLabel.setText("-");
    }

    private RoundedPanel createRankingCard(String rankText) {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new FlowLayout(FlowLayout.LEFT));
        card.setBackground(UIColors.LIGHT_GRAY);
        JLabel label = new JLabel("<html><b>" + rankText + "</b></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setForeground(UIColors.NIGHT_BLUE);
        card.add(label);
        return card;
    }

    private void createUIComponents() {
        rTitlePanel = new RoundedPanel();
        rLocationPanel = new RoundedPanel();
        rStartDatePanel = new RoundedPanel();
        rEndDatePanel = new RoundedPanel();
        rDeadlinePanel = new RoundedPanel();
        rMaxParticipantsPanel = new RoundedPanel();
        rMaxTeamSizePanel = new RoundedPanel();
        rOrganizerPanel = new RoundedPanel();
        rTitleContentPanel = new RoundedPanel();
        rLocationContentPanel = new RoundedPanel();
        rStartDateContentPanel = new RoundedPanel();
        rEndDateContentPanel = new RoundedPanel();
        rDeadlineContentPanel = new RoundedPanel();
        rMaxParticipantsContentPanel = new RoundedPanel();
        rMaxTeamSizeContentPanel = new RoundedPanel();
        rOrganizerContentPanel = new RoundedPanel();
        rEditPanel = new RoundedPanel();
        rPublishPanel = new RoundedPanel();
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
        rootPanel.setPreferredSize(new Dimension(-1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        rootPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setAlignmentY(0.5f);
        panel1.add(panel2, BorderLayout.NORTH);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        hackathonLabel = new JLabel();
        Font hackathonLabelFont = this.$$$getFont$$$(null, -1, 26, hackathonLabel.getFont());
        if (hackathonLabelFont != null) hackathonLabel.setFont(hackathonLabelFont);
        hackathonLabel.setText("Hackathon");
        panel4.add(hackathonLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        infoLabel = new JLabel();
        infoLabel.setText("You're currently not registered for an event.");
        panel5.add(infoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel6.add(separator1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPanel = new JScrollPane();
        scrollPanel.setAlignmentY(0.5f);
        panel1.add(scrollPanel, BorderLayout.CENTER);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(17, 1, new Insets(0, 0, 0, 0), -1, -1));
        scrollPanel.setViewportView(panel7);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 3, new Insets(10, 0, 10, 0), -1, -1));
        panel7.add(panel8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel8.add(panel9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        overviewLabel = new JLabel();
        Font overviewLabelFont = this.$$$getFont$$$(null, -1, 18, overviewLabel.getFont());
        if (overviewLabelFont != null) overviewLabel.setFont(overviewLabelFont);
        overviewLabel.setText("Overview");
        panel9.add(overviewLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel8.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel8.add(panel10, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        final Spacer spacer3 = new Spacer();
        panel7.add(spacer3, new GridConstraints(16, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel11, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rTitlePanel.setLayout(new GridLayoutManager(1, 3, new Insets(1, 10, 1, 2), -1, -1));
        panel11.add(rTitlePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        titleLabel = new JLabel();
        Font titleLabelFont = this.$$$getFont$$$(null, -1, -1, titleLabel.getFont());
        if (titleLabelFont != null) titleLabel.setFont(titleLabelFont);
        titleLabel.setText("Title");
        rTitlePanel.add(titleLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rTitleContentPanel.setLayout(new GridLayoutManager(1, 1, new Insets(6, 6, 6, 6), -1, -1));
        rTitlePanel.add(rTitleContentPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        titleContentLabel = new JLabel();
        Font titleContentLabelFont = this.$$$getFont$$$(null, -1, -1, titleContentLabel.getFont());
        if (titleContentLabelFont != null) titleContentLabel.setFont(titleContentLabelFont);
        titleContentLabel.setText("sample_text");
        rTitleContentPanel.add(titleContentLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        rTitlePanel.add(spacer4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel11.add(spacer5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel12, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rLocationPanel.setLayout(new GridLayoutManager(1, 3, new Insets(1, 10, 1, 2), -1, -1));
        panel12.add(rLocationPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        locationLabel = new JLabel();
        Font locationLabelFont = this.$$$getFont$$$(null, -1, -1, locationLabel.getFont());
        if (locationLabelFont != null) locationLabel.setFont(locationLabelFont);
        locationLabel.setText("Location");
        rLocationPanel.add(locationLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rLocationContentPanel.setLayout(new GridLayoutManager(1, 1, new Insets(6, 6, 6, 6), -1, -1));
        rLocationPanel.add(rLocationContentPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        locationContentLabel = new JLabel();
        locationContentLabel.setText("sample_text");
        rLocationContentPanel.add(locationContentLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        rLocationPanel.add(spacer6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel12.add(spacer7, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel13, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rStartDatePanel.setLayout(new GridLayoutManager(1, 3, new Insets(1, 10, 1, 2), -1, -1));
        panel13.add(rStartDatePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        startDateLabel = new JLabel();
        Font startDateLabelFont = this.$$$getFont$$$(null, -1, -1, startDateLabel.getFont());
        if (startDateLabelFont != null) startDateLabel.setFont(startDateLabelFont);
        startDateLabel.setText("Start Date");
        rStartDatePanel.add(startDateLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rStartDateContentPanel.setLayout(new GridLayoutManager(1, 1, new Insets(6, 6, 6, 6), -1, -1));
        rStartDatePanel.add(rStartDateContentPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        startDateContentLabel = new JLabel();
        startDateContentLabel.setText("sample_text");
        rStartDateContentPanel.add(startDateContentLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        rStartDatePanel.add(spacer8, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        panel13.add(spacer9, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel14, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rEndDatePanel.setLayout(new GridLayoutManager(1, 3, new Insets(1, 10, 1, 2), -1, -1));
        panel14.add(rEndDatePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        endDateLabel = new JLabel();
        Font endDateLabelFont = this.$$$getFont$$$(null, -1, -1, endDateLabel.getFont());
        if (endDateLabelFont != null) endDateLabel.setFont(endDateLabelFont);
        endDateLabel.setText("End Date");
        rEndDatePanel.add(endDateLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rEndDateContentPanel.setLayout(new GridLayoutManager(1, 1, new Insets(6, 6, 6, 6), -1, -1));
        rEndDatePanel.add(rEndDateContentPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        endDateContentLabel = new JLabel();
        endDateContentLabel.setText("sample_text");
        rEndDateContentPanel.add(endDateContentLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer10 = new Spacer();
        rEndDatePanel.add(spacer10, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer11 = new Spacer();
        panel14.add(spacer11, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel15, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rDeadlinePanel.setLayout(new GridLayoutManager(1, 3, new Insets(1, 10, 1, 2), -1, -1));
        panel15.add(rDeadlinePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        deadlineLabel = new JLabel();
        Font deadlineLabelFont = this.$$$getFont$$$(null, -1, -1, deadlineLabel.getFont());
        if (deadlineLabelFont != null) deadlineLabel.setFont(deadlineLabelFont);
        deadlineLabel.setText("Deadline");
        rDeadlinePanel.add(deadlineLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rDeadlineContentPanel.setLayout(new GridLayoutManager(1, 1, new Insets(6, 6, 6, 6), -1, -1));
        rDeadlinePanel.add(rDeadlineContentPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        deadlineContentLabel = new JLabel();
        deadlineContentLabel.setText("sample_text");
        rDeadlineContentPanel.add(deadlineContentLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer12 = new Spacer();
        rDeadlinePanel.add(spacer12, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer13 = new Spacer();
        panel15.add(spacer13, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel16, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rOrganizerPanel.setLayout(new GridLayoutManager(1, 3, new Insets(1, 10, 1, 2), -1, -1));
        panel16.add(rOrganizerPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        organizerLabel = new JLabel();
        Font organizerLabelFont = this.$$$getFont$$$(null, -1, -1, organizerLabel.getFont());
        if (organizerLabelFont != null) organizerLabel.setFont(organizerLabelFont);
        organizerLabel.setText("Organizer");
        rOrganizerPanel.add(organizerLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rOrganizerContentPanel.setLayout(new GridLayoutManager(1, 1, new Insets(6, 6, 6, 6), -1, -1));
        rOrganizerPanel.add(rOrganizerContentPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        organizerContentLabel = new JLabel();
        organizerContentLabel.setText("sample_text");
        rOrganizerContentPanel.add(organizerContentLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer14 = new Spacer();
        rOrganizerPanel.add(spacer14, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer15 = new Spacer();
        panel16.add(spacer15, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel17 = new JPanel();
        panel17.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel17, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel18 = new JPanel();
        panel18.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel17.add(panel18, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        final Spacer spacer16 = new Spacer();
        panel17.add(spacer16, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel19 = new JPanel();
        panel19.setLayout(new GridLayoutManager(1, 3, new Insets(10, 0, 10, 0), -1, -1));
        panel7.add(panel19, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel20 = new JPanel();
        panel20.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel19.add(panel20, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        problemStatementLabel = new JLabel();
        Font problemStatementLabelFont = this.$$$getFont$$$(null, -1, 18, problemStatementLabel.getFont());
        if (problemStatementLabelFont != null) problemStatementLabel.setFont(problemStatementLabelFont);
        problemStatementLabel.setText("Problem Statement");
        panel20.add(problemStatementLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer17 = new Spacer();
        panel19.add(spacer17, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        rEditPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        rEditPanel.setInheritsPopupMenu(false);
        panel19.add(rEditPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, 30), new Dimension(100, 30), new Dimension(100, 30), 0, false));
        editLabel = new JLabel();
        editLabel.setText("Edit");
        rEditPanel.add(editLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel21 = new JPanel();
        panel21.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel21, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel22 = new JPanel();
        panel22.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel21.add(panel22, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        final Spacer spacer18 = new Spacer();
        panel21.add(spacer18, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel23 = new JPanel();
        panel23.setLayout(new GridLayoutManager(1, 3, new Insets(10, 0, 10, 0), -1, -1));
        panel7.add(panel23, new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel24 = new JPanel();
        panel24.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel23.add(panel24, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rankingLabel = new JLabel();
        Font rankingLabelFont = this.$$$getFont$$$(null, -1, 18, rankingLabel.getFont());
        if (rankingLabelFont != null) rankingLabel.setFont(rankingLabelFont);
        rankingLabel.setText("Ranking");
        panel24.add(rankingLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer19 = new Spacer();
        panel23.add(spacer19, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        rPublishPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel23.add(rPublishPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, 30), new Dimension(100, 30), new Dimension(100, 30), 0, false));
        publishLabel = new JLabel();
        publishLabel.setText("Publish");
        rPublishPanel.add(publishLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel25 = new JPanel();
        panel25.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel25, new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rankingInfoLabel = new JLabel();
        rankingInfoLabel.setText("The ranking is currently unavailable.");
        panel25.add(rankingInfoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rankingListPanel = new JPanel();
        rankingListPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(rankingListPanel, new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel26 = new JPanel();
        panel26.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel26, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rMaxParticipantsPanel.setLayout(new GridLayoutManager(1, 3, new Insets(1, 10, 1, 2), -1, -1));
        panel26.add(rMaxParticipantsPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        maxParticipantsLabel = new JLabel();
        Font maxParticipantsLabelFont = this.$$$getFont$$$(null, -1, -1, maxParticipantsLabel.getFont());
        if (maxParticipantsLabelFont != null) maxParticipantsLabel.setFont(maxParticipantsLabelFont);
        maxParticipantsLabel.setText("Max Participants");
        rMaxParticipantsPanel.add(maxParticipantsLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rMaxParticipantsContentPanel.setLayout(new GridLayoutManager(1, 1, new Insets(6, 6, 6, 6), -1, -1));
        rMaxParticipantsPanel.add(rMaxParticipantsContentPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        maxParticipantsContentLabel = new JLabel();
        maxParticipantsContentLabel.setText("sample_text");
        rMaxParticipantsContentPanel.add(maxParticipantsContentLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer20 = new Spacer();
        rMaxParticipantsPanel.add(spacer20, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer21 = new Spacer();
        panel26.add(spacer21, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel27 = new JPanel();
        panel27.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel27, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rMaxTeamSizePanel.setLayout(new GridLayoutManager(1, 3, new Insets(1, 10, 1, 2), -1, -1));
        panel27.add(rMaxTeamSizePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        maxTeamSizeLabel = new JLabel();
        Font maxTeamSizeLabelFont = this.$$$getFont$$$(null, -1, -1, maxTeamSizeLabel.getFont());
        if (maxTeamSizeLabelFont != null) maxTeamSizeLabel.setFont(maxTeamSizeLabelFont);
        maxTeamSizeLabel.setText("Max Team Size");
        rMaxTeamSizePanel.add(maxTeamSizeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rMaxTeamSizeContentPanel.setLayout(new GridLayoutManager(1, 1, new Insets(6, 6, 6, 6), -1, -1));
        rMaxTeamSizePanel.add(rMaxTeamSizeContentPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        maxTeamSizeContentLabel = new JLabel();
        maxTeamSizeContentLabel.setText("sample_text");
        rMaxTeamSizeContentPanel.add(maxTeamSizeContentLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer22 = new Spacer();
        rMaxTeamSizePanel.add(spacer22, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer23 = new Spacer();
        panel27.add(spacer23, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel28 = new JPanel();
        panel28.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel28, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        problemStatementTextArea = new JTextArea();
        panel28.add(problemStatementTextArea, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
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
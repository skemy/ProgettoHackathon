package gui;

import controller.Controller;
import model.Hackathon;
import model.Team;
import utils.RoundedPanel;
import utils.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class HackathonCardPanel {
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

    private final Controller controller;
    private boolean isEditingMode = false;

    public HackathonCardPanel(Controller controller) {
        this.controller = controller;

        // Inizializza i componenti personalizzati
        createUIComponents();

        // Configura colori e scrolling
        customizeComponents();

        // Collega i tasti (Edit e Publish)
        setupREditPanel();
        setupRPublishPanel();

        // Carica i dati dal DB
        refreshData(false);
    }

    public void refreshData(boolean showPopup) {
        Hackathon currentHackathon = controller.getCurrentHackathon();

        if (currentHackathon != null) {
            infoLabel.setText("You are currently registered for the event: " + currentHackathon.getTitle());

            // NOVITÀ: Se è un semplice User, avvisalo che deve andare nella sezione Team!
            if (controller.getCurrentUser().getClass().equals(model.User.class)) {
                infoLabel.setText(infoLabel.getText() + " | ⚠️ VAI NELLA SEZIONE 'TEAM' PER CREARE/UNIRTI A UNA SQUADRA!");
            }
            addHackathonInfo(currentHackathon);
            if (currentHackathon.getProblemDescription() == null || currentHackathon.getProblemDescription().trim().isEmpty()) {
                problemStatementTextArea.setText("Problem statement is empty.");
            } else {
                problemStatementTextArea.setText(currentHackathon.getProblemDescription());
            }
        } else {
            infoLabel.setText("You are currently not registered for an event.");
            titleContentLabel.setText("N/A");
            locationContentLabel.setText("N/A");
            startDateContentLabel.setText("N/A");
            endDateContentLabel.setText("N/A");
            deadlineContentLabel.setText("N/A");
            maxParticipantsContentLabel.setText("N/A");
            maxTeamSizeContentLabel.setText("N/A");
            organizerContentLabel.setText("N/A");
            problemStatementTextArea.setText("No hackathon loaded.");
        }
    }

    private void customizeComponents() {
        setupScrollPanel();

        // Colori Labels e Pannelli
        hackathonLabel.setForeground(UIColors.NIGHT_BLUE);
        infoLabel.setForeground(UIColors.CARMINE_RED);
        overviewLabel.setForeground(UIColors.CARMINE_RED);

        rTitlePanel.setBackground(UIColors.LIGHT_GRAY);
        rLocationPanel.setBackground(UIColors.LIGHT_GRAY);
        rStartDatePanel.setBackground(UIColors.LIGHT_GRAY);
        rEndDatePanel.setBackground(UIColors.LIGHT_GRAY);
        rDeadlinePanel.setBackground(UIColors.LIGHT_GRAY);
        rMaxParticipantsPanel.setBackground(UIColors.LIGHT_GRAY);
        rMaxTeamSizePanel.setBackground(UIColors.LIGHT_GRAY);
        rOrganizerPanel.setBackground(UIColors.LIGHT_GRAY);

        rTitleContentPanel.setBackground(Color.WHITE);
        rLocationContentPanel.setBackground(Color.WHITE);
        rStartDateContentPanel.setBackground(Color.WHITE);
        rEndDateContentPanel.setBackground(Color.WHITE);
        rDeadlineContentPanel.setBackground(Color.WHITE);
        rMaxParticipantsContentPanel.setBackground(Color.WHITE);
        rMaxTeamSizeContentPanel.setBackground(Color.WHITE);
        rOrganizerContentPanel.setBackground(Color.WHITE);

        // Problem Statement
        problemStatementLabel.setForeground(UIColors.CARMINE_RED);
        rEditPanel.setBackground(UIColors.NIGHT_BLUE);
        editLabel.setForeground(Color.WHITE);
        problemStatementTextArea.setBackground(null);
        problemStatementTextArea.setForeground(Color.DARK_GRAY);

        // Ranking
        rankingLabel.setForeground(UIColors.CARMINE_RED);
        rankingInfoLabel.setForeground(Color.GRAY);
        rPublishPanel.setBackground(UIColors.NIGHT_BLUE);
        publishLabel.setForeground(Color.WHITE);

        setupRankingListPanel();
        SwingUtilities.invokeLater(() -> scrollPanel.getVerticalScrollBar().setValue(0));
    }

    private void setupREditPanel() {
        // Garantisce che l'area sia bloccata all'avvio
        problemStatementTextArea.setEditable(false);
        problemStatementTextArea.setFocusable(false);

        // Mouse Listener condiviso per pannello e label
        MouseAdapter editHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Controllo Sicurezza BCE
                if (!controller.isCurrentUserJudge()) {
                    JOptionPane.showMessageDialog(null,
                            "Only a Judge can edit the Problem Statement.",
                            "Access Restricted",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!isEditingMode) {
                    // Modalità MODIFICA
                    isEditingMode = true;
                    problemStatementTextArea.setEditable(true);
                    problemStatementTextArea.setFocusable(true);
                    problemStatementTextArea.requestFocus();
                    problemStatementTextArea.setBackground(Color.WHITE);
                    problemStatementTextArea.setBorder(BorderFactory.createLineBorder(UIColors.CARMINE_RED, 1));

                    editLabel.setText("Save");
                    rEditPanel.setBackground(new Color(46, 204, 113)); // Verde
                } else {
                    // Modalità SALVATAGGIO
                    String newText = problemStatementTextArea.getText().trim();
                    if (controller.updateHackathonProblem(newText)) {
                        isEditingMode = false;
                        problemStatementTextArea.setEditable(false);
                        problemStatementTextArea.setFocusable(false);
                        problemStatementTextArea.setBackground(null);
                        problemStatementTextArea.setBorder(null);
                        editLabel.setText("Edit");
                        rEditPanel.setBackground(UIColors.NIGHT_BLUE);
                        JOptionPane.showMessageDialog(null, "Saved successfully!");
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isEditingMode) rEditPanel.setBackground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isEditingMode) rEditPanel.setBackground(UIColors.NIGHT_BLUE);
            }
        };

        // COLLEGA IL LISTENER A ENTRAMBI (Importante!)
        rEditPanel.addMouseListener(editHandler);
        editLabel.addMouseListener(editHandler);
        rEditPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void setupRPublishPanel() {
        MouseAdapter publishHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Controllo Sicurezza BCE
                if (!controller.isCurrentUserJudge()) {
                    JOptionPane.showMessageDialog(null,
                            "Only a Judge can publish the ranking.",
                            "Access Restricted",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Logica Classifica (Solo per Giudici)
                List<Team> rankedTeams = controller.getRankedTeams();
                rankingListPanel.removeAll();

                if (rankedTeams != null && !rankedTeams.isEmpty()) {
                    for (int i = 0; i < rankedTeams.size(); i++) {
                        rankingListPanel.add(createRankingCard(i + 1, rankedTeams.get(i).getTeamName()));
                        rankingListPanel.add(Box.createVerticalStrut(10));
                    }
                } else {
                    rankingListPanel.add(new JLabel("No ranking data available."));
                }

                rankingListPanel.revalidate();
                rankingListPanel.repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) { rPublishPanel.setBackground(UIColors.CARMINE_RED); }
            @Override
            public void mouseExited(MouseEvent e) { rPublishPanel.setBackground(UIColors.NIGHT_BLUE); }
        };

        rPublishPanel.addMouseListener(publishHandler);
        publishLabel.addMouseListener(publishHandler);
        rPublishPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        publishLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // --- ALTRI METODI DI SUPPORTO (Scroll, Info, ecc.) ---
    private void setupScrollPanel() {
        scrollPanel.setBorder(null);
        scrollPanel.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPanel.getVerticalScrollBar().setUnitIncrement(10);
    }

    private void setupRankingListPanel() {
        rankingListPanel.setLayout(new BoxLayout(rankingListPanel, BoxLayout.Y_AXIS));
    }

    private void addHackathonInfo(Hackathon hackathon) {
        titleContentLabel.setText(hackathon.getTitle());
        locationContentLabel.setText(hackathon.getLocation());
        startDateContentLabel.setText(hackathon.getStartDate().toString());
        endDateContentLabel.setText(hackathon.getEndDate().toString());
        deadlineContentLabel.setText(hackathon.getRegistrationEndDate().toString());
        maxParticipantsContentLabel.setText(String.valueOf(hackathon.getMaxParticipants()));
        maxTeamSizeContentLabel.setText(String.valueOf(hackathon.getMaxTeamSize()));
        organizerContentLabel.setText("@" + controller.getOrganizerNameForHackathon(hackathon.getHackathonId()));
    }

    private RoundedPanel createRankingCard(int rank, String name) {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new FlowLayout(FlowLayout.LEFT));
        card.setBackground(Color.WHITE);
        card.add(new JLabel(rank + "° " + name));
        return card;
    }

    private void createUIComponents() {
        rTitlePanel = new RoundedPanel(); rLocationPanel = new RoundedPanel();
        rStartDatePanel = new RoundedPanel(); rEndDatePanel = new RoundedPanel();
        rDeadlinePanel = new RoundedPanel(); rMaxParticipantsPanel = new RoundedPanel();
        rMaxTeamSizePanel = new RoundedPanel(); rOrganizerPanel = new RoundedPanel();
        rTitleContentPanel = new RoundedPanel(); rLocationContentPanel = new RoundedPanel();
        rStartDateContentPanel = new RoundedPanel(); rEndDateContentPanel = new RoundedPanel();
        rDeadlineContentPanel = new RoundedPanel(); rMaxParticipantsContentPanel = new RoundedPanel();
        rMaxTeamSizeContentPanel = new RoundedPanel(); rOrganizerContentPanel = new RoundedPanel();
        rEditPanel = new RoundedPanel(); rPublishPanel = new RoundedPanel();
    }

    public JPanel getRootPanel() { return rootPanel; }
}
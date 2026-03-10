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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import model.Hackathon;

public class DashboardCardPanel {
    private JPanel rootPanel;
    private JLabel dashboardLabel;
    private JLabel welcomeLabel;
    private JPanel rAddPanel;
    private JLabel addLabel;
    private JScrollPane scrollPanel;
    private JPanel eventListPanel;
    private JLabel infoLabel;
    private JLabel openEventsLabel;
    private JLabel emailLabel;

    private final Controller controller;

    public DashboardCardPanel(Controller controller) {
        this.controller = controller;
        customizeComponents();
        setupScrollPanel();
        populateEventListPanel();
    }

    private void customizeComponents() {
        dashboardLabel.setForeground(UIColors.NIGHT_BLUE);
        welcomeLabel.setForeground(UIColors.CARMINE_RED);

        // AGGIORNATO: Usiamo getName() invece del vecchio getUsername()
        welcomeLabel.setText("Welcome, @" + controller.getCurrentUser().getName() + "!");

        emailLabel.setForeground(Color.GRAY);
        emailLabel.setText("E-mail: " + controller.getCurrentUser().getEmail());

        openEventsLabel.setForeground(UIColors.CARMINE_RED);
        rAddPanel.setBackground(UIColors.NIGHT_BLUE);
        addLabel.setForeground(Color.WHITE);
        infoLabel.setForeground(Color.GRAY);

        SwingUtilities.invokeLater(() -> scrollPanel.getVerticalScrollBar().setValue(0));
    }

    private void setupScrollPanel() {
        scrollPanel.setBorder(null);
        scrollPanel.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPanel.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPanel.getVerticalScrollBar().setUnitIncrement(10);

        eventListPanel.setLayout(new BoxLayout(eventListPanel, BoxLayout.Y_AXIS));
        eventListPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    }

    private void populateEventListPanel() {
        // AGGIORNATO: Chiama il nuovo metodo del Controller
        if (controller.getAllHackathons().isEmpty()) {
            infoLabel.setVisible(true);
        } else {
            infoLabel.setVisible(false);
            for (Hackathon h : controller.getAllHackathons()) {
                RoundedPanel card = createEventCard(h);
                eventListPanel.add(card, 0);
                eventListPanel.add(Box.createVerticalStrut(15), 1);
            }
        }
    }

    private RoundedPanel createEventCard(Hackathon hackathon) {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorderColor(UIColors.LIGHT_GRAY);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // NOTA: Se alcuni di questi getter (es. getLocation) danno errore rosso,
        // modificali in base ai metodi reali della tua classe Hackathon.java
        JLabel titleLabel = new JLabel(hackathon.getTitle());
        JLabel locationLabel = new JLabel("Location: " + hackathon.getLocation());
        JLabel startDateLabel = new JLabel("Start Date: " + hackathon.getStartDate());
        JLabel endDateLabel = new JLabel("End Date: " + hackathon.getEndDate());
        JLabel maxParticipants = new JLabel("Max participants: " + hackathon.getMaxParticipants());
        JLabel maxTeamSize = new JLabel("Max team size: " + hackathon.getMaxTeamSize());

        titleLabel.setForeground(UIColors.CARMINE_RED);
        titleLabel.setFont(new Font(null, Font.BOLD, 14));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(locationLabel);
        card.add(startDateLabel);
        card.add(endDateLabel);
        card.add(maxParticipants);
        card.add(maxTeamSize);

        makeCardInteractive(card, hackathon);

        return card;
    }

    private void makeCardInteractive(RoundedPanel card, Hackathon hackathon) {
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int result = JOptionPane.showConfirmDialog(null, "Do you want to register to this hackathon?", "Register", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    try {
                        // AGGIORNATO: Delega tutta la logica di iscrizione al Controller
                        // Assumiamo che la classe Hackathon abbia un metodo getId() o simile
                        controller.joinHackathon(hackathon.getHackathonId());
                        JOptionPane.showMessageDialog(null, "Registration completed!");
                    } catch (Exception ex) {
                        showErrorDialog(ex.getMessage());
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(UIColors.LIGHT_GRAY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });
    }

    private void createUIComponents() {
        rAddPanel = new RoundedPanel();
        setupAddPanelListener();
    }

    private void setupAddPanelListener() {
        rAddPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        rAddPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                User currentUser = controller.getCurrentUser();

                // AGGIORNATO: Nuova logica dei Ruoli tramite polimorfismo!
                if (currentUser instanceof Participant || currentUser instanceof Organizer || currentUser instanceof Judge) {
                    showErrorDialog("You cannot create a new event while you already have an active role in another event.");
                } else {
                    try {
                        JTextField titleField = new JTextField();
                        JTextField locationField = new JTextField();
                        JTextField startDateField = new JTextField(LocalDate.now().plusDays(3).toString());
                        JTextField endDateField = new JTextField(LocalDate.now().plusDays(4).toString());
                        JTextField maxParticipantsField = new JTextField("100");
                        JTextField maxTeamSizeField = new JTextField("5");

                        JPanel panel = new JPanel();
                        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                        panel.add(new JLabel("Title:"));
                        panel.add(titleField);
                        panel.add(new JLabel("Location:"));
                        panel.add(locationField);
                        panel.add(new JLabel("Start Date (YYYY-MM-DD):"));
                        panel.add(startDateField);
                        panel.add(new JLabel("End Date (YYYY-MM-DD):"));
                        panel.add(endDateField);
                        panel.add(new JLabel("Max number of participants:"));
                        panel.add(maxParticipantsField);
                        panel.add(new JLabel("Max team size:"));
                        panel.add(maxTeamSizeField);

                        int result = JOptionPane.showConfirmDialog(null, panel, "Add Hackathon", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                        if (result == JOptionPane.OK_OPTION) {
                            String title = titleField.getText();
                            String location = locationField.getText();
                            LocalDate startDate = LocalDate.parse(startDateField.getText());
                            LocalDate endDate = LocalDate.parse(endDateField.getText());
                            int maxParticipants = Integer.parseInt(maxParticipantsField.getText());
                            int maxTeamSize = Integer.parseInt(maxTeamSizeField.getText());

                            // AGGIORNATO: Il Controller fa tutto il lavoro!
                            controller.createHackathon(title, location, startDate, endDate, maxParticipants, maxTeamSize);

                            updateEventListPanel();
                            JOptionPane.showMessageDialog(null, "Event created successfully!");
                        }
                    } catch (Exception ex) {
                        showErrorDialog("Error creating event: " + ex.getMessage());
                    }
                }
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

    private void updateEventListPanel() {
        eventListPanel.removeAll();
        populateEventListPanel();
        eventListPanel.revalidate();
        eventListPanel.repaint();
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public void loadDashboardData() {
        // Ora 'List' verrà riconosciuta come java.util.List
        List<Hackathon> eventi = controller.getAllHackathons();

        if (eventi != null) {
            for (Hackathon h : eventi) {
                String titolo = h.getTitle();
                System.out.println("Hackathon caricato: " + titolo);
                // Qui aggiungerai la logica per popolare la tua JTable o i tuoi pannelli
            }
        }
    }

}
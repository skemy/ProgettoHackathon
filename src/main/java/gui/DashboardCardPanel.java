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
        $$$setupUI$$$();
        customizeComponents();
        setupScrollPanel();
        populateEventListPanel();
    }

    private void customizeComponents() {
        dashboardLabel.setForeground(UIColors.NIGHT_BLUE);
        welcomeLabel.setForeground(UIColors.CARMINE_RED);
        welcomeLabel.setText("Welcome, @" + controller.getCurrentUser().getName() + "!");

        emailLabel.setForeground(Color.GRAY);
        emailLabel.setText("E-mail: " + controller.getCurrentUser().getEmail());

        openEventsLabel.setForeground(UIColors.CARMINE_RED);

        // --- LOGICA DI SPARIZIONE TASTO ---
        rAddPanel.setVisible(controller.canUserCreateHackathon()); //

        rAddPanel.setBackground(UIColors.NIGHT_BLUE);
        addLabel.setForeground(Color.WHITE);
        infoLabel.setForeground(Color.GRAY);

        SwingUtilities.invokeLater(() -> scrollPanel.getVerticalScrollBar().setValue(0));
    }

    // Aggiungi questo metodo per permettere al MainFrame di aggiornare la visibilità al cambio scheda
    public void refreshData() {
        rAddPanel.setVisible(controller.canUserCreateHackathon());
        updateEventListPanel();
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
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        dashboardLabel = new JLabel();
        Font dashboardLabelFont = this.$$$getFont$$$(null, -1, 26, dashboardLabel.getFont());
        if (dashboardLabelFont != null) dashboardLabel.setFont(dashboardLabelFont);
        dashboardLabel.setText("Dashboard");
        panel4.add(dashboardLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        welcomeLabel = new JLabel();
        welcomeLabel.setText("Welcome, %USERNAME%!");
        panel5.add(welcomeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        emailLabel = new JLabel();
        emailLabel.setText("E-mail: ");
        panel5.add(emailLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel6.add(separator1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel7, BorderLayout.CENTER);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new BorderLayout(0, 0));
        panel7.add(panel8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 3, new Insets(10, 0, 10, 0), -1, -1));
        panel8.add(panel9, BorderLayout.NORTH);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        openEventsLabel = new JLabel();
        Font openEventsLabelFont = this.$$$getFont$$$(null, -1, 18, openEventsLabel.getFont());
        if (openEventsLabelFont != null) openEventsLabel.setFont(openEventsLabelFont);
        openEventsLabel.setText("Open Events");
        panel10.add(openEventsLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel9.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        rAddPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel9.add(rAddPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, 30), new Dimension(100, 30), new Dimension(100, 30), 0, false));
        addLabel = new JLabel();
        addLabel.setText("Add");
        rAddPanel.add(addLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scrollPanel = new JScrollPane();
        panel8.add(scrollPanel, BorderLayout.CENTER);
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new BorderLayout(0, 0));
        scrollPanel.setViewportView(panel11);
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel11.add(panel12, BorderLayout.NORTH);
        infoLabel = new JLabel();
        infoLabel.setText("There are currently no available events.");
        panel12.add(infoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventListPanel = new JPanel();
        eventListPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel11.add(eventListPanel, BorderLayout.CENTER);
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
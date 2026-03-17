package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import controller.Controller;
import model.Hackathon;
import model.User;
import utils.RoundedPanel;
import utils.UIColors;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

/**
 * Pannello principale della Dashboard (Layer Boundary).
 * Visualizza l'elenco degli hackathon disponibili e gestisce la creazione di nuovi eventi.
 * <p>
 * Nota Architetturale: Rispetta il pattern BCE delegando la logica di business
 * al Controller e gestendo le eccezioni di persistenza per fornire feedback all'utente.
 */
public class DashboardCardPanel {

    // COSTANTE per risolvere SonarQube S1192 (Literal "Error" duplication)
    private static final String ERROR_TITLE = "Error";

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

    /**
     * Costruttore del pannello Dashboard.
     *
     * @param controller Il coordinatore centrale del sistema.
     */
    public DashboardCardPanel(Controller controller) {
        this.controller = controller;
        $$$setupUI$$$();
        customizeComponents();
        setupScrollPanel();
        refreshData();
    }

    /**
     * Esegue il refresh dei dati utente e della lista eventi.
     * Nota: Rimosso try-catch su getCurrentUser() perché è un getter in memoria (risoluzione errore compilazione).
     */
    public void refreshData() {
        rAddPanel.setVisible(controller.canUserCreateHackathon());

        User user = controller.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, @" + user.getName() + "!");
            emailLabel.setText("E-mail: " + user.getEmail());
        }

        updateEventList(); // Questo metodo gestisce internamente la SQLException
    }

    /**
     * Applica stili e colori personalizzati ai componenti UI.
     */
    private void customizeComponents() {
        dashboardLabel.setForeground(UIColors.NIGHT_BLUE);
        welcomeLabel.setForeground(UIColors.CARMINE_RED);
        emailLabel.setForeground(Color.GRAY);
        openEventsLabel.setForeground(UIColors.CARMINE_RED);

        rAddPanel.setBackground(UIColors.NIGHT_BLUE);
        addLabel.setForeground(Color.WHITE);
        infoLabel.setForeground(Color.GRAY);

        SwingUtilities.invokeLater(() -> scrollPanel.getVerticalScrollBar().setValue(0));
    }

    private void setupScrollPanel() {
        scrollPanel.setBorder(null);
        scrollPanel.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
        scrollPanel.getVerticalScrollBar().setUnitIncrement(16);

        eventListPanel.setLayout(new BoxLayout(eventListPanel, BoxLayout.Y_AXIS));
        eventListPanel.setBackground(Color.WHITE);
    }

    /**
     * Aggiorna la lista degli hackathon interrogando il database tramite il Controller.
     */
    private void updateEventList() {
        eventListPanel.removeAll();
        try {
            List<Hackathon> events = controller.getAllHackathons();

            if (events.isEmpty()) {
                infoLabel.setVisible(true);
            } else {
                infoLabel.setVisible(false);
                for (Hackathon h : events) {
                    eventListPanel.add(createEventCard(h));
                    eventListPanel.add(Box.createVerticalStrut(15));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(rootPanel, "Error loading events: " + e.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }

        eventListPanel.revalidate();
        eventListPanel.repaint();
    }

    /**
     * Crea graficamente una card per rappresentare un Hackathon.
     */
    private RoundedPanel createEventCard(Hackathon h) {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorderColor(UIColors.LIGHT_GRAY);
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel title = new JLabel(h.getTitle());
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(UIColors.NIGHT_BLUE);

        JLabel details = new JLabel(String.format("📍 %s | 📅 %s - %s",
                h.getLocation(), h.getStartDate().toLocalDate(), h.getEndDate().toLocalDate()));
        details.setFont(new Font("SansSerif", Font.PLAIN, 13));
        details.setForeground(Color.DARK_GRAY);

        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(details);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleRegistration(h);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(245, 245, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    private void handleRegistration(Hackathon h) {
        int choice = JOptionPane.showConfirmDialog(rootPanel,
                "Do you want to register for: " + h.getTitle() + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                controller.joinHackathon(h.getHackathonId());
                JOptionPane.showMessageDialog(rootPanel, "Registration completed!");
                refreshData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(rootPanel, "Database error: " + ex.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(rootPanel, ex.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setupAddPanelListener() {
        rAddPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rAddPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (controller.canUserCreateHackathon()) {
                    showCreateHackathonDialog();
                } else {
                    JOptionPane.showMessageDialog(rootPanel, "You already have an active role.", "Access Denied", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    private void showCreateHackathonDialog() {
        JTextField titleF = new JTextField();
        JTextField locF = new JTextField();
        JTextField startF = new JTextField(LocalDate.now().plusDays(7).toString());
        JTextField endF = new JTextField(LocalDate.now().plusDays(8).toString());

        Object[] message = {"Title:", titleF, "Location:", locF, "Start (YYYY-MM-DD):", startF, "End (YYYY-MM-DD):", endF};

        int option = JOptionPane.showConfirmDialog(rootPanel, message, "New Hackathon", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                controller.createHackathon(titleF.getText(), locF.getText(), LocalDate.parse(startF.getText()), LocalDate.parse(endF.getText()), 100, 5);
                JOptionPane.showMessageDialog(rootPanel, "Hackathon created successfully!");
                refreshData();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(rootPanel, "Invalid date format.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(rootPanel, "DB Error: " + ex.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(rootPanel, ex.getMessage(), ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void createUIComponents() {
        rAddPanel = new RoundedPanel();
        setupAddPanelListener();
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
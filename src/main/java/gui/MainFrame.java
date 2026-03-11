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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainFrame extends JFrame {
    private JPanel rootPanel;
    private JPanel sidebarPanel;
    private JPanel cardPanel;
    private JPanel containerPanel;

    private JPanel rDashboardPanel;
    private JPanel rHackathonPanel;
    private JPanel rTeamPanel;
    private JPanel rLogoutPanel;
    private JPanel rManagePanel;

    private JLabel dashboardLabel;
    private JLabel hackathonLabel;
    private JLabel teamLabel;
    private JLabel logoutLabel;
    private JLabel manageLabel;
    private JLabel menuLabel;
    private JPanel menuPanel;

    private final Controller controller;
    private final Map<String, JPanel> cardMap = new HashMap<>();
    private CardLayout cardLayout;

    public MainFrame(Controller controller) {
        this.controller = controller;

        $$$setupUI$$$();
        setTitle("Hackathon.IO - Home (" + controller.getCurrentUser().getName() + ")");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(rootPanel);
        setLocationRelativeTo(null);

        setupCardPanel();
        customizeComponents();
    }

    private HackathonCardPanel hackathonCard;

    private void setupCardPanel() {
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        DashboardCardPanel dashboardCard = new DashboardCardPanel(controller);
        JPanel dashboardPanel = dashboardCard.getRootPanel();

        hackathonCard = new HackathonCardPanel(controller);
        JPanel hackathonPanel = hackathonCard.getRootPanel();

        // Segnaposti rimanenti
        JPanel teamPanel = new JPanel(); teamPanel.setBackground(Color.LIGHT_GRAY);
        JPanel managePanel = new JPanel(); managePanel.setBackground(Color.DARK_GRAY);

        cardPanel.add(dashboardPanel, "dashboard");
        cardPanel.add(hackathonPanel, "hackathon");
        cardPanel.add(teamPanel, "team");
        cardPanel.add(managePanel, "manage");

        cardLayout.show(cardPanel, "dashboard");
    }

    private void customizeComponents() {
        sidebarPanel.setBackground(UIColors.NIGHT_BLUE);
        menuLabel.setForeground(Color.WHITE);
        containerPanel.setBackground(UIColors.NIGHT_BLUE);
        menuPanel.setBackground(UIColors.NIGHT_BLUE);

        rDashboardPanel.setBackground(UIColors.NIGHT_BLUE);
        dashboardLabel.setForeground(Color.WHITE);
        rHackathonPanel.setBackground(UIColors.NIGHT_BLUE);
        hackathonLabel.setForeground(Color.WHITE);
        rTeamPanel.setBackground(UIColors.NIGHT_BLUE);
        teamLabel.setForeground(Color.WHITE);
        rManagePanel.setBackground(UIColors.NIGHT_BLUE);
        manageLabel.setForeground(Color.WHITE);
        rLogoutPanel.setBackground(UIColors.NIGHT_BLUE);
        logoutLabel.setForeground(Color.WHITE);
    }

    private void createUIComponents() {
        rDashboardPanel = new RoundedPanel();
        rHackathonPanel = new RoundedPanel();
        rTeamPanel = new RoundedPanel();
        rManagePanel = new RoundedPanel();
        rLogoutPanel = new RoundedPanel();
        setupListeners();
    }

    private void setupListeners() {
        // Tasto Dashboard
        rDashboardPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { cardLayout.show(cardPanel, "dashboard"); }
            @Override public void mouseEntered(MouseEvent e) { rDashboardPanel.setBackground(UIColors.CARMINE_RED); }
            @Override public void mouseExited(MouseEvent e) { rDashboardPanel.setBackground(UIColors.NIGHT_BLUE); }
        });


        rHackathonPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                hackathonCard.refreshData(true); // <--- AGGIUNGI IL TRUE QUI
                cardLayout.show(cardPanel, "hackathon");
            }
            @Override public void mouseEntered(MouseEvent e) { rHackathonPanel.setBackground(UIColors.CARMINE_RED); }
            @Override public void mouseExited(MouseEvent e) { rHackathonPanel.setBackground(UIColors.NIGHT_BLUE); }
        });

        // Tasto Team (Il mio team / Creazione team)
        rTeamPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { cardLayout.show(cardPanel, "team"); }
            @Override public void mouseEntered(MouseEvent e) { rTeamPanel.setBackground(UIColors.CARMINE_RED); }
            @Override public void mouseExited(MouseEvent e) { rTeamPanel.setBackground(UIColors.NIGHT_BLUE); }
        });

        // Tasto Manage (Solo per Organizer)
        rManagePanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { cardLayout.show(cardPanel, "manage"); }
            @Override public void mouseEntered(MouseEvent e) { rManagePanel.setBackground(UIColors.CARMINE_RED); }
            @Override public void mouseExited(MouseEvent e) { rManagePanel.setBackground(UIColors.NIGHT_BLUE); }
        });

        // Tasto Logout
        rLogoutPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dispose();
                new AuthFrame(new Controller()).setVisible(true);
            }
            @Override public void mouseEntered(MouseEvent e) { rLogoutPanel.setBackground(UIColors.CARMINE_RED); }
            @Override public void mouseExited(MouseEvent e) { rLogoutPanel.setBackground(UIColors.NIGHT_BLUE); }
        });
    }

    // --- NON CANCELLARE IL METODO $$$setupUI$$$ CHE SEGUE ---
    private void $$$setupUI$$$() {
        // ... (lascia che IntelliJ lo generi salvando il file .form)
    }
}
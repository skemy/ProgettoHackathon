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
import java.util.Locale;

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
    private JPanel rEvaluationPanel;

    private JLabel dashboardLabel;
    private JLabel hackathonLabel;
    private JLabel teamLabel;
    private JLabel logoutLabel;
    private JLabel manageLabel;
    private JLabel menuLabel;
    private JPanel menuPanel;
    private JLabel evaluationLable;

    private final Controller controller;
    private CardLayout cardLayout;

    // Riferimenti ai pannelli reali per gestire i refresh
    private HackathonCardPanel hackathonCard;
    private TeamCardPanel teamCard;
    private OrganizerManageCardPanel organizerCard;
    private JudgeManageCardPanel judgeCard;

    public MainFrame(Controller controller) {
        this.controller = controller;

        // 1. Carica l'interfaccia dal file .form
        $$$setupUI$$$();

        // 2. Impostazioni base della finestra
        setTitle("Hackathon.IO - Home (" + controller.getCurrentUser().getName() + ")");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(rootPanel);
        setLocationRelativeTo(null);

        // 3. Inizializza i contenuti e la logica
        setupCardPanel();
        customizeComponents(); // Colora la sidebar e i pannelli
        setupListeners();      // Attiva i click sui tasti

        // Controllo se l'utente è stato espulso per mancanza di team
        if (controller.wasRecentlyKicked()) {
            JOptionPane.showMessageDialog(this,
                    "Attenzione: L'evento a cui eri iscritto è iniziato e non facevi parte di alcun team.\nLa tua registrazione è stata annullata.",
                    "Tempo Scaduto",
                    JOptionPane.WARNING_MESSAGE);
            controller.resetKickedFlag();
        }
    }

    private void setupCardPanel() {
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        // Inizializzazione dei pannelli reali
        DashboardCardPanel dashboardCard = new DashboardCardPanel(controller);
        cardPanel.add(dashboardCard.getRootPanel(), "dashboard");

        hackathonCard = new HackathonCardPanel(controller);
        cardPanel.add(hackathonCard.getRootPanel(), "hackathon");

        teamCard = new TeamCardPanel(controller);
        cardPanel.add(teamCard.getRootPanel(), "team");

        organizerCard = new OrganizerManageCardPanel(controller);
        cardPanel.add(organizerCard.getRootPanel(), "manage");

        judgeCard = new JudgeManageCardPanel(controller);
        cardPanel.add(judgeCard.getRootPanel(), "evaluation");

        cardLayout.show(cardPanel, "dashboard");
    }

    private void customizeComponents() {
        // Colori Sidebar e Sfondo
        sidebarPanel.setBackground(UIColors.NIGHT_BLUE);
        containerPanel.setBackground(UIColors.NIGHT_BLUE);
        menuPanel.setBackground(UIColors.NIGHT_BLUE);
        menuLabel.setForeground(Color.WHITE);

        // Reset colori pannelli tasti
        rDashboardPanel.setBackground(UIColors.NIGHT_BLUE);
        dashboardLabel.setForeground(Color.WHITE);

        rHackathonPanel.setBackground(UIColors.NIGHT_BLUE);
        hackathonLabel.setForeground(Color.WHITE);

        rTeamPanel.setBackground(UIColors.NIGHT_BLUE);
        teamLabel.setForeground(Color.WHITE);

        rManagePanel.setBackground(UIColors.NIGHT_BLUE);
        manageLabel.setForeground(Color.WHITE);
        manageLabel.setText("Manage");

        rEvaluationPanel.setBackground(UIColors.NIGHT_BLUE);
        evaluationLable.setForeground(Color.WHITE);
        evaluationLable.setText("Evaluation");

        rLogoutPanel.setBackground(UIColors.NIGHT_BLUE);
        logoutLabel.setForeground(Color.WHITE);

        // Logica di visibilità dinamica
        User user = controller.getCurrentUser();
        rManagePanel.setVisible(user instanceof Organizer);
        rEvaluationPanel.setVisible(user instanceof Judge);
    }

    private void createUIComponents() {
        // Obbligatorio per componenti Custom Create nel .form
        rDashboardPanel = new RoundedPanel();
        rHackathonPanel = new RoundedPanel();
        rTeamPanel = new RoundedPanel();
        rManagePanel = new RoundedPanel();
        rEvaluationPanel = new RoundedPanel();
        rLogoutPanel = new RoundedPanel();
    }

    private void setupListeners() {
        // Listener per Dashboard
        rDashboardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                cardLayout.show(cardPanel, "dashboard");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rDashboardPanel.setBackground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rDashboardPanel.setBackground(UIColors.NIGHT_BLUE);
            }
        });

        // Listener per Hackathon
        rHackathonPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                hackathonCard.refreshData(true);
                cardLayout.show(cardPanel, "hackathon");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rHackathonPanel.setBackground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rHackathonPanel.setBackground(UIColors.NIGHT_BLUE);
            }
        });

        // Listener per Team
        rTeamPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (controller.getCurrentHackathon() == null) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Devi prima iscriverti a un Hackathon dalla Dashboard!",
                            "Accesso Negato", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                teamCard.refreshData();
                cardLayout.show(cardPanel, "team");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rTeamPanel.setBackground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rTeamPanel.setBackground(UIColors.NIGHT_BLUE);
            }
        });

        // Listener per Manage (Organizer)
        rManagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                organizerCard.refreshData();
                cardLayout.show(cardPanel, "manage");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rManagePanel.setBackground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rManagePanel.setBackground(UIColors.NIGHT_BLUE);
            }
        });

        // Listener per Evaluation (Judge)
        rEvaluationPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                judgeCard.refreshData();
                cardLayout.show(cardPanel, "evaluation");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rEvaluationPanel.setBackground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rEvaluationPanel.setBackground(UIColors.NIGHT_BLUE);
            }
        });

        // Listener per Logout
        rLogoutPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dispose();
                // Assicurati che il nome della classe AuthFrame sia corretto nel tuo progetto
                // new AuthFrame(new Controller()).setVisible(true);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rLogoutPanel.setBackground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rLogoutPanel.setBackground(UIColors.NIGHT_BLUE);
            }
        });
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
        rootPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new GridLayoutManager(1, 1, new Insets(20, 20, 20, 20), -1, -1));
        rootPanel.add(sidebarPanel, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, -1), new Dimension(200, -1), 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        sidebarPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 10, 20, 0), -1, -1));
        panel1.add(menuPanel, BorderLayout.NORTH);
        menuLabel = new JLabel();
        Font menuLabelFont = this.$$$getFont$$$(null, -1, 26, menuLabel.getFont());
        if (menuLabelFont != null) menuLabel.setFont(menuLabelFont);
        menuLabel.setText("Menu");
        menuPanel.add(menuLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        containerPanel = new JPanel();
        containerPanel.setLayout(new GridLayoutManager(7, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(containerPanel, BorderLayout.CENTER);
        rDashboardPanel.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        containerPanel.add(rDashboardPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        dashboardLabel = new JLabel();
        dashboardLabel.setIcon(new ImageIcon(getClass().getResource("/icons/dashboard.png")));
        dashboardLabel.setText("Dashboard");
        rDashboardPanel.add(dashboardLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        containerPanel.add(spacer1, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        rHackathonPanel.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        containerPanel.add(rHackathonPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        hackathonLabel = new JLabel();
        hackathonLabel.setIcon(new ImageIcon(getClass().getResource("/icons/hackathon.png")));
        hackathonLabel.setText("Hackathon");
        rHackathonPanel.add(hackathonLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rTeamPanel.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        containerPanel.add(rTeamPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        teamLabel = new JLabel();
        teamLabel.setIcon(new ImageIcon(getClass().getResource("/icons/team.png")));
        teamLabel.setText("Team");
        rTeamPanel.add(teamLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rManagePanel.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        containerPanel.add(rManagePanel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        manageLabel = new JLabel();
        manageLabel.setIcon(new ImageIcon(getClass().getResource("/icons/manage.png")));
        manageLabel.setText("Manage");
        rManagePanel.add(manageLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rLogoutPanel.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        containerPanel.add(rLogoutPanel, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        logoutLabel = new JLabel();
        logoutLabel.setIcon(new ImageIcon(getClass().getResource("/icons/logout.png")));
        logoutLabel.setText("Logout");
        rLogoutPanel.add(logoutLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rEvaluationPanel.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        containerPanel.add(rEvaluationPanel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        evaluationLable = new JLabel();
        evaluationLable.setIcon(new ImageIcon(getClass().getResource("/icons/manage.png")));
        evaluationLable.setText("Evaluation");
        rEvaluationPanel.add(evaluationLable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cardPanel = new JPanel();
        cardPanel.setLayout(new CardLayout(0, 0));
        panel2.add(cardPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
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
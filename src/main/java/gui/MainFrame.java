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
import java.sql.SQLException;
import java.util.Locale;

/**
 * Finestra principale dell'applicazione (Layer Boundary).
 * <p>
 * Gestisce la navigazione tra le diverse aree funzionali tramite una sidebar interattiva
 * e un sistema di CardLayout. Consente il passaggio tra Dashboard, Hackathon, Team,
 * Manage (Organizzatori) ed Evaluation (Giudici) in base al ruolo dell'utente.
 * </p>
 * <p>
 * Funzionalità principali:
 * <ul>
 *   <li>Caricamento dello stato dell'utente loggato.</li>
 *   <li>Gestione della visibilità dei pannelli in base al ruolo (Organizer, Judge, Participant).</li>
 *   <li>Navigazione tra i vari card panel tramite click sulla sidebar.</li>
 *   <li>Effetti visivi interattivi (hover) sulla sidebar.</li>
 *   <li>Controllo dello stato di "kicked" per utenti rimossi dall'evento.</li>
 * </ul>
 * </p>
 *
 * @see Controller
 * @see DashboardCardPanel
 * @see HackathonCardPanel
 * @see TeamCardPanel
 * @see OrganizerManageCardPanel
 * @see JudgeManageCardPanel
 */
public class MainFrame extends JFrame {

    private static final String DASHBOARD_ID = "dashboard";
    private static final String DB_ERROR_TITLE = "Database Error";

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

    // Marcato come transient per risolvere SonarQube S1948 (Serialization)
    private final transient Controller controller;
    private CardLayout cardLayout;

    private HackathonCardPanel hackathonCard;
    private TeamCardPanel teamCard;
    private OrganizerManageCardPanel organizerCard;
    private JudgeManageCardPanel judgeCard;

    /**
     * Inizializza la cornice principale e carica lo stato dell'utente loggato.
     *
     * @param controller Il coordinatore della logica di business.
     */
    public MainFrame(Controller controller) {
        this.controller = controller;

        $$$setupUI$$$();
        initializeWindowProperties();
        setupCardPanel();
        customizeComponents();
        setupListeners();
        checkKickedStatus();
    }

    /**
     * Inizializza le proprietà della finestra principale.
     * <p>
     * Imposta il titolo con il nome dell'utente corrente, le dimensioni della finestra,
     * l'operazione di chiusura e la posizione rispetto allo schermo.
     * </p>
     */
    private void initializeWindowProperties() {
        String userName = "User";
        try {
            userName = controller.getCurrentUser().getName();
        } catch (Exception e) {
        }
        setTitle("Hackathon.IO - Home (@" + userName + ")");
        setSize(1000, 700);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContentPane(rootPanel);
        setLocationRelativeTo(null);
    }

    /**
     * Configura il sistema di CardLayout e aggiunge tutti i pannelli delle schede.
     * <p>
     * Crea e registra i seguenti pannelli:
     * <ul>
     *   <li>DashboardCardPanel - Visualizzazione hackathon disponibili.</li>
     *   <li>HackathonCardPanel - Dettagli evento e ranking.</li>
     *   <li>TeamCardPanel - Gestione del team.</li>
     *   <li>OrganizerManageCardPanel - Gestione per organizzatori.</li>
     *   <li>JudgeManageCardPanel - Valutazione per giudici.</li>
     * </ul>
     * Mostra il pannello Dashboard all'avvio.
     * </p>
     */
    private void setupCardPanel() {
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        DashboardCardPanel dashboardCard = new DashboardCardPanel(controller);
        cardPanel.add(dashboardCard.getRootPanel(), DASHBOARD_ID);

        hackathonCard = new HackathonCardPanel(controller);
        cardPanel.add(hackathonCard.getRootPanel(), "hackathon");

        teamCard = new TeamCardPanel(controller);
        cardPanel.add(teamCard.getRootPanel(), "team");

        organizerCard = new OrganizerManageCardPanel(controller);
        cardPanel.add(organizerCard.getRootPanel(), "manage");

        judgeCard = new JudgeManageCardPanel(controller);
        cardPanel.add(judgeCard.getRootPanel(), "evaluation");

        cardLayout.show(cardPanel, DASHBOARD_ID);
    }

    /**
     * Applica stili e colori personalizzati ai componenti UI.
     * <p>
     * Configura la palette di colori della sidebar secondo UIColors, ripristina i colori
     * di tutti i pannelli della sidebar e gestisce la visibilità dei pannelli di Manage
     * e Evaluation in base al ruolo dell'utente.
     * </p>
     */
    private void customizeComponents() {
        sidebarPanel.setBackground(UIColors.NIGHT_BLUE);
        containerPanel.setBackground(UIColors.NIGHT_BLUE);
        menuPanel.setBackground(UIColors.NIGHT_BLUE);
        menuLabel.setForeground(Color.WHITE);

        resetSidebarColors();

        try {
            User user = controller.getCurrentUser();
            rManagePanel.setVisible(user instanceof Organizer);
            rEvaluationPanel.setVisible(user instanceof Judge);
        } catch (Exception e) {
            rManagePanel.setVisible(false);
            rEvaluationPanel.setVisible(false);
        }
    }

    /**
     * Ripristina i colori standard della sidebar.
     * <p>
     * Imposta il colore di sfondo di tutti i pannelli della sidebar a NIGHT_BLUE
     * e il colore del testo a bianco.
     * </p>
     */
    private void resetSidebarColors() {
        rDashboardPanel.setBackground(UIColors.NIGHT_BLUE);
        dashboardLabel.setForeground(Color.WHITE);
        rHackathonPanel.setBackground(UIColors.NIGHT_BLUE);
        hackathonLabel.setForeground(Color.WHITE);
        rTeamPanel.setBackground(UIColors.NIGHT_BLUE);
        teamLabel.setForeground(Color.WHITE);
        rManagePanel.setBackground(UIColors.NIGHT_BLUE);
        manageLabel.setForeground(Color.WHITE);
        rEvaluationPanel.setBackground(UIColors.NIGHT_BLUE);
        evaluationLable.setForeground(Color.WHITE);
        rLogoutPanel.setBackground(UIColors.NIGHT_BLUE);
        logoutLabel.setForeground(Color.WHITE);
    }

    /**
     * Verifica se l'utente è stato rimosso dall'evento e visualizza un messaggio di avviso.
     * <p>
     * Se l'evento è iniziato senza che l'utente avesse un team, l'iscrizione viene annullata
     * e un messaggio di avviso viene mostrato all'utente.
     * </p>
     */
    private void checkKickedStatus() {
        if (controller.wasRecentlyKicked()) {
            JOptionPane.showMessageDialog(this,
                    "The event started and you didn't have a team. Your registration has been canceled.",
                    "Registration Expired", JOptionPane.WARNING_MESSAGE);
            controller.resetKickedFlag();
        }
    }

    /**
     * Configura i listener della sidebar per la navigazione tra i pannelli.
     * <p>
     * Registra i listener per:
     * <ul>
     *   <li>Dashboard - Navigazione senza refresh.</li>
     *   <li>Hackathon - Navigazione con refresh dei dati.</li>
     *   <li>Team - Navigazione con verifica di iscrizione a evento.</li>
     *   <li>Manage - Navigazione con refresh per organizzatori.</li>
     *   <li>Evaluation - Navigazione con refresh per giudici.</li>
     *   <li>Logout - Chiusura della finestra.</li>
     * </ul>
     * Gestisce gli effetti hover (cambio colore) su tutti gli elementi della sidebar.
     * </p>
     * @throws SQLException Gestita internamente con visualizzazione di messaggi di errore.
     */
    private void setupListeners() {
        rDashboardPanel.addMouseListener(new SidebarListener(rDashboardPanel, DASHBOARD_ID, null));

        rHackathonPanel.addMouseListener(new SidebarListener(rHackathonPanel, "hackathon", () -> hackathonCard.refreshData()));

        rTeamPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    if (controller.getCurrentHackathon() == null) {
                        JOptionPane.showMessageDialog(MainFrame.this, "Sign up for a Hackathon first!", "Access denied", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    teamCard.refreshData();
                    cardLayout.show(cardPanel, "team");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Connection error: " + ex.getMessage(), DB_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
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

        rManagePanel.addMouseListener(new SidebarListener(rManagePanel, "manage", () -> organizerCard.refreshData()));
        rEvaluationPanel.addMouseListener(new SidebarListener(rEvaluationPanel, "evaluation", () -> judgeCard.refreshData()));

        rLogoutPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    new AuthFrame(controller).setVisible(true);
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(rootPanel);
                    if (frame != null) frame.dispose();
                });
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
     * Classe interna per gestire i listener della sidebar.
     * <p>
     * Riduce la duplicazione di codice centralizzando la logica di navigazione,
     * refresh e effetti visivi per i pannelli della sidebar.
     * </p>
     */
    private class SidebarListener extends MouseAdapter {
        private final JPanel panel;
        private final String cardId;
        private final Runnable refreshAction;

        /**
         * Costruttore del listener della sidebar.
         *
         * @param panel Il pannello della sidebar su cui registrare il listener.
         * @param cardId L'ID della scheda da visualizzare nel CardLayout.
         * @param refreshAction Un'azione opzionale da eseguire prima della navigazione (può essere null).
         */
        public SidebarListener(JPanel panel, String cardId, Runnable refreshAction) {
            this.panel = panel;
            this.cardId = cardId;
            this.refreshAction = refreshAction;
        }

        /**
         * Gestisce l'evento di pressione del mouse sulla sidebar.
         * <p>
         * Esegue l'azione di refresh se fornita, quindi mostra la scheda corrispondente.
         * </p>
         *
         * @param e L'evento del mouse.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if (refreshAction != null) refreshAction.run();
            cardLayout.show(cardPanel, cardId);
        }

        /**
         * Gestisce l'evento di mouse che entra nella zona della sidebar.
         * <p>
         * Cambia il colore di sfondo del pannello a CARMINE_RED per evidenziare l'elemento.
         * </p>
         *
         * @param e L'evento del mouse.
         */
        @Override
        public void mouseEntered(MouseEvent e) {
            panel.setBackground(UIColors.CARMINE_RED);
        }

        /**
         * Gestisce l'evento di mouse che esce dalla zona della sidebar.
         * <p>
         * Ripristina il colore di sfondo del pannello a NIGHT_BLUE.
         * </p>
         *
         * @param e L'evento del mouse.
         */
        @Override
        public void mouseExited(MouseEvent e) {
            panel.setBackground(UIColors.NIGHT_BLUE);
        }
    }

    /**
     * Crea i componenti UI personalizzati.
     * Inizializza i pannelli arrotondati (RoundedPanel) per la sidebar e il logout.
     */
    private void createUIComponents() {
        rDashboardPanel = new RoundedPanel();
        rHackathonPanel = new RoundedPanel();
        rTeamPanel = new RoundedPanel();
        rManagePanel = new RoundedPanel();
        rEvaluationPanel = new RoundedPanel();
        rLogoutPanel = new RoundedPanel();
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


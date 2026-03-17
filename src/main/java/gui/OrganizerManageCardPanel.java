package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import controller.Controller;
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
import java.util.List;
import java.util.Locale;

/**
 * Pannello per l'Organizzatore: gestisce la promozione degli utenti nel "Limbo" a Giudici (Layer Boundary).
 * <p>
 * Nota: Sopprimiamo S1450 perché i campi Label/Panel sono necessari al GUI Designer
 * di IntelliJ per il corretto binding del file .form.
 */
@SuppressWarnings("java:S1450")
public class OrganizerManageCardPanel {
    private JPanel rootPanel;
    private JLabel manageLabel;
    private JLabel infoLabel;
    private JLabel participantsLabel;
    private JLabel participantsInfoLabel; // Ripristinato per binding .form
    private JScrollPane scrollPanel;
    private JScrollPane participantsListScrollPanel;
    private JPanel participantsListPanel;
    private JPanel rParticipantListPanel;

    private final Controller controller;

    /**
     * Costruttore del pannello di gestione organizzatore.
     *
     * @param controller Il coordinatore del layer Control.
     */
    public OrganizerManageCardPanel(Controller controller) {
        this.controller = controller;

        $$$setupUI$$$();
        customizeComponents();
        refreshData();
    }

    /**
     * Recupera gli utenti nel limbo e popola la lista grafica.
     * Gestisce la 'SQLException' per evitare blocchi dell'interfaccia.
     */
    public void refreshData() {
        participantsListPanel.removeAll();

        try {
            List<User> usersInLimbo = controller.getUsersInLimbo();

            if (usersInLimbo == null || usersInLimbo.isEmpty()) {
                addEmptyStateLabel();
            } else {
                for (User u : usersInLimbo) {
                    participantsListPanel.add(createUserCard(u));
                    participantsListPanel.add(Box.createVerticalStrut(10));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(rootPanel, "Errore caricamento dati: " + e.getMessage(), "Errore Database", JOptionPane.ERROR_MESSAGE);
        }

        participantsListPanel.revalidate();
        participantsListPanel.repaint();
    }

    private void addEmptyStateLabel() {
        JLabel emptyLabel = new JLabel("No participants waiting for promotion.");
        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        participantsListPanel.add(emptyLabel);
    }

    /**
     * Crea una card interattiva per l'utente con logica di promozione a Giudice.
     *
     * @param u L'utente da visualizzare.
     * @return Il pannello grafico della card.
     */
    private JPanel createUserCard(User u) {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel nameLabel = new JLabel("👤 " + u.getName() + " (" + u.getEmail() + ")");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        card.add(nameLabel, BorderLayout.WEST);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(UIColors.CARMINE_RED);
                nameLabel.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
                nameLabel.setForeground(Color.BLACK);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handlePromotionRequest(u);
            }
        });

        return card;
    }

    private void handlePromotionRequest(User u) {
        int response = JOptionPane.showConfirmDialog(rootPanel,
                "Promote '" + u.getName() + "' to Judge?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            try {
                controller.promoteToJudgeAction(u.getUserId());
                JOptionPane.showMessageDialog(rootPanel, "Promotion successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(rootPanel, "DB Error: " + ex.getMessage(), "Action Denied", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(rootPanel, ex.getMessage(), "Action Denied", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void customizeComponents() {
        manageLabel.setForeground(UIColors.NIGHT_BLUE);
        infoLabel.setForeground(UIColors.CARMINE_RED);
        participantsLabel.setForeground(UIColors.CARMINE_RED);

        participantsListPanel.setLayout(new BoxLayout(participantsListPanel, BoxLayout.Y_AXIS));
        participantsListPanel.setBackground(Color.WHITE);

        scrollPanel.setBorder(null);
        participantsListScrollPanel.setBorder(null);
        participantsListScrollPanel.setBackground(Color.WHITE);
    }

    private void createUIComponents() {
        rParticipantListPanel = new RoundedPanel();
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
        manageLabel = new JLabel();
        Font manageLabelFont = this.$$$getFont$$$(null, -1, 26, manageLabel.getFont());
        if (manageLabelFont != null) manageLabel.setFont(manageLabelFont);
        manageLabel.setText("Manage");
        panel3.add(manageLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        infoLabel = new JLabel();
        infoLabel.setText("This panel is reserved for Organizers to promote Judges.");
        panel4.add(infoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel2.add(separator1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPanel = new JScrollPane();
        panel1.add(scrollPanel, BorderLayout.CENTER);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        scrollPanel.setViewportView(panel5);
        participantsLabel = new JLabel();
        Font participantsLabelFont = this.$$$getFont$$$(null, -1, 18, participantsLabel.getFont());
        if (participantsLabelFont != null) participantsLabel.setFont(participantsLabelFont);
        participantsLabel.setText("Participants Waiting for Promotion");
        panel5.add(participantsLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        participantsInfoLabel = new JLabel();
        participantsInfoLabel.setText("Click on a user to promote them to Judge!");
        panel5.add(participantsInfoLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rParticipantListPanel.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        panel5.add(rParticipantListPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        participantsListScrollPanel = new JScrollPane();
        rParticipantListPanel.add(participantsListScrollPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        participantsListPanel = new JPanel();
        participantsListPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        participantsListScrollPanel.setViewportView(participantsListPanel);
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
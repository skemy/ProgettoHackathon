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
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;

public class TeamCardPanel {
    private JPanel rootPanel;
    private JLabel teamLabel;
    private JScrollPane scrollPanel;
    private JLabel infoLabel;
    private JLabel createTeamLabel;
    private JLabel joinTeamLabel;
    private JPanel rJoinTeamPanel;
    private JPanel rCreateTeamPanel;
    private JLabel membersLabel;
    private JLabel uploadsLabel;
    private JLabel addLabel;
    private JPanel rAddPanel;
    private JLabel uploadsInfoLabel;
    private JLabel membersInfoLabel;
    private JPanel membersListPanel;
    private JPanel uploadsListPanel;
    private JLabel accessCodeLabel;
    private JPanel membersInfoJPanel;

    private final Controller controller;

    public TeamCardPanel(Controller controller) {
        this.controller = controller;

        $$$setupUI$$$();
        customizeComponents();
        setupScrollPanel();
        setupAllListeners();
        refreshData();
    }

    public void refreshData() {
        User currentUser = controller.getCurrentUser();

        if (currentUser instanceof Participant) {
            Participant p = (Participant) currentUser;
            Team myTeam = controller.getMyTeam();

            if (myTeam != null) {
                infoLabel.setText("Team: " + myTeam.getTeamName());
                accessCodeLabel.setText("Codice: " + myTeam.getAccessCode());
                accessCodeLabel.setVisible(true);
            }

            rCreateTeamPanel.setVisible(false);
            rJoinTeamPanel.setVisible(false);
            rAddPanel.setVisible(true);
            membersInfoLabel.setVisible(false);
            uploadsInfoLabel.setVisible(false);

            updateMembersList();
            updateUploadsList();
        } else {
            infoLabel.setText("Non sei ancora in un team.");
            accessCodeLabel.setVisible(false);
            rCreateTeamPanel.setVisible(true);
            rJoinTeamPanel.setVisible(true);
            rAddPanel.setVisible(false);
            membersInfoLabel.setVisible(true);
            uploadsInfoLabel.setVisible(true);

            membersListPanel.removeAll();
            uploadsListPanel.removeAll();
        }

        rootPanel.revalidate();
        rootPanel.repaint();
    }

    private void setupAllListeners() {
        // 1. TASTO CREA TEAM
        rCreateTeamPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                User u = controller.getCurrentUser();

                if (u instanceof Organizer) {
                    JOptionPane.showMessageDialog(rootPanel, "Sei un Organizzatore per questo evento. Non puoi creare un team.", "Accesso Negato", JOptionPane.WARNING_MESSAGE);
                    return;
                } else if (u instanceof Judge) {
                    JOptionPane.showMessageDialog(rootPanel, "Sei un Giudice per questo evento. Non puoi creare un team.", "Accesso Negato", JOptionPane.WARNING_MESSAGE);
                    return;
                } else if (u instanceof Participant) {
                    JOptionPane.showMessageDialog(rootPanel, "Fai già parte di un team!", "Azione non consentita", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String name = JOptionPane.showInputDialog(rootPanel, "Nome del Team:", "Nuovo Team", JOptionPane.PLAIN_MESSAGE);
                if (name != null && !name.trim().isEmpty()) {
                    try {
                        controller.createTeamAction(name.trim());
                        refreshData();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(rootPanel, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rCreateTeamPanel.setBackground(UIColors.CARMINE_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rCreateTeamPanel.setBackground(UIColors.NIGHT_BLUE);
            }
        });

        // 2. TASTO JOIN TEAM
        rJoinTeamPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                User u = controller.getCurrentUser();

                if (u instanceof Organizer) {
                    JOptionPane.showMessageDialog(rootPanel, "Sei un Organizzatore. Non puoi unirti a un team.", "Accesso Negato", JOptionPane.WARNING_MESSAGE);
                    return;
                } else if (u instanceof Judge) {
                    JOptionPane.showMessageDialog(rootPanel, "Sei un Giudice. Non puoi unirti a un team.", "Accesso Negato", JOptionPane.WARNING_MESSAGE);
                    return;
                } else if (u instanceof Participant) {
                    JOptionPane.showMessageDialog(rootPanel, "Fai già parte di un team!", "Azione non consentita", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String code = JOptionPane.showInputDialog(rootPanel, "Inserisci Codice:", "Join Team", JOptionPane.PLAIN_MESSAGE);
                if (code != null && !code.trim().isEmpty()) {
                    try {
                        controller.joinTeamAction(code.trim().toUpperCase());
                        refreshData();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(rootPanel, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // 3. TASTO ADD (UPLOAD)
        rAddPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Window parent = SwingUtilities.getWindowAncestor(rootPanel);
                DocumentUploadDialog dialog = new DocumentUploadDialog((JFrame) parent, controller);
                dialog.setVisible(true);
                refreshData();
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

        // 4. LABEL CODICE (COPIA)
        accessCodeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Team myTeam = controller.getMyTeam();
                if (myTeam != null) {
                    String code = myTeam.getAccessCode();
                    StringSelection ss = new StringSelection(code);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);

                    String oldText = accessCodeLabel.getText();
                    accessCodeLabel.setText("✅ Copiato!");
                    new Timer(1000, ev -> accessCodeLabel.setText(oldText)).start();
                }
            }
        });
    }

    private void customizeComponents() {
        rCreateTeamPanel.setBackground(UIColors.NIGHT_BLUE);
        createTeamLabel.setForeground(Color.WHITE);

        rJoinTeamPanel.setBackground(Color.WHITE);
        ((RoundedPanel) rJoinTeamPanel).setBorderColor(UIColors.NIGHT_BLUE);
        joinTeamLabel.setForeground(UIColors.NIGHT_BLUE);

        rAddPanel.setBackground(UIColors.NIGHT_BLUE);
        addLabel.setForeground(Color.WHITE);

        rCreateTeamPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rJoinTeamPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rAddPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        accessCodeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void createUIComponents() {
        rCreateTeamPanel = new RoundedPanel();
        rJoinTeamPanel = new RoundedPanel();
        rAddPanel = new RoundedPanel();
    }

    private void setupScrollPanel() {
        scrollPanel.setBorder(null);
        membersListPanel.setLayout(new BoxLayout(membersListPanel, BoxLayout.Y_AXIS));
        uploadsListPanel.setLayout(new BoxLayout(uploadsListPanel, BoxLayout.Y_AXIS));
    }

    private void updateMembersList() {
        membersListPanel.removeAll();
        List<Participant> members = controller.getMyTeamMembers();
        for (Participant m : members) {
            JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT));
            card.setBackground(Color.WHITE);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            card.add(new JLabel("👤 " + m.getName() + " (" + m.getEmail() + ")"));
            membersListPanel.add(card);
        }
        membersListPanel.revalidate();
        membersListPanel.repaint();
    }

    private void updateUploadsList() {
        uploadsListPanel.removeAll();
        List<Document> docs = controller.getMyTeamDocuments();

        for (Document d : docs) {
            RoundedPanel card = new RoundedPanel();
            card.setLayout(new BorderLayout());
            card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            card.setBackground(Color.WHITE);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel docName = new JLabel("📄 " + d.getName());
            card.add(docName, BorderLayout.CENTER);

            /**
             * Apre una finestra modale (JOptionPane) per visualizzare lo storico dei feedback.
             * * NOTA ARCHITETTURALE SULLA GUI (Rendering HTML in Swing):
             * Al fine di garantire una User Experience (UX) moderna e gerarchica, questa sezione
             * non utilizza una classica JTextArea (limitata al Plain Text), ma un JEditorPane
             * configurato con content-type "text/html".
             * * - PERCHÉ: Permette l'utilizzo di stili CSS inline per differenziare cromaticamente
             * i titoli (es. rosso scuro per l'intestazione), i sottotitoli (es. blu per il nome
             * del giudice) e introdurre veri divisori grafici (<hr>) impossibili col testo semplice.
             * - COME: I dati estratti dal DB (tramite il Controller) vengono iterati e concatenati
             * in uno StringBuilder avvolti da tag HTML. Attenzione: i ritorni a capo testuali (\n)
             * generati dagli utenti vengono convertiti dinamicamente in tag <br> per preservare
             * la formattazione originale del commento a schermo.
             */
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    List<Feedback> feedbacks = controller.getDocumentFeedbacks(d.getDocumentId());

                    // Costruiamo una stringa HTML invece di testo semplice
                    StringBuilder html = new StringBuilder();
                    html.append("<html><body style='font-family: sans-serif; padding: 10px; width: 350px;'>");

                    // 1. Titolo in Rosso e più grande
                    html.append("<h2 style='color: #D32F2F; margin-top: 0;'>Comments from Judges:</h2>");

                    if (feedbacks.isEmpty()) {
                        html.append("<p style='color: gray; font-style: italic;'>No comments yet.</p>");
                    } else {
                        for (Feedback f : feedbacks) {
                            html.append("<div style='margin-bottom: 10px;'>");
                            // 2. Sottotitolo del Giudice in Blu Notte
                            html.append("<h4 style='color: #1A237E; margin: 0 0 5px 0;'>👨‍⚖️ Judge: ").append(f.getJudgeName()).append("</h4>");

                            // 3. Sezione Commento (Convertiamo gli a capo \n in <br> per l'HTML)
                            String safeComment = f.getComment().replace("\n", "<br>");
                            html.append("<p style='margin: 0; font-size: 13px;'><b>Comment:</b> <br><i>").append(safeComment).append("</i></p>");
                            html.append("</div>");

                            // 4. Linea di separazione estesa e sottile (<hr>)
                            html.append("<hr style='border: 0; border-top: 1px solid #BDBDBD; margin-bottom: 15px;'>");
                        }
                    }
                    html.append("</body></html>");

                    // Usiamo JEditorPane invece di JTextArea per leggere l'HTML
                    JEditorPane editorPane = new JEditorPane("text/html", html.toString());
                    editorPane.setEditable(false);
                    editorPane.setOpaque(false); // Sfondo trasparente per integrarsi col PopUp

                    JScrollPane scrollPane = new JScrollPane(editorPane);
                    scrollPane.setPreferredSize(new Dimension(450, 300)); // Finestra più larga
                    scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Togliamo i bordi brutti

                    JOptionPane.showMessageDialog(rootPanel, scrollPane, "Feedback History", JOptionPane.PLAIN_MESSAGE);
                }
            });
            uploadsListPanel.add(card);
            uploadsListPanel.add(Box.createVerticalStrut(5));
        }
        uploadsListPanel.revalidate();
        uploadsListPanel.repaint();
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
        panel2.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, BorderLayout.NORTH);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        teamLabel = new JLabel();
        Font teamLabelFont = this.$$$getFont$$$(null, -1, 26, teamLabel.getFont());
        if (teamLabelFont != null) teamLabel.setFont(teamLabelFont);
        teamLabel.setText("Team");
        panel4.add(teamLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        infoLabel = new JLabel();
        infoLabel.setText("You're currently not part of a team.");
        panel6.add(infoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        rJoinTeamPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel5.add(rJoinTeamPanel, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, 30), new Dimension(100, 30), new Dimension(100, 30), 0, false));
        joinTeamLabel = new JLabel();
        joinTeamLabel.setText("Join Team");
        rJoinTeamPanel.add(joinTeamLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rCreateTeamPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel5.add(rCreateTeamPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, 30), new Dimension(100, 30), new Dimension(100, 30), 0, false));
        createTeamLabel = new JLabel();
        createTeamLabel.setText("Create Team");
        rCreateTeamPanel.add(createTeamLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel7, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel7.add(separator1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel8, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        accessCodeLabel = new JLabel();
        accessCodeLabel.setText("Access Code:");
        panel8.add(accessCodeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel8.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        scrollPanel = new JScrollPane();
        scrollPanel.setVerticalScrollBarPolicy(20);
        panel1.add(scrollPanel, BorderLayout.CENTER);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(8, 1, new Insets(0, 0, 0, 0), -1, -1));
        scrollPanel.setViewportView(panel9);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 2, new Insets(10, 0, 10, 0), -1, -1));
        panel9.add(panel10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(panel11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        membersLabel = new JLabel();
        Font membersLabelFont = this.$$$getFont$$$(null, -1, 18, membersLabel.getFont());
        if (membersLabelFont != null) membersLabel.setFont(membersLabelFont);
        membersLabel.setText("Members");
        panel11.add(membersLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel10.add(spacer4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel9.add(spacer5, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        membersInfoJPanel = new JPanel();
        membersInfoJPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(membersInfoJPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        membersInfoLabel = new JLabel();
        membersInfoLabel.setText("Create or join a team to view its members!");
        membersInfoJPanel.add(membersInfoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 3, new Insets(10, 0, 10, 0), -1, -1));
        panel9.add(panel12, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel12.add(panel13, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        uploadsLabel = new JLabel();
        Font uploadsLabelFont = this.$$$getFont$$$(null, -1, 18, uploadsLabel.getFont());
        if (uploadsLabelFont != null) uploadsLabel.setFont(uploadsLabelFont);
        uploadsLabel.setText("Uploads");
        panel13.add(uploadsLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel12.add(spacer6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        rAddPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel12.add(rAddPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, 30), new Dimension(100, 30), new Dimension(100, 30), 0, false));
        addLabel = new JLabel();
        addLabel.setText("Add");
        rAddPanel.add(addLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel14, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        uploadsInfoLabel = new JLabel();
        uploadsInfoLabel.setText("Create or join a team to view or add your progress!");
        panel14.add(uploadsInfoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        uploadsListPanel = new JPanel();
        uploadsListPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(uploadsListPanel, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel15, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel15.add(panel16, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        final Spacer spacer7 = new Spacer();
        panel15.add(spacer7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        membersListPanel = new JPanel();
        membersListPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(membersListPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
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
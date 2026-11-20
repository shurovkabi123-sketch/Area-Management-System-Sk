package ui;

import model.SessionUser;
import service.ComplaintService;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

/** Resident dashboard with a clean navy/teal visual system. */
public class ResidentFrame extends JFrame {
    private final SessionUser user;
    private final ComplaintService service = new ComplaintService();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Tracking ID", "Complaint", "Category", "Status", "Submitted"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<String> category = new JComboBox<>();
    private final JTextField title = UITheme.field();
    private final JTextField location = UITheme.field();
    private final JTextArea description = new JTextArea(6, 24);
    private final JLabel total = new JLabel("0");
    private final JLabel photoName = new JLabel("No photo selected");
    private File selectedPhoto;

    public ResidentFrame(SessionUser user) {
        this.user = user;
        setTitle("Area Complaint Management System | Resident Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadCategories();
        loadRows();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 8));
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(18, 28, 18, 28)));

        JPanel identity = new JPanel();
        identity.setOpaque(false);
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));
        JLabel heading = UITheme.title("Resident Dashboard");
        JLabel sub = UITheme.subtitle("Report issues in " + user.wardName + " and track every update in one place.");
        identity.add(heading);
        identity.add(Box.createVerticalStrut(5));
        identity.add(sub);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actions.setOpaque(false);
        JLabel userPill = new JLabel("  " + user.name + "  ·  " + user.wardName + " RESIDENT  ");
        userPill.setFont(new Font("SansSerif", Font.BOLD, 12));
        userPill.setForeground(UITheme.TEAL_DARK);
        userPill.setBackground(new Color(225, 244, 244));
        userPill.setOpaque(true);
        userPill.setBorder(new EmptyBorder(10, 9, 10, 9));

        JButton change = UITheme.button("Change password", new Color(235, 241, 246));
        change.addActionListener(e -> new PasswordChangeDialog(this, user).setVisible(true));
        JButton logout = UITheme.button("Log out", UITheme.NAVY);
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        actions.add(userPill);
        actions.add(change);
        actions.add(logout);
        header.add(identity, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel buildContent() {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setBackground(UITheme.BG);
        page.setBorder(new EmptyBorder(22, 26, 24, 26));

        JPanel overview = new JPanel(new BorderLayout(16, 0));
        overview.setOpaque(false);
        JPanel intro = new JPanel();
        intro.setOpaque(false);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        JLabel welcome = new JLabel("Good to see you, " + user.name + "!");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 19));
        welcome.setForeground(UITheme.NAVY);
        JLabel hint = UITheme.subtitle("Use the form below to submit a new issue or review your existing complaints.");
        intro.add(welcome);
        intro.add(Box.createVerticalStrut(5));
        intro.add(hint);

        JPanel stat = new JPanel(new BorderLayout(10, 0));
        stat.setBackground(Color.WHITE);
        stat.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER, 1, true), new EmptyBorder(10, 16, 10, 18)));
        JLabel statLabel = new JLabel("MY COMPLAINTS");
        statLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        statLabel.setForeground(UITheme.MUTED);
        total.setFont(new Font("SansSerif", Font.BOLD, 24));
        total.setForeground(UITheme.TEAL_DARK);
        JPanel statText = new JPanel();
        statText.setOpaque(false);
        statText.setLayout(new BoxLayout(statText, BoxLayout.Y_AXIS));
        statText.add(statLabel);
        statText.add(total);
        JLabel statIcon = new JLabel("▤");
        statIcon.setFont(new Font("SansSerif", Font.BOLD, 28));
        statIcon.setForeground(UITheme.TEAL);
        stat.add(statText, BorderLayout.CENTER);
        stat.add(statIcon, BorderLayout.EAST);
        overview.add(intro, BorderLayout.CENTER);
        overview.add(stat, BorderLayout.EAST);

        JPanel split = new JPanel(new GridLayout(1, 2, 18, 0));
        split.setOpaque(false);
        split.add(buildSubmitCard());
        split.add(buildTableCard());
        page.add(overview, BorderLayout.NORTH);
        page.add(split, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildSubmitCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 14));
        card.add(sectionHeading("Submit a New Complaint", "Provide the details so the right team can investigate."), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 0, 5, 0); g.anchor = GridBagConstraints.WEST;
        addFormField(form, g, 0, "Complaint category", category);
        addFormField(form, g, 2, "Complaint title", title);
        addFormField(form, g, 4, "Location / Area", location);

        g.gridy = 6; g.weighty = 0; g.fill = GridBagConstraints.HORIZONTAL;
        form.add(UITheme.fieldLabel("Describe the issue"), g);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(UITheme.REGULAR);
        description.setForeground(UITheme.TEXT);
        description.setBorder(new EmptyBorder(10, 11, 10, 11));
        description.setBackground(Color.WHITE);
        JScrollPane descriptionScroll = new JScrollPane(description);
        descriptionScroll.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        descriptionScroll.setPreferredSize(new Dimension(300, 142));
        g.gridy = 7; g.fill = GridBagConstraints.BOTH; g.weighty = 1;
        form.add(descriptionScroll, g);

        JButton choosePhoto = UITheme.button("＋  Add photo", new Color(235, 241, 246));
        choosePhoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose complaint photo");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedPhoto = chooser.getSelectedFile();
                photoName.setText(selectedPhoto.getName() + "  ·  " + (selectedPhoto.length()/1024) + " KB");
                try {
                    java.awt.image.BufferedImage preview = javax.imageio.ImageIO.read(selectedPhoto);
                    if (preview == null) throw new Exception("Choose a valid JPG or PNG image.");
                    Image scaled = preview.getScaledInstance(220, 140, Image.SCALE_SMOOTH);
                    JOptionPane.showMessageDialog(this, new JLabel(new ImageIcon(scaled)), "Photo preview", JOptionPane.PLAIN_MESSAGE);
                } catch (Exception ex) {
                    selectedPhoto = null;
                    photoName.setText("No photo selected");
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid photo", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        photoName.setFont(new Font("SansSerif", Font.PLAIN, 11));
        photoName.setForeground(UITheme.MUTED);
        JPanel photoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        photoRow.setOpaque(false);
        photoRow.add(choosePhoto);
        photoRow.add(photoName);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(form, BorderLayout.CENTER);
        center.add(photoRow, BorderLayout.SOUTH);
        card.add(center, BorderLayout.CENTER);

        JButton submit = UITheme.button("Submit complaint  →", UITheme.TEAL);
        submit.setFont(new Font("SansSerif", Font.BOLD, 13));
        submit.addActionListener(e -> submit());
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel privacy = UITheme.subtitle("Photo evidence is optional.");
        footer.add(privacy, BorderLayout.WEST);
        footer.add(submit, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private void addFormField(JPanel panel, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy = row; g.weighty = 0; g.fill = GridBagConstraints.HORIZONTAL;
        panel.add(UITheme.fieldLabel(label), g);
        g.gridy = row + 1;
        if (field instanceof JComboBox) {
            field.setFont(UITheme.REGULAR);
            field.setBackground(Color.WHITE);
            field.setPreferredSize(new Dimension(180, 38));
        }
        panel.add(field, g);
    }

    private JPanel buildTableCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 14));
        card.add(sectionHeading("My Complaint Records", "Track submitted issues and check their current status."), BorderLayout.NORTH);
        UITheme.styleTable(table);
        table.setRowHeight(36);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(135);
        table.getColumnModel().getColumn(1).setPreferredWidth(145);
        table.getColumnModel().getColumn(2).setPreferredWidth(105);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(125);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        tableScroll.getViewport().setBackground(Color.WHITE);
        card.add(tableScroll, BorderLayout.CENTER);

        JButton refresh = UITheme.button("↻  Refresh", new Color(235, 241, 246));
        refresh.addActionListener(e -> loadRows());
        JButton evidence = UITheme.button("View complaint photos", UITheme.TEAL);
        evidence.addActionListener(e -> viewComplaintEvidence());
        JButton reopen = UITheme.button("Request reopen", UITheme.TEAL);
        reopen.addActionListener(e -> requestReopen());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(refresh);
        actions.add(evidence);
        actions.add(reopen);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(UITheme.subtitle("Select a resolved complaint if the issue is still unresolved."), BorderLayout.CENTER);
        footer.add(actions, BorderLayout.SOUTH);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCard() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER, 1, true), new EmptyBorder(18, 18, 16, 18)));
        return p;
    }

    private JPanel sectionHeading(String titleText, String subText) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(UITheme.NAVY);
        JLabel sub = UITheme.subtitle(subText);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        p.add(title);
        p.add(Box.createVerticalStrut(5));
        p.add(sub);
        return p;
    }

    private void viewComplaintEvidence() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select one of your complaints first.",
                    "No complaint selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String tracking = String.valueOf(model.getValueAt(table.convertRowIndexToModel(selected), 0));
        try {
            Object[] d = service.residentComplaintDetails(user.id, tracking);
            if (d == null) {
                JOptionPane.showMessageDialog(this, "The complaint could not be found in your account.",
                        "Complaint unavailable", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JPanel details = new JPanel(new GridLayout(0, 1, 4, 4));
            details.setBorder(new EmptyBorder(8, 8, 8, 8));
            details.add(new JLabel("Tracking ID: " + tracking));
            details.add(new JLabel("Title: " + d[0]));
            details.add(new JLabel("Category: " + d[2]));
            details.add(new JLabel("Location: " + d[3]));
            details.add(new JLabel("Status: " + d[5]));
            JTextArea desc = new JTextArea(String.valueOf(d[4]), 3, 45);
            desc.setLineWrap(true); desc.setWrapStyleWord(true); desc.setEditable(false);
            JScrollPane descScroll = new JScrollPane(desc);
            descScroll.setBorder(BorderFactory.createTitledBorder("Complaint description"));

            JTabbedPane photos = new JTabbedPane();
            photos.addTab("Submitted photo", evidencePanel(d[6] == null ? null : String.valueOf(d[6]),
                    "Resident's original complaint photo"));
            photos.addTab("After-resolution photo", evidencePanel(d[7] == null ? null : String.valueOf(d[7]),
                    "Ward Admin's resolution evidence"));
            JPanel content = new JPanel(new BorderLayout(8, 8));
            content.add(details, BorderLayout.NORTH);
            content.add(descScroll, BorderLayout.CENTER);
            content.add(photos, BorderLayout.SOUTH);
            photos.setPreferredSize(new Dimension(620, 390));
            content.setPreferredSize(new Dimension(650, 570));
            JOptionPane.showMessageDialog(this, content, "Complaint evidence - " + tracking,
                    JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) { error(ex); }
    }

    private JPanel evidencePanel(String storedPath, String titleText) {
        JPanel panel = new JPanel(new BorderLayout());
        if (storedPath == null || storedPath.isBlank() || "null".equals(storedPath)) {
            JLabel empty = new JLabel("No photo has been uploaded yet.", SwingConstants.CENTER);
            empty.setForeground(UITheme.MUTED);
            panel.add(empty, BorderLayout.CENTER);
            return panel;
        }
        File file = ComplaintService.resolveImage(storedPath);
        if (file == null || !file.isFile()) {
            JLabel missing = new JLabel("Photo file is unavailable. Ask the ward administrator to check the upload folder.",
                    SwingConstants.CENTER);
            missing.setForeground(new Color(170, 70, 60));
            panel.add(missing, BorderLayout.CENTER);
            return panel;
        }
        ImageIcon original = new ImageIcon(file.getAbsolutePath());
        int maxW = 570, maxH = 310;
        double scale = Math.min((double) maxW / original.getIconWidth(), (double) maxH / original.getIconHeight());
        Image scaled = original.getImage().getScaledInstance(
                Math.max(1, (int)(original.getIconWidth() * scale)),
                Math.max(1, (int)(original.getIconHeight() * scale)), Image.SCALE_SMOOTH);
        JLabel image = new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
        panel.add(new JLabel(titleText, SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(new JScrollPane(image), BorderLayout.CENTER);
        return panel;
    }

    private void requestReopen() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a complaint first.");
            return;
        }
        String tracking = String.valueOf(model.getValueAt(table.convertRowIndexToModel(row), 0));
        String reason = JOptionPane.showInputDialog(this, "Why should this complaint be reopened?");
        if (reason != null && !reason.isBlank()) {
            try {
                service.reopenComplaint(user.id, tracking, reason);
                JOptionPane.showMessageDialog(this, "Reopen request submitted successfully.");
                loadRows();
            } catch (Exception ex) { error(ex); }
        }
    }

    private void loadCategories() {
        try {
            category.removeAllItems();
            for (String c : service.categories()) category.addItem(c);
        } catch (Exception e) { error(e); }
    }

    private void loadRows() {
        try {
            model.setRowCount(0);
            Object[][] rows = service.residentRows(user.id);
            for (Object[] r : rows) model.addRow(r);
            total.setText(String.valueOf(rows.length));
        } catch (Exception e) { error(e); }
    }

    private void submit() {
        String t = title.getText().trim();
        String loc = location.getText().trim();
        String desc = description.getText().trim();
        if (t.isEmpty() || loc.isEmpty() || desc.isEmpty() || category.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please complete all complaint fields.");
            return;
        }
        try {
            service.submit(user, (String) category.getSelectedItem(), t, desc, loc, ComplaintService.saveImage(selectedPhoto));
            JOptionPane.showMessageDialog(this, "Complaint submitted successfully. Your tracking ID is available in the records table.");
            title.setText(""); location.setText(""); description.setText("");
            selectedPhoto = null; photoName.setText("No photo selected");
            loadRows();
        } catch (Exception e) { error(e); }
    }

    private void error(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 11));
            if (!selected) {
                String status = value == null ? "" : value.toString().toUpperCase();
                if (status.contains("RESOLVED")) { label.setForeground(new Color(28, 130, 88)); label.setBackground(new Color(232, 246, 238)); }
                else if (status.contains("REJECT")) { label.setForeground(new Color(176, 62, 62)); label.setBackground(new Color(252, 237, 237)); }
                else { label.setForeground(new Color(166, 112, 25)); label.setBackground(new Color(255, 247, 226)); }
            }
            return label;
        }
    }
}

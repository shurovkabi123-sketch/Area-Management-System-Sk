package ui;

import model.SessionUser;
import service.AuthService;
import service.ComplaintService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

/** Area-wide read-only complaint monitoring plus ward/admin management. */
public class SuperAdminFrame extends JFrame {
    private final SessionUser user;
    private final AuthService auth = new AuthService();
    private final ComplaintService complaints = new ComplaintService();

    private final DefaultTableModel wardModel = new DefaultTableModel(
            new String[]{"Ward ID", "Ward No.", "Ward Name", "Active", "Ward Admins"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable wardTable = new JTable(wardModel);

    private final DefaultTableModel complaintModel = new DefaultTableModel(
            new String[]{"ID", "Tracking ID", "Ward No.", "Ward", "Resident", "Title", "Category", "Status", "Submitted", "Photo"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable complaintTable = new JTable(complaintModel);
    private final JLabel[] statValues = new JLabel[5];

    public SuperAdminFrame(SessionUser user) {
        this.user = user;
        setTitle("ACMS | Super Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1390, 850);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(22, 24, 22, 24));
        root.add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("Area Overview & Complaints", buildOverview());
        tabs.addTab("Ward Management", buildWardManagement());
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
        refreshAll();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(UITheme.title("Super Admin Dashboard"));
        titles.add(UITheme.subtitle("Area-wide complaint monitoring and ward administration · " + user.name));
        JButton logout = UITheme.button("Log out", UITheme.NAVY);
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        header.add(titles, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);
        return header;
    }

    private JPanel buildOverview() {
        JPanel page = new JPanel(new BorderLayout(12, 14));
        page.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(1, 5, 10, 0));
        cards.setOpaque(false);
        String[] labels = {"TOTAL COMPLAINTS", "PENDING", "IN PROGRESS", "RESOLVED", "REJECTED"};
        Color[] colors = {UITheme.NAVY, new Color(177, 124, 31), new Color(42, 112, 180), new Color(35, 139, 91), new Color(177, 65, 65)};
        for (int i=0;i<labels.length;i++) {
            JPanel card = new JPanel();
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                    new EmptyBorder(12, 14, 12, 14)));
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("SansSerif", Font.BOLD, 10));
            label.setForeground(UITheme.MUTED);
            statValues[i] = new JLabel("—");
            statValues[i].setFont(new Font("SansSerif", Font.BOLD, 27));
            statValues[i].setForeground(colors[i]);
            card.add(label); card.add(Box.createVerticalStrut(7)); card.add(statValues[i]);
            cards.add(card);
        }
        JButton refresh = UITheme.button("↻ Refresh reports", UITheme.TEAL);
        refresh.addActionListener(e -> refreshAll());
        JPanel refreshRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        refreshRow.setOpaque(false); refreshRow.add(refresh);
        top.add(cards, BorderLayout.CENTER);
        top.add(refreshRow, BorderLayout.SOUTH);
        page.add(top, BorderLayout.NORTH);

        UITheme.styleTable(complaintTable);
        complaintTable.setRowHeight(30);
        complaintTable.setAutoCreateRowSorter(true);
        complaintTable.getColumnModel().getColumn(0).setMinWidth(0);
        complaintTable.getColumnModel().getColumn(0).setMaxWidth(0);
        complaintTable.getColumnModel().getColumn(0).setWidth(0);
        complaintTable.getColumnModel().getColumn(1).setPreferredWidth(145);
        complaintTable.getColumnModel().getColumn(5).setPreferredWidth(190);
        JPanel tableCard = new JPanel(new BorderLayout(8, 8));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(new EmptyBorder(14, 14, 14, 14));
        JLabel heading = new JLabel("All Ward Complaint Reports");
        heading.setFont(new Font("SansSerif", Font.BOLD, 17));
        heading.setForeground(UITheme.NAVY);
        JLabel note = UITheme.subtitle("Read-only area-wide view. Ward Admins remain responsible for updating their own complaints.");
        JPanel titlePanel = new JPanel(); titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(heading); titlePanel.add(Box.createVerticalStrut(4)); titlePanel.add(note);
        tableCard.add(titlePanel, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(complaintTable), BorderLayout.CENTER);
        JButton details = UITheme.button("View report / photos", UITheme.NAVY);
        details.addActionListener(e -> viewSelectedComplaint());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false); actions.add(details);
        tableCard.add(actions, BorderLayout.SOUTH);
        page.add(tableCard, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildWardManagement() {
        JPanel page = new JPanel(new BorderLayout(10, 12));
        page.setOpaque(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); actions.setOpaque(false);
        JButton addWard = UITheme.button("+ Add Ward", UITheme.TEAL); addWard.addActionListener(e -> addWard());
        JButton addAdmin = UITheme.button("+ Create Ward Admin", UITheme.NAVY); addAdmin.addActionListener(e -> createAdmin());
        JButton refresh = UITheme.button("Refresh", new Color(90,110,130)); refresh.addActionListener(e -> refreshWards());
        actions.add(addWard); actions.add(addAdmin); actions.add(refresh);
        UITheme.styleTable(wardTable);
        JLabel intro = UITheme.subtitle("Create wards and assign an administrator to each ward. Complaint data remains separated by ward for operational access.");
        JPanel north = new JPanel(); north.setOpaque(false); north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS)); north.add(intro); north.add(Box.createVerticalStrut(8)); north.add(actions);
        page.add(north, BorderLayout.NORTH);
        JPanel card = new JPanel(new BorderLayout()); card.setBackground(Color.WHITE); card.setBorder(new EmptyBorder(12,12,12,12)); card.add(new JScrollPane(wardTable));
        page.add(card, BorderLayout.CENTER);
        return page;
    }

    private void refreshAll() {
        try {
            int[] counts = complaints.areaStatusCounts();
            for (int i=0;i<counts.length;i++) statValues[i].setText(String.valueOf(counts[i]));
            complaintModel.setRowCount(0);
            for (Object[] row : complaints.allComplaintRows()) complaintModel.addRow(row);
            refreshWards();
        } catch (Exception e) { error(e); }
    }

    private void refreshWards() {
        try { wardModel.setRowCount(0); for (Object[] row : auth.listWards()) wardModel.addRow(row); }
        catch (Exception e) { error(e); }
    }

    private void viewSelectedComplaint() {
        int view = complaintTable.getSelectedRow();
        if (view < 0) { JOptionPane.showMessageDialog(this, "Select a complaint first."); return; }
        int row = complaintTable.convertRowIndexToModel(view);
        int id = ((Number) complaintModel.getValueAt(row, 0)).intValue();
        try {
            Object[] d = complaints.superAdminComplaintDetails(id);
            if (d == null) throw new Exception("Complaint report no longer exists.");
            JTextArea info = new JTextArea(10, 48);
            info.setEditable(false); info.setLineWrap(true); info.setWrapStyleWord(true);
            info.setText("Tracking ID: " + d[5] + "\nWard: " + d[6] + "\nStatus: " + d[4] +
                    "\nLocation: " + d[0] + "\n\nDescription:\n" + d[1]);
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.add(new JScrollPane(info), BorderLayout.CENTER);
            JPanel photos = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton original = new JButton("View submitted photo");
            original.addActionListener(e -> showImage(String.valueOf(d[2]), "Submitted complaint photo"));
            photos.add(original);
            JButton resolved = new JButton("View resolution photo");
            resolved.addActionListener(e -> showImage(String.valueOf(d[3]), "Resolution photo"));
            photos.add(resolved);
            panel.add(photos, BorderLayout.SOUTH);
            JOptionPane.showMessageDialog(this, panel, "Complaint report", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { error(e); }
    }

    private void showImage(String path, String title) {
        if (path == null || path.isBlank() || "null".equals(path)) { JOptionPane.showMessageDialog(this, "No photo is attached."); return; }
        File file = ComplaintService.resolveImage(path);
        if (file == null || !file.isFile()) { JOptionPane.showMessageDialog(this, "Photo file not found: " + path); return; }
        ImageIcon source = new ImageIcon(file.getAbsolutePath());
        Image scaled = source.getImage().getScaledInstance(720, 520, Image.SCALE_SMOOTH);
        JOptionPane.showMessageDialog(this, new JLabel(new ImageIcon(scaled)), title, JOptionPane.PLAIN_MESSAGE);
    }

    private void addWard() {
        JTextField no = new JTextField(), name = new JTextField(), desc = new JTextField();
        JPanel p = new JPanel(new GridLayout(0,1,5,5));
        p.add(new JLabel("Ward number (e.g. 06)")); p.add(no); p.add(new JLabel("Ward name")); p.add(name); p.add(new JLabel("Description")); p.add(desc);
        if (JOptionPane.showConfirmDialog(this,p,"Create Ward",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            try { auth.createWard(no.getText(),name.getText(),desc.getText()); refreshWards(); }
            catch(Exception e) { error(e); }
        }
    }

    private void createAdmin() {
        java.util.List<Object[]> wards;
        try { wards = auth.listWards(); } catch(Exception e) { error(e); return; }
        if (wards.isEmpty()) { JOptionPane.showMessageDialog(this,"Create a ward first."); return; }
        JComboBox<String> ward = new JComboBox<>(); java.util.Map<String,Integer> ids = new java.util.LinkedHashMap<>();
        for (Object[] w : wards) { String label="Ward "+w[1]+" - "+w[2]; ward.addItem(label); ids.put(label,((Number)w[0]).intValue()); }
        JTextField name=new JTextField(),email=new JTextField(),phone=new JTextField(),address=new JTextField(); JPasswordField pass=new JPasswordField();
        JPanel p=new JPanel(new GridLayout(0,1,4,4));
        p.add(new JLabel("Assign ward"));p.add(ward);p.add(new JLabel("Administrator full name"));p.add(name);p.add(new JLabel("Email"));p.add(email);p.add(new JLabel("Phone"));p.add(phone);p.add(new JLabel("Address"));p.add(address);p.add(new JLabel("Temporary password (minimum 8 characters)"));p.add(pass);
        if(JOptionPane.showConfirmDialog(this,p,"Create Ward Administrator",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            try { auth.createWardAdmin(name.getText(),email.getText(),phone.getText(),address.getText(),new String(pass.getPassword()),ids.get(ward.getSelectedItem())); JOptionPane.showMessageDialog(this,"Ward Admin account created successfully."); refreshWards(); }
            catch(Exception e) { error(e); }
        }
    }

    private void error(Exception e) { JOptionPane.showMessageDialog(this,e.getMessage(),"Operation failed",JOptionPane.ERROR_MESSAGE); }
}

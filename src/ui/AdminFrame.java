package ui;

import model.SessionUser;
import service.ComplaintService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.regex.Pattern;

public class AdminFrame extends JFrame {
    private final SessionUser user;
    private final ComplaintService service = new ComplaintService();
    private final JPanel content = new JPanel(new BorderLayout());
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Tracking ID", "Resident", "Complaint", "Category", "Department", "Status", "Submitted"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField search = UITheme.field();
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All statuses", "PENDING", "IN_PROGRESS", "RESOLVED", "REJECTED"});
    private final JTextField idField = UITheme.field();
    private final JTextField residentField = UITheme.field();
    private final JTextField titleField = UITheme.field();
    private final JTextField categoryField = UITheme.field();
    private final JTextField locationField = UITheme.field();
    private final JComboBox<String> departmentField = new JComboBox<>();
    private final JComboBox<String> statusField = new JComboBox<>(new String[]{"PENDING", "IN_PROGRESS", "RESOLVED", "REJECTED"});
    private final JTextArea descriptionField = new JTextArea(4, 18);
    private final JTextArea remarksField = new JTextArea(3, 18);
    private JLabel totalLabel = new JLabel("Total complaints: 0");
    private TableRowSorter<DefaultTableModel> sorter;
    private java.io.File resolutionPhoto;
    private final JLabel resolutionPhotoName = new JLabel("No resolution photo selected");

    public AdminFrame(SessionUser user) {
        this.user = user;
        setTitle("Area Complaint Management System | Administrator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1320, 790);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(), BorderLayout.CENTER);
        loadDepartments();
        showDashboard();
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(UITheme.NAVY);
        side.setPreferredSize(new Dimension(218, 0));
        side.setBorder(new EmptyBorder(22, 14, 18, 14));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel("  ACMS"); logo.setFont(new Font("SansSerif", Font.BOLD, 23)); logo.setForeground(Color.WHITE);
        JLabel sub = new JLabel("  AREA SERVICES"); sub.setFont(new Font("SansSerif", Font.PLAIN, 10)); sub.setForeground(new Color(175, 203, 216));
        side.add(logo); side.add(Box.createVerticalStrut(4)); side.add(sub); side.add(Box.createVerticalStrut(34));
        side.add(nav("▦   Dashboard", this::showDashboard));
        side.add(nav("▤   Complaint Management", this::showComplaints));
        side.add(nav("⌖   Department Tracking", this::showComplaints));
        side.add(nav("▥   Reports", this::showReports));
        side.add(Box.createVerticalGlue());
        JLabel userLabel = new JLabel("  " + user.name + "  (" + user.wardName + " Admin)");
        userLabel.setForeground(Color.WHITE); userLabel.setFont(UITheme.BOLD);
        side.add(userLabel); side.add(Box.createVerticalStrut(8));
        JButton change = UITheme.button("Change password", new Color(79, 101, 123));
        change.setAlignmentX(Component.LEFT_ALIGNMENT); change.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        change.addActionListener(e -> new PasswordChangeDialog(this,user).setVisible(true)); side.add(change); side.add(Box.createVerticalStrut(8));
        JButton logout = UITheme.button("Log out", new Color(79, 101, 123));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT); logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 39));
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        side.add(logout);
        return side;
    }

    private JButton nav(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT); b.setFont(UITheme.BOLD);
        b.setForeground(new Color(232, 241, 246)); b.setBackground(UITheme.NAVY);
        b.setBorder(new EmptyBorder(12, 10, 12, 6)); b.setFocusPainted(false);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46)); b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> action.run());
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(UITheme.TEAL_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(UITheme.NAVY); }
        });
        return b;
    }

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout()); main.setBackground(UITheme.BG);
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(15, 24, 15, 24));
        JLabel brand = new JLabel("Area Complaint Management System"); brand.setFont(new Font("SansSerif", Font.BOLD, 17)); brand.setForeground(UITheme.NAVY);
        JLabel badge = new JLabel("●  " + user.name + "  |  " + user.wardName + "   "); badge.setForeground(UITheme.TEAL_DARK); badge.setFont(UITheme.BOLD);
        header.add(brand, BorderLayout.WEST); header.add(badge, BorderLayout.EAST);
        content.setOpaque(false);
        main.add(header, BorderLayout.NORTH); main.add(content, BorderLayout.CENTER);
        return main;
    }

    private void switchContent(JComponent panel) {
        content.removeAll(); content.add(panel, BorderLayout.CENTER); content.revalidate(); content.repaint();
    }

    private void showDashboard() {
        JPanel page = UITheme.page();
        JPanel heading = new JPanel(); heading.setOpaque(false); heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(UITheme.title("Dashboard")); heading.add(Box.createVerticalStrut(5)); heading.add(UITheme.subtitle("" + user.wardName + " · Overview of complaints and service response in your ward"));
        page.add(heading, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(14, 14)); body.setOpaque(false);
        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 12)); stats.setOpaque(false);
        try {
            Object[][] rows = service.adminRows(user.wardId);
            int pending=0, progress=0, resolved=0, rejected=0;
            for (Object[] r : rows) {
                String s = String.valueOf(r[6]);
                if ("PENDING".equals(s)) pending++; else if ("IN_PROGRESS".equals(s)) progress++;
                else if ("RESOLVED".equals(s)) resolved++; else if ("REJECTED".equals(s)) rejected++;
            }
            stats.add(statCard("TOTAL COMPLAINTS", String.valueOf(rows.length), UITheme.NAVY));
            stats.add(statCard("PENDING", String.valueOf(pending), UITheme.AMBER));
            stats.add(statCard("IN PROGRESS", String.valueOf(progress), UITheme.TEAL));
            stats.add(statCard("RESOLVED", String.valueOf(resolved), UITheme.GREEN));
            body.add(stats, BorderLayout.NORTH);
            JPanel recent = UITheme.card(); recent.setLayout(new BorderLayout(8, 10));
            JLabel recentTitle = new JLabel("Recent complaints"); recentTitle.setFont(new Font("SansSerif", Font.BOLD, 16)); recentTitle.setForeground(UITheme.NAVY);
            recent.add(recentTitle, BorderLayout.NORTH);
            DefaultTableModel recentModel = new DefaultTableModel(new String[]{"Tracking ID", "Resident", "Complaint", "Department", "Status", "Submitted"}, 0) {
                public boolean isCellEditable(int r,int c){return false;}
            };
            for (int i=0; i<Math.min(rows.length, 8); i++) {
                Object[] r=rows[i]; recentModel.addRow(new Object[]{r[1],r[2],r[3],r[5],r[6],r[7]});
            }
            JTable recentTable = new JTable(recentModel); UITheme.styleTable(recentTable);
            recent.add(new JScrollPane(recentTable), BorderLayout.CENTER);
            JButton all = UITheme.button("Open complaint management", UITheme.TEAL);
            all.addActionListener(e -> showComplaints());
            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); foot.setOpaque(false); foot.add(all); recent.add(foot, BorderLayout.SOUTH);
            body.add(recent, BorderLayout.CENTER);
        } catch (Exception e) { body.add(errorPanel(e), BorderLayout.CENTER); }
        page.add(body, BorderLayout.CENTER); switchContent(page);
    }

    private JPanel statCard(String label, String value, Color accent) {
        JPanel p = UITheme.card(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label); l.setFont(new Font("SansSerif", Font.BOLD, 11)); l.setForeground(UITheme.MUTED);
        JLabel v = new JLabel(value); v.setFont(new Font("SansSerif", Font.BOLD, 30)); v.setForeground(accent);
        p.add(l); p.add(Box.createVerticalStrut(9)); p.add(v); return p;
    }

    private void showComplaints() {
        JPanel page = UITheme.page();
        JPanel heading = new JPanel(); heading.setOpaque(false); heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(UITheme.title("Complaint Management")); heading.add(Box.createVerticalStrut(5));
        heading.add(UITheme.subtitle("Manage and resolve complaints submitted within " + user.wardName));
        JPanel toolbar = new JPanel(new BorderLayout(8, 8)); toolbar.setOpaque(false);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0)); filters.setOpaque(false);
        search.setPreferredSize(new Dimension(230, 36)); search.setToolTipText("Search tracking ID, resident, title or category");
        JButton filter = UITheme.button("Filter", UITheme.TEAL); JButton reset = UITheme.button("Reset", new Color(235,241,246));
        statusFilter.setFont(UITheme.REGULAR); statusFilter.setPreferredSize(new Dimension(145,36));
        filters.add(new JLabel("⌕")); filters.add(search); filters.add(statusFilter); filters.add(filter); filters.add(reset);
        totalLabel.setFont(UITheme.BOLD); totalLabel.setForeground(UITheme.NAVY);
        toolbar.add(filters, BorderLayout.WEST); toolbar.add(totalLabel, BorderLayout.EAST);
        JPanel topBlock = new JPanel(new BorderLayout(0, 14)); topBlock.setOpaque(false);
        topBlock.add(heading, BorderLayout.NORTH); topBlock.add(toolbar, BorderLayout.SOUTH);
        page.add(topBlock, BorderLayout.NORTH);

        JPanel split = new JPanel(new GridLayout(1,2,14,0)); split.setOpaque(false);
        split.add(buildDetailsForm()); split.add(buildDirectory());
        page.add(split, BorderLayout.CENTER);
        filter.addActionListener(e -> applyFilter()); reset.addActionListener(e -> { search.setText(""); statusFilter.setSelectedIndex(0); applyFilter(); });
        search.addActionListener(e -> applyFilter()); statusFilter.addActionListener(e -> applyFilter());
        loadComplaints(); switchContent(page);
    }

    private JPanel buildDetailsForm() {
        JPanel card = UITheme.card(); card.setLayout(new BorderLayout(8, 10));
        JLabel h = new JLabel("Complaint Details"); h.setFont(new Font("SansSerif", Font.BOLD, 16)); h.setForeground(UITheme.NAVY);
        card.add(h, BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(4,3,4,3); g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1;
        addField(fields,g,0,"Complaint ID",idField); addField(fields,g,1,"Resident",residentField);
        addField(fields,g,2,"Complaint title",titleField); addField(fields,g,3,"Category",categoryField);
        addField(fields,g,4,"Location / Area",locationField);
        g.gridx=0;g.gridy=10;g.gridwidth=2;fields.add(UITheme.fieldLabel("Description"),g);
        descriptionField.setLineWrap(true);descriptionField.setWrapStyleWord(true);descriptionField.setFont(UITheme.REGULAR);descriptionField.setEditable(false);
        JScrollPane ds=new JScrollPane(descriptionField);ds.setPreferredSize(new Dimension(260,72));g.gridy=11;fields.add(ds,g);
        g.gridy=12;fields.add(UITheme.fieldLabel("Assigned department"),g);g.gridy=13;fields.add(departmentField,g);
        g.gridy=14;fields.add(UITheme.fieldLabel("Status"),g);g.gridy=15;fields.add(statusField,g);
        g.gridy=16;fields.add(UITheme.fieldLabel("Admin remarks / update note"),g);
        remarksField.setLineWrap(true);remarksField.setWrapStyleWord(true);remarksField.setFont(UITheme.REGULAR);
        JScrollPane rs=new JScrollPane(remarksField);rs.setPreferredSize(new Dimension(260,58));g.gridy=17;fields.add(rs,g);
        JScrollPane fieldScroll = new JScrollPane(fields); fieldScroll.setBorder(null); fieldScroll.getViewport().setBackground(Color.WHITE);
        card.add(fieldScroll,BorderLayout.CENTER);
        JButton chooseResolution=UITheme.button("Attach after-resolution photo (required to resolve)",new Color(235,241,246));
        chooseResolution.addActionListener(e->{JFileChooser fc=new JFileChooser();fc.setDialogTitle("Choose resolution evidence photo");if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){resolutionPhoto=fc.getSelectedFile();resolutionPhotoName.setText(resolutionPhoto.getName());}});
        JPanel evidence=new JPanel(new FlowLayout(FlowLayout.LEFT));evidence.setOpaque(false);evidence.add(chooseResolution);evidence.add(resolutionPhotoName);
        JButton viewEvidence=UITheme.button("View submitted photo",new Color(235,241,246));
        viewEvidence.addActionListener(e->viewComplaintPhoto(false));
        JButton viewResolution=UITheme.button("View resolution photo",new Color(225,240,245));
        viewResolution.addActionListener(e->viewComplaintPhoto(true));
        JPanel evidenceWrap=new JPanel();evidenceWrap.setOpaque(false);evidenceWrap.setLayout(new BoxLayout(evidenceWrap,BoxLayout.Y_AXIS));
        evidenceWrap.add(evidence);evidenceWrap.add(viewEvidence);evidenceWrap.add(viewResolution);
        JButton update=UITheme.button("Update complaint",UITheme.TEAL);
        JButton history=UITheme.button("View history",UITheme.NAVY);
        JButton clear=UITheme.button("Clear",new Color(235,241,246));
        update.addActionListener(e->updateSelected()); history.addActionListener(e->viewHistory()); clear.addActionListener(e->clearForm());
        JPanel south=new JPanel(new BorderLayout());south.setOpaque(false);south.add(evidenceWrap,BorderLayout.NORTH);
        JPanel buttons=new JPanel(new GridLayout(1,3,7,0));buttons.setOpaque(false);buttons.add(update);buttons.add(history);buttons.add(clear);south.add(buttons,BorderLayout.SOUTH);
        card.add(south,BorderLayout.SOUTH); return card;
    }

    private void addField(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx=0;g.gridy=row*2;g.gridwidth=2;p.add(UITheme.fieldLabel(label),g);
        if(field instanceof JTextField) ((JTextField)field).setEditable(false);
        g.gridy=row*2+1;p.add(field,g);
    }

    private JPanel buildDirectory() {
        JPanel card=UITheme.card();card.setLayout(new BorderLayout(8,10));
        JLabel h=new JLabel("Registered Complaints Directory");h.setFont(new Font("SansSerif",Font.BOLD,16));h.setForeground(UITheme.NAVY);
        JLabel note=UITheme.subtitle("Select a row to view details and update its assignment");
        JPanel north=new JPanel();north.setOpaque(false);north.setLayout(new BoxLayout(north,BoxLayout.Y_AXIS));north.add(h);north.add(Box.createVerticalStrut(4));north.add(note);
        card.add(north,BorderLayout.NORTH);
        UITheme.styleTable(table);table.getColumnModel().getColumn(0).setMinWidth(0);table.getColumnModel().getColumn(0).setMaxWidth(0);table.getColumnModel().getColumn(0).setWidth(0);
        sorter=new TableRowSorter<>(model);table.setRowSorter(sorter);
        table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())populateForm();});
        card.add(new JScrollPane(table),BorderLayout.CENTER);return card;
    }

    private void loadDepartments() {
        try { departmentField.removeAllItems(); for(String d:service.departments())departmentField.addItem(d); }
        catch(Exception e){ JOptionPane.showMessageDialog(this,e.getMessage(),"Departments unavailable",JOptionPane.ERROR_MESSAGE); }
    }

    private void loadComplaints() {
        try {
            model.setRowCount(0); Object[][] rows=service.adminRows(user.wardId);
            for(Object[] r:rows)model.addRow(r);
            totalLabel.setText("Total complaints: " + rows.length);
            applyFilter();
        } catch(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Could not load complaints",JOptionPane.ERROR_MESSAGE);}
    }

    private void applyFilter() {
        if(sorter==null)return;
        String q=search.getText().trim();String status=(String)statusFilter.getSelectedItem();
        sorter.setRowFilter(new RowFilter<DefaultTableModel,Integer>() {
            public boolean include(Entry<? extends DefaultTableModel,? extends Integer> entry) {
                boolean textMatch=q.isEmpty();
                if(!textMatch){for(int i=1;i<entry.getValueCount();i++){Object v=entry.getValue(i);if(v!=null&&v.toString().toLowerCase().contains(q.toLowerCase())){textMatch=true;break;}}}
                boolean statusMatch="All statuses".equals(status)||status.equals(entry.getStringValue(6));
                return textMatch&&statusMatch;
            }
        });
    }

    private void populateForm() {
        int view=table.getSelectedRow();if(view<0)return;int row=table.convertRowIndexToModel(view);
        idField.setText(String.valueOf(model.getValueAt(row,0)));residentField.setText(String.valueOf(model.getValueAt(row,2)));
        titleField.setText(String.valueOf(model.getValueAt(row,3)));categoryField.setText(String.valueOf(model.getValueAt(row,4)));
        String dept=String.valueOf(model.getValueAt(row,5));departmentField.setSelectedItem(dept);
        statusField.setSelectedItem(String.valueOf(model.getValueAt(row,6)));
        try {
            // Fetch the full description/location for the selected record.
            Object[] details=service.complaintDetails(Integer.parseInt(idField.getText()), user.wardId);
            if(details!=null){locationField.setText(String.valueOf(details[0]));descriptionField.setText(String.valueOf(details[1]));}
        } catch(Exception ex){ locationField.setText(""); descriptionField.setText(""); }
        remarksField.setText("");
    }

    private void updateSelected() {
        if(idField.getText().isBlank()){JOptionPane.showMessageDialog(this,"Select a complaint from the directory first.");return;}
        try {
            String dept=(String)departmentField.getSelectedItem();
            String photoPath=ComplaintService.saveImage(resolutionPhoto);
            service.updateComplaint(user.id,user.wardId,Integer.parseInt(idField.getText()),(String)statusField.getSelectedItem(),dept,remarksField.getText().trim(),photoPath);
            resolutionPhoto=null;resolutionPhotoName.setText("No resolution photo selected");
            JOptionPane.showMessageDialog(this,"Complaint assignment/status updated, history recorded and resident notified by email when SMTP is configured.");
            loadComplaints();clearForm();
        } catch(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Update failed",JOptionPane.ERROR_MESSAGE);}
    }



    private void viewComplaintPhoto(boolean resolution) {
        if(idField.getText().isBlank()){JOptionPane.showMessageDialog(this,"Select a complaint first.");return;}
        try {
            Object[] d=service.complaintDetails(Integer.parseInt(idField.getText()), user.wardId);
            if(d==null)return;
            String path=d[resolution?3:2]==null?null:String.valueOf(d[resolution?3:2]);
            if(path==null||path.isBlank()){JOptionPane.showMessageDialog(this,resolution?"No after-resolution photo has been uploaded.":"No complaint photo was attached.");return;}
            java.io.File file=ComplaintService.resolveImage(path);
            if(file==null||!file.isFile()){JOptionPane.showMessageDialog(this,"Photo file not found: "+path);return;}
            ImageIcon source=new ImageIcon(file.getAbsolutePath());
            Image scaled=source.getImage().getScaledInstance(650,440,Image.SCALE_SMOOTH);
            JOptionPane.showMessageDialog(this,new JLabel(new ImageIcon(scaled)),resolution?"After-resolution photo":"Submitted complaint photo",JOptionPane.PLAIN_MESSAGE);
        } catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Photo unavailable",JOptionPane.ERROR_MESSAGE);}
    }

    private void viewHistory() {
        if (idField.getText().isBlank()) { JOptionPane.showMessageDialog(this, "Select a complaint first."); return; }
        try {
            Object[][] rows = service.historyRows(Integer.parseInt(idField.getText()), user.wardId);
            DefaultTableModel historyModel = new DefaultTableModel(
                    new String[]{"Old status", "New status", "Previous department", "New department", "Updated by", "Remarks", "Date"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (Object[] row : rows) historyModel.addRow(row);
            JTable historyTable = new JTable(historyModel); UITheme.styleTable(historyTable);
            JScrollPane scroll = new JScrollPane(historyTable); scroll.setPreferredSize(new Dimension(850, 300));
            JOptionPane.showMessageDialog(this, scroll, "Complaint History - " + idField.getText(), JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "History unavailable", JOptionPane.ERROR_MESSAGE); }
    }

    private void clearForm(){table.clearSelection();idField.setText("");residentField.setText("");titleField.setText("");categoryField.setText("");locationField.setText("");descriptionField.setText("");remarksField.setText("");}

    private void showReports() {
        JPanel page=UITheme.page();JPanel head=new JPanel();head.setOpaque(false);head.setLayout(new BoxLayout(head,BoxLayout.Y_AXIS));
        head.add(UITheme.title("Reports & Analytics"));head.add(Box.createVerticalStrut(5));head.add(UITheme.subtitle("Complaint volume and resolution summary for " + user.wardName));page.add(head,BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(12,12));body.setOpaque(false);
        try {
            Object[][] rows=service.adminRows(user.wardId);int pending=0,progress=0,resolved=0,rejected=0;
            for(Object[] r:rows){switch(String.valueOf(r[6])){case "PENDING":pending++;break;case "IN_PROGRESS":progress++;break;case "RESOLVED":resolved++;break;case "REJECTED":rejected++;break;}}
            JPanel cards=new JPanel(new GridLayout(1,4,12,12));cards.setOpaque(false);
            cards.add(statCard("TOTAL",String.valueOf(rows.length),UITheme.NAVY));cards.add(statCard("PENDING",String.valueOf(pending),UITheme.AMBER));
            cards.add(statCard("IN PROGRESS",String.valueOf(progress),UITheme.TEAL));cards.add(statCard("RESOLVED",String.valueOf(resolved),UITheme.GREEN));body.add(cards,BorderLayout.NORTH);
            JPanel detail=UITheme.card();detail.setLayout(new BorderLayout(8,8));JLabel label=new JLabel("Status breakdown");label.setFont(new Font("SansSerif",Font.BOLD,16));label.setForeground(UITheme.NAVY);detail.add(label,BorderLayout.NORTH);
            DefaultTableModel m=new DefaultTableModel(new String[]{"Status","Number of complaints"},0);m.addRow(new Object[]{"Pending",pending});m.addRow(new Object[]{"In progress",progress});m.addRow(new Object[]{"Resolved",resolved});m.addRow(new Object[]{"Rejected",rejected});
            JTable t=new JTable(m);UITheme.styleTable(t);detail.add(new JScrollPane(t),BorderLayout.CENTER);
            JLabel total=new JLabel("Total records analysed: "+rows.length);total.setFont(UITheme.BOLD);total.setForeground(UITheme.MUTED);detail.add(total,BorderLayout.SOUTH);body.add(detail,BorderLayout.CENTER);
        }catch(Exception e){body.add(errorPanel(e),BorderLayout.CENTER);}
        page.add(body,BorderLayout.CENTER);switchContent(page);
    }

    private JPanel errorPanel(Exception e){JPanel p=UITheme.card();p.add(new JLabel("Unable to load data: "+e.getMessage()));return p;}
}

package ui;

import model.SessionUser;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Modern two-panel sign-in screen for the Area Complaint Management System. */
public class LoginFrame extends JFrame {

    private static final Color NAVY = new Color(8, 31, 69);
    private static final Color NAVY_LIGHT = new Color(13, 54, 99);
    private static final Color TEAL = new Color(0, 174, 165);
    private static final Color TEAL_DARK = new Color(0, 130, 133);
    private static final Color INK = new Color(19, 40, 72);
    private static final Color MUTED = new Color(105, 123, 146);
    private static final Color FIELD_BORDER = new Color(203, 216, 232);
    private static final Color RIGHT_BG = new Color(238, 244, 251);

    private final AuthService auth = new AuthService();
    private final JTextField email = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private boolean passwordVisible;

    public LoginFrame() {
        super("Area Complaint Management System");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setSize(1440, 880);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridLayout(1, 2, 0, 0));
        root.add(createBrandPanel());
        root.add(createLoginSide());
        setContentPane(root);
        getRootPane().setDefaultButton(createDefaultLoginButtonReference());
    }

    // The button is created in createLoginSide; this method retrieves the root default button.
    private JButton createDefaultLoginButtonReference() {
        return (JButton) getRootPane().getClientProperty("acms.loginButton");
    }

    private JPanel createBrandPanel() {
        BrandPanel panel = new BrandPanel();
        panel.setLayout(new GridBagLayout());

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        // Updated community/area-management icon, followed by the project title.
        java.net.URL logoResource = LoginFrame.class.getResource(
                "/icons/area_management_logo.png");

        JLabel logoLabel;
        if (logoResource != null) {
            ImageIcon sourceLogo = new ImageIcon(logoResource);
            Image scaledLogo = sourceLogo.getImage().getScaledInstance(
                    430, 430, Image.SCALE_SMOOTH);
            logoLabel = new JLabel(new ImageIcon(scaledLogo));
        } else {
            logoLabel = new JLabel("AREA MANAGEMENT");
            logoLabel.setForeground(Color.WHITE);
            logoLabel.setFont(new Font("SansSerif", Font.BOLD, 25));
        }
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleMain = new JLabel("AREA COMPLAINT", SwingConstants.CENTER);
        titleMain.setFont(new Font("SansSerif", Font.BOLD, 39));
        titleMain.setForeground(Color.WHITE);
        titleMain.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleSub = new JLabel("MANAGEMENT SYSTEM", SwingConstants.CENTER);
        titleSub.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleSub.setForeground(new Color(190, 220, 243));
        titleSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        stack.add(Box.createVerticalGlue());
        stack.add(logoLabel);
        stack.add(Box.createVerticalStrut(16));
        stack.add(titleMain);
        stack.add(Box.createVerticalStrut(4));
        stack.add(titleSub);
        stack.add(Box.createVerticalGlue());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(35, 24, 35, 24);
        panel.add(stack, c);
        return panel;
    }

    private JPanel accentLine() {
        JPanel line = new JPanel();
        line.setBackground(TEAL);
        line.setPreferredSize(new Dimension(52, 2));
        line.setMaximumSize(new Dimension(52, 2));
        return line;
    }

    private JPanel createLoginSide() {
        JPanel side = new JPanel(new GridBagLayout());
        side.setBackground(RIGHT_BG);
        side.setBorder(new EmptyBorder(26, 24, 26, 24));

        LoginCard card = new LoginCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 38, 30, 38));
        card.setPreferredSize(new Dimension(590, 760));
        card.setMaximumSize(new Dimension(620, 780));

        // Centered content column: header and complete login form share one center axis.
        JPanel centerColumn = new JPanel();
        centerColumn.setOpaque(false);
        centerColumn.setLayout(new BoxLayout(centerColumn, BoxLayout.Y_AXIS));
        centerColumn.setPreferredSize(new Dimension(440, 630));
        centerColumn.setMaximumSize(new Dimension(440, 650));
        centerColumn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Header: emblem, heading and subtitle all horizontally centered.
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.setMaximumSize(new Dimension(440, 165));

        IconTile emblem = new IconTile(new PeopleIcon(), new Color(220, 243, 245));
        emblem.setAlignmentX(Component.CENTER_ALIGNMENT);
        emblem.setPreferredSize(new Dimension(78, 68));
        emblem.setMinimumSize(new Dimension(78, 68));
        emblem.setMaximumSize(new Dimension(78, 68));

        JLabel heading = new JLabel("Welcome back", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 38));
        heading.setForeground(INK);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.setMaximumSize(new Dimension(440, 48));

        JLabel subheading = new JLabel("Sign in to continue to your account.", SwingConstants.CENTER);
        subheading.setFont(new Font("SansSerif", Font.PLAIN, 17));
        subheading.setForeground(MUTED);
        subheading.setAlignmentX(Component.CENTER_ALIGNMENT);
        subheading.setMaximumSize(new Dimension(440, 28));

        headerPanel.add(emblem);
        headerPanel.add(Box.createVerticalStrut(13));
        headerPanel.add(heading);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(subheading);

        centerColumn.add(headerPanel);
        centerColumn.add(Box.createVerticalStrut(27));

        // Fixed-width form column centered in the card. Labels remain left-aligned
        // with the input fields; the form itself is centered as a single unit.
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setPreferredSize(new Dimension(430, 440));
        formPanel.setMinimumSize(new Dimension(430, 440));
        formPanel.setMaximumSize(new Dimension(430, 440));
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Email label and field.
        formPanel.add(fieldLabel(new MailIcon(), "Email address"));
        formPanel.add(Box.createVerticalStrut(8));
        styleTextField(email, "Enter your email address");
        formPanel.add(email);
        formPanel.add(Box.createVerticalStrut(14));

        // Password label and field with a vector eye toggle.
        formPanel.add(fieldLabel(new LockIcon(), "Password"));
        formPanel.add(Box.createVerticalStrut(8));
        JPanel passwordShell = new JPanel(new BorderLayout(8, 0));
        passwordShell.setBackground(Color.WHITE);
        passwordShell.setBorder(new RoundedLineBorder(FIELD_BORDER, 1, 12));
        passwordShell.setMaximumSize(new Dimension(430, 48));
        passwordShell.setMinimumSize(new Dimension(430, 48));
        passwordShell.setPreferredSize(new Dimension(430, 48));
        passwordShell.setAlignmentX(Component.LEFT_ALIGNMENT);
        password.setFont(new Font("SansSerif", Font.PLAIN, 16));
        password.setEchoChar('\u2022');
        password.setBorder(new EmptyBorder(9, 12, 9, 4));
        password.setOpaque(false);
        password.putClientProperty("JTextField.placeholderText", "Enter your password");
        JButton toggle = new JButton(new EyeIcon());
        toggle.setToolTipText("Show or hide password");
        toggle.setBorderPainted(false);
        toggle.setContentAreaFilled(false);
        toggle.setFocusPainted(false);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.setPreferredSize(new Dimension(42, 40));
        toggle.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            password.setEchoChar(passwordVisible ? (char) 0 : '\u2022');
            toggle.repaint();
        });
        passwordShell.add(password, BorderLayout.CENTER);
        passwordShell.add(toggle, BorderLayout.EAST);
        formPanel.add(passwordShell);

        // Forgot password is right-aligned to the form's right edge.
        JPanel forgotRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        forgotRow.setOpaque(false);
        forgotRow.setPreferredSize(new Dimension(430, 30));
        forgotRow.setMinimumSize(new Dimension(430, 30));
        forgotRow.setMaximumSize(new Dimension(430, 30));
        forgotRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton forgot = textButton("Forgot password?");
        forgot.addActionListener(e -> new PasswordResetDialog(this).setVisible(true));
        forgotRow.add(forgot);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(forgotRow);
        formPanel.add(Box.createVerticalStrut(10));

        JButton login = new ActionButton("Sign in", new LoginArrowIcon(), TEAL_DARK, Color.WHITE);
        login.setMaximumSize(new Dimension(430, 53));
        login.setMinimumSize(new Dimension(430, 53));
        login.addActionListener(e -> doLogin());
        formPanel.add(login);
        formPanel.add(Box.createVerticalStrut(9));

        JButton register = new ActionButton("Resident registration", new UserPlusIcon(), new Color(228, 236, 246), INK);
        register.setMaximumSize(new Dimension(430, 53));
        register.setMinimumSize(new Dimension(430, 53));
        register.addActionListener(e -> new RegisterDialog(this, false).setVisible(true));
        formPanel.add(register);
        formPanel.add(Box.createVerticalStrut(11));

        JPanel divider = new JPanel(new BorderLayout(13, 0));
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(430, 24));
        divider.setMinimumSize(new Dimension(430, 24));
        divider.setMaximumSize(new Dimension(430, 24));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSeparator left = new JSeparator();
        JSeparator right = new JSeparator();
        JLabel or = new JLabel("Ward administrators sign in here", SwingConstants.CENTER);
        or.setForeground(MUTED);
        or.setFont(new Font("SansSerif", Font.PLAIN, 12));
        divider.add(left, BorderLayout.WEST);
        divider.add(or, BorderLayout.CENTER);
        divider.add(right, BorderLayout.EAST);
        formPanel.add(divider);
        formPanel.add(Box.createVerticalStrut(10));

        JButton setup = new ActionButton("Super Admin setup", new GearIcon(), Color.WHITE, TEAL_DARK);
        setup.setBorder(new RoundedLineBorder(TEAL, 2, 11));
        setup.setMaximumSize(new Dimension(430, 53));
        setup.setMinimumSize(new Dimension(430, 53));
        setup.addActionListener(e -> setupAdmin());
        formPanel.add(setup);

        centerColumn.add(formPanel);
        card.add(centerColumn);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        side.add(card, c);

        getRootPane().putClientProperty("acms.loginButton", login);
        return side;
    }

    private JLabel fieldLabel(Icon icon, String text) {
        JLabel label = new JLabel(text, icon, SwingConstants.LEFT);
        label.setIconTextGap(13);
        label.setFont(new Font("SansSerif", Font.BOLD, 17));
        label.setForeground(INK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        return label;
    }

    private void styleTextField(JTextField field, String placeholder) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setForeground(INK);
        field.setBackground(Color.WHITE);
        field.setBorder(new RoundedLineBorder(FIELD_BORDER, 1, 12));
        field.setPreferredSize(new Dimension(430, 48));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.putClientProperty("JTextField.placeholderText", placeholder);
    }

    private JButton textButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 15));
        b.setForeground(TEAL_DARK);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void doLogin() {
        String em = email.getText().trim();
        String pw = new String(password.getPassword());
        if (em.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email address and password.",
                    "Missing information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            SessionUser user = auth.login(em, pw);
            dispose();
            if ("SUPER_ADMIN".equalsIgnoreCase(user.role)) {
                new SuperAdminFrame(user).setVisible(true);
            } else if ("ADMIN".equalsIgnoreCase(user.role)) {
                new AdminFrame(user).setVisible(true);
            } else {
                new ResidentFrame(user).setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sign in failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupAdmin() {
        try {
            if (auth.countSuperAdmins() > 0) {
                JOptionPane.showMessageDialog(this,
                        "Super Admin account creation is disabled because an account already exists.\nPlease sign in with the Super Admin account.",
                        "Super Admin already configured", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            new InitialSuperAdminDialog(this).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Setup unavailable", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Custom-painted backgrounds and cards ----------

    private static class BrandPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, NAVY, getWidth(), getHeight(), NAVY_LIGHT));
            g2.fillRect(0, 0, getWidth(), getHeight());

            Path2D top = new Path2D.Double();
            top.moveTo(0, 0);
            top.lineTo(getWidth() * .40, 0);
            top.curveTo(getWidth() * .30, getHeight() * .13,
                    getWidth() * .19, getHeight() * .20, 0, getHeight() * .27);
            top.closePath();
            g2.setColor(new Color(0, 176, 177, 75));
            g2.fill(top);

            Path2D lower = new Path2D.Double();
            lower.moveTo(getWidth(), getHeight() * .72);
            lower.curveTo(getWidth() * .78, getHeight() * .77,
                    getWidth() * .78, getHeight() * .91,
                    getWidth() * .55, getHeight());
            lower.lineTo(getWidth(), getHeight());
            lower.closePath();
            g2.setColor(new Color(0, 169, 180, 90));
            g2.fill(lower);

            // Subtle lower wave highlight.
            g2.setColor(new Color(36, 117, 168, 45));
            g2.setStroke(new BasicStroke(2f));
            g2.drawArc((int)(getWidth()*.48), (int)(getHeight()*.79),
                    (int)(getWidth()*.60), (int)(getHeight()*.30), 180, 150);
            g2.dispose();
        }
    }

    private static class LoginCard extends JPanel {
        LoginCard() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 12;
            int h = getHeight() - 14;
            g2.setColor(new Color(26, 51, 82, 28));
            g2.fill(new RoundRectangle2D.Double(5, 8, w, h, 26, 26));
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 26, 26));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedLineBorder extends javax.swing.border.AbstractBorder {
        private final Color color; private final int thickness; private final int radius;
        RoundedLineBorder(Color color, int thickness, int radius) {
            this.color = color; this.thickness = thickness; this.radius = radius;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(9, 13, 9, 13); }
    }

    private static class IconTile extends JPanel {
        private final Icon icon;
        IconTile(Icon icon, Color background) { this.icon = icon; setOpaque(false); this.setBackground(background); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            icon.paintIcon(this, g2, (getWidth()-icon.getIconWidth())/2, (getHeight()-icon.getIconHeight())/2);
            g2.dispose();
        }
    }

    // ---------- Vector illustration ----------

    private static class CommunityIcon extends JComponent {
        private final Image illustration;
        CommunityIcon() {
            setPreferredSize(new Dimension(430, 220));
            setMaximumSize(new Dimension(430, 220));
            java.net.URL resource = LoginFrame.class.getResource("/icons/community_illustration.png");
            illustration = resource == null ? null : new ImageIcon(resource).getImage();
        }
        @Override protected void paintComponent(Graphics g) {
            if (illustration != null) {
                Graphics2D imageGraphics = (Graphics2D) g.create();
                imageGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                imageGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int drawWidth = Math.min(getWidth(), 430);
                int drawHeight = Math.min(getHeight(), 220);
                imageGraphics.drawImage(illustration, (getWidth()-drawWidth)/2, (getHeight()-drawHeight)/2,
                        drawWidth, drawHeight, this);
                imageGraphics.dispose();
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth()/2;
            Color aqua = new Color(65, 226, 215);
            g2.setColor(new Color(0, 208, 202, 105));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx-105, 7, 210, 190);

            // Map pin.
            g2.setColor(new Color(0, 210, 190));
            Path2D pin = new Path2D.Double();
            pin.moveTo(cx, 32); pin.curveTo(cx-35, 32, cx-42, 69, cx, 119);
            pin.curveTo(cx+42, 69, cx+35, 32, cx, 32); pin.closePath();
            g2.fill(pin);
            g2.setColor(NAVY); g2.fillOval(cx-12, 49, 24, 24);

            g2.setColor(aqua); g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Left house.
            Path2D r1 = new Path2D.Double(); r1.moveTo(cx-112, 132); r1.lineTo(cx-70, 99); r1.lineTo(cx-28, 132); g2.draw(r1);
            g2.drawRect(cx-102, 132, 66, 65);
            g2.fillRect(cx-87, 147, 14, 14); g2.fillRect(cx-61, 147, 14, 14); g2.fillRect(cx-87, 173, 14, 14);
            // Center house.
            Path2D r2 = new Path2D.Double(); r2.moveTo(cx-25, 151); r2.lineTo(cx+18, 112); r2.lineTo(cx+61, 151); g2.draw(r2);
            g2.drawRect(cx-16, 151, 68, 46); g2.fillRect(cx+5, 171, 17, 26);
            // Apartment building.
            g2.drawRect(cx+67, 113, 49, 84);
            g2.drawRect(cx+80, 126, 13, 13); g2.drawRect(cx+80, 150, 13, 13); g2.drawRect(cx+80, 174, 13, 13);
            // Trees and ground.
            tree(g2, cx-160, 145); tree(g2, cx+160, 145);
            g2.setStroke(new BasicStroke(3)); g2.drawArc(cx-184, 145, 368, 91, 190, 160);
            g2.dispose();
        }
        private void tree(Graphics2D g, int x, int y) {
            g.setColor(new Color(0, 210, 190)); g.fillOval(x-15, y-17, 30, 34);
            g.setStroke(new BasicStroke(3)); g.drawLine(x, y+14, x, y+42);
        }
    }

    // ---------- Small crisp vector icons ----------

    private abstract static class BaseIcon implements Icon {
        final int size; BaseIcon(int size) { this.size = size; }
        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }
        protected Graphics2D graphics(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(INK); g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            return g2;
        }
    }
    private static class MailIcon extends BaseIcon {
        MailIcon() { super(18); }
        public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D a=graphics(g); a.drawRoundRect(x+2,y+4,14,10,2,2); a.drawLine(x+2,y+5,x+9,y+10); a.drawLine(x+16,y+5,x+9,y+10); a.dispose(); }
    }
    private static class LockIcon extends BaseIcon {
        LockIcon() { super(18); }
        public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D a=graphics(g); a.drawRoundRect(x+3,y+8,12,9,2,2); a.drawArc(x+5,y+1,8,13,0,180); a.drawLine(x+9,y+11,x+9,y+14); a.dispose(); }
    }
    private static class EyeIcon extends BaseIcon {
        EyeIcon() { super(22); }
        public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D a=(Graphics2D)g.create(); a.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); a.setColor(MUTED); a.setStroke(new BasicStroke(1.8f)); a.drawOval(x+3,y+6,16,10); a.fillOval(x+9,y+9,4,4); a.dispose(); }
    }
    private static class PeopleIcon extends BaseIcon {
        PeopleIcon() { super(43); }
        public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D a=graphics(g); a.drawOval(x+16,y+2,11,11); a.drawOval(x+3,y+8,8,8); a.drawOval(x+32,y+8,8,8); a.drawArc(x+10,y+17,24,19,0,180); a.drawArc(x,y+20,14,15,0,180); a.drawArc(x+29,y+20,14,15,0,180); a.dispose(); }
    }
    private static class LoginArrowIcon extends BaseIcon {
        LoginArrowIcon() { super(22); }
        public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D a=graphics(g); a.setColor(Color.WHITE); a.drawRoundRect(x+2,y+3,11,16,2,2); a.drawLine(x+9,y+11,x+20,y+11); a.drawLine(x+15,y+6,x+20,y+11); a.drawLine(x+15,y+16,x+20,y+11); a.dispose(); }
    }
    private static class UserPlusIcon extends BaseIcon {
        UserPlusIcon() { super(22); }
        public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D a=graphics(g); a.drawOval(x+3,y+2,8,8); a.drawArc(x,y+12,15,10,0,180); a.drawLine(x+17,y+6,x+17,y+16); a.drawLine(x+13,y+11,x+21,y+11); a.dispose(); }
    }
    private static class GearIcon extends BaseIcon {
        GearIcon() { super(22); }
        public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D a=graphics(g); a.drawOval(x+6,y+6,10,10); a.drawOval(x+9,y+9,4,4); a.drawLine(x+11,y+1,x+11,y+5); a.drawLine(x+11,y+17,x+11,y+21); a.drawLine(x+1,y+11,x+5,y+11); a.drawLine(x+17,y+11,x+21,y+11); a.drawLine(x+4,y+4,x+7,y+7); a.drawLine(x+15,y+15,x+18,y+18); a.drawLine(x+4,y+18,x+7,y+15); a.drawLine(x+15,y+7,x+18,y+4); a.dispose(); }
    }

    private static class ActionButton extends JButton {
        private final Color fill;
        private final Color textColor;
        private final Icon icon;
        ActionButton(String text, Icon icon, Color fill, Color textColor) {
            super(text); this.fill=fill; this.textColor=textColor; this.icon=icon;
            setFont(new Font("SansSerif", Font.BOLD, 16)); setForeground(textColor); setIcon(icon);
            setIconTextGap(13); setHorizontalTextPosition(SwingConstants.RIGHT); setHorizontalAlignment(SwingConstants.CENTER);
            setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false); setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(430, 53)); setMaximumSize(new Dimension(Integer.MAX_VALUE, 53));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D a=(Graphics2D)g.create(); a.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            Color c=getModel().isRollover()?fill.brighter():fill;
            a.setColor(c); a.fillRoundRect(0,0,getWidth(),getHeight(),11,11); a.dispose(); super.paintComponent(g);
        }
    }
}

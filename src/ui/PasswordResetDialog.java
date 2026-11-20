package ui;

import service.AuthService;
import javax.swing.*;
import java.awt.*;

/** Email OTP password recovery. Requires SMTP configuration in db.properties. */
public class PasswordResetDialog extends JDialog {
    private final AuthService auth = new AuthService();
    private final JTextField email = UITheme.field();
    private final JTextField code = UITheme.field();
    private final JPasswordField password = new JPasswordField();
    public PasswordResetDialog(JFrame owner) {
        super(owner, "Forgot password", true);
        setSize(430, 340); setLocationRelativeTo(owner);
        JPanel panel = new JPanel(); panel.setBackground(Color.WHITE); panel.setBorder(BorderFactory.createEmptyBorder(22,26,22,26));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(UITheme.title("Reset your password"));
        panel.add(Box.createVerticalStrut(8)); panel.add(UITheme.subtitle("We will email a one-time code valid for 10 minutes."));
        panel.add(Box.createVerticalStrut(16)); panel.add(UITheme.fieldLabel("Registered email")); panel.add(email);
        JButton send = UITheme.button("Send email code", UITheme.TEAL); send.setAlignmentX(Component.LEFT_ALIGNMENT);
        send.addActionListener(e -> { try { auth.sendPasswordResetCode(email.getText().trim()); JOptionPane.showMessageDialog(this,"A reset code has been sent if SMTP is configured."); } catch(Exception ex) { error(ex); } });
        panel.add(Box.createVerticalStrut(8)); panel.add(send); panel.add(Box.createVerticalStrut(12));
        panel.add(UITheme.fieldLabel("Email verification code")); panel.add(code); panel.add(Box.createVerticalStrut(10));
        panel.add(UITheme.fieldLabel("New password")); password.setFont(UITheme.REGULAR); password.setBorder(email.getBorder()); password.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); panel.add(password);
        JButton reset = UITheme.button("Reset password", UITheme.NAVY); reset.setAlignmentX(Component.LEFT_ALIGNMENT);
        reset.addActionListener(e -> { String pw=new String(password.getPassword()); if(pw.length()<8){error(new Exception("Use at least 8 characters for the new password."));return;} try {auth.resetPassword(email.getText().trim(),code.getText().trim(),pw);JOptionPane.showMessageDialog(this,"Password changed. You can now sign in.");dispose();}catch(Exception ex){error(ex);} });
        panel.add(Box.createVerticalStrut(12)); panel.add(reset); setContentPane(panel);
    }
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Password reset",JOptionPane.ERROR_MESSAGE);}
}

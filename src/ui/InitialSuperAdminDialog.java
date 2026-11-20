package ui;

import service.AuthService;
import javax.swing.*;
import java.awt.*;

/** One-time bootstrap screen. Once a Super Admin exists, setup is locked. */
public class InitialSuperAdminDialog extends JDialog {
    public InitialSuperAdminDialog(JFrame owner) {
        super(owner,"Initial Super Admin Setup",true);
        setSize(470,440);setLocationRelativeTo(owner);
        JTextField name=new JTextField(),email=new JTextField(),phone=new JTextField();JPasswordField pass=new JPasswordField(),confirm=new JPasswordField();
        JPanel form=new JPanel(new GridLayout(0,1,6,6));form.setBorder(BorderFactory.createEmptyBorder(20,24,12,24));
        form.add(new JLabel("Full name"));form.add(name);form.add(new JLabel("Email"));form.add(email);form.add(new JLabel("Phone"));form.add(phone);form.add(new JLabel("Password (minimum 10 characters)"));form.add(pass);form.add(new JLabel("Confirm password"));form.add(confirm);
        JButton create=UITheme.button("Create Super Admin",UITheme.TEAL);create.addActionListener(e->{try{String a=new String(pass.getPassword()),b=new String(confirm.getPassword());if(!a.equals(b))throw new Exception("Passwords do not match.");new AuthService().createInitialSuperAdmin(name.getText(),email.getText(),phone.getText(),a);JOptionPane.showMessageDialog(this,"Super Admin created. You can now sign in.");dispose();}catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Setup failed",JOptionPane.ERROR_MESSAGE);}});
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT));bottom.add(create);add(form,BorderLayout.CENTER);add(bottom,BorderLayout.SOUTH);
    }
}

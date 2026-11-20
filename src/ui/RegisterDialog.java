package ui;

import service.AuthService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class RegisterDialog extends JDialog {
    private final JTextField name=UITheme.field(), email=UITheme.field(), phone=UITheme.field(), address=UITheme.field();
    private final JPasswordField password=new JPasswordField(), confirm=new JPasswordField();
    private final JComboBox<String> wardBox = new JComboBox<>();
    private final Map<String,Integer> wardIds;

    public RegisterDialog(JFrame owner, boolean admin) {
        super(owner, admin ? "Ward Administrator Setup" : "Resident Registration", true);
        setSize(540, 650); setMinimumSize(new Dimension(500, 610));
        JPanel root=new JPanel(new BorderLayout(10,10));root.setBackground(Color.WHITE);root.setBorder(new EmptyBorder(24,28,22,28));
        JLabel heading=UITheme.title("Resident registration");
        JLabel sub=UITheme.subtitle("Create your account and select your residential ward");
        JPanel top=new JPanel();top.setOpaque(false);top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));top.add(heading);top.add(Box.createVerticalStrut(5));top.add(sub);root.add(top,BorderLayout.NORTH);
        JPanel form=new JPanel(new GridBagLayout());form.setOpaque(false);GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(5,3,5,3);g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;g.gridwidth=2;
        field(form,g,0,"Full name",name);field(form,g,1,"Email address",email);field(form,g,2,"Phone number",phone);field(form,g,3,"Address",address);
        AuthService auth = new AuthService();
        Map<String,Integer> loaded;
        try { loaded = auth.getActiveWards(); }
        catch (Exception ex) { loaded = java.util.Collections.emptyMap(); JOptionPane.showMessageDialog(owner,"Could not load wards: "+ex.getMessage(),"Ward list",JOptionPane.ERROR_MESSAGE); }
        wardIds = loaded;
        wardBox.setFont(UITheme.REGULAR);
        for(String ward:wardIds.keySet()) wardBox.addItem(ward);
        field(form,g,4,"Select your ward",wardBox);
        password.setFont(UITheme.REGULAR);password.setBorder(name.getBorder());password.setPreferredSize(new Dimension(180,36));
        confirm.setFont(UITheme.REGULAR);confirm.setBorder(name.getBorder());confirm.setPreferredSize(new Dimension(180,36));
        field(form,g,5,"Password (minimum 6 characters)",password);field(form,g,6,"Confirm password",confirm);
        root.add(form,BorderLayout.CENTER);
        JButton save=UITheme.button("Register account",UITheme.TEAL);
        JButton cancel=UITheme.button("Cancel",new Color(235,241,246));
        JPanel buttons=new JPanel(new GridLayout(1,2,10,0));buttons.setOpaque(false);buttons.add(cancel);buttons.add(save);root.add(buttons,BorderLayout.SOUTH);
        cancel.addActionListener(e->dispose());save.addActionListener(e->save());
        setContentPane(root);setLocationRelativeTo(owner);
    }
    private void field(JPanel p,GridBagConstraints g,int row,String label,JComponent c){g.gridx=0;g.gridy=row*2;p.add(UITheme.fieldLabel(label),g);g.gridy=row*2+1;p.add(c,g);}
    private void save(){
        String n=name.getText().trim(),em=email.getText().trim(),ph=phone.getText().trim(),ad=address.getText().trim();String pw=new String(password.getPassword());
        if(n.isEmpty()||em.isEmpty()||ph.isEmpty()||ad.isEmpty()||pw.length()<6){JOptionPane.showMessageDialog(this,"Complete all fields. Password must contain at least 6 characters.");return;}
        if(wardBox.getSelectedItem()==null){JOptionPane.showMessageDialog(this,"No active wards are available. Contact the application administrator.");return;}
        if(!em.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")){JOptionPane.showMessageDialog(this,"Enter a valid email address.");return;}
        if(!pw.equals(new String(confirm.getPassword()))){JOptionPane.showMessageDialog(this,"Passwords do not match.");return;}
        try{new AuthService().register(n,em,ph,ad,pw,wardIds.get(String.valueOf(wardBox.getSelectedItem())));
            JOptionPane.showMessageDialog(this,"Registration successful. You can now sign in.");dispose();}
        catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Registration failed",JOptionPane.ERROR_MESSAGE);}
    }
}

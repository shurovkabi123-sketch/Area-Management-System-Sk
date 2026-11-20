package service;

import config.AppConfig;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/** SMTP email delivery. Configure SMTP settings in db.properties; messages are skipped when SMTP is not configured. */
public final class EmailService {
    private EmailService() {}
    public static boolean configured() { return !AppConfig.smtpHost().isBlank() && !AppConfig.smtpUser().isBlank() && !AppConfig.smtpPassword().isBlank() && !AppConfig.smtpFrom().isBlank(); }
    public static void send(String to, String subject, String body) throws Exception {
        if (!configured()) throw new IllegalStateException("Email is not configured. Add SMTP settings to db.properties first.");
        Properties p = new Properties();
        p.put("mail.smtp.auth", "true"); p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.host", AppConfig.smtpHost()); p.put("mail.smtp.port", AppConfig.smtpPort());
        Session s = Session.getInstance(p, new Authenticator() { protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(AppConfig.smtpUser(), AppConfig.smtpPassword()); }});
        Message m = new MimeMessage(s); m.setFrom(new InternetAddress(AppConfig.smtpFrom()));
        m.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); m.setSubject(subject);
        m.setText(body); Transport.send(m);
    }
}

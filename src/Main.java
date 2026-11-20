import com.formdev.flatlaf.FlatLightLaf;
import config.DBConnection;
import exception.DatabaseException;
import ui.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            try (var ignored = DBConnection.getConnection()) {
                new LoginFrame().setVisible(true);
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(),
                        "Database Connection Failed", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(),
                        "Application Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

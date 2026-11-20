package ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

/** Shared visual system: practical navy, teal and white desktop UI. */
public final class UITheme {
    public static final Color NAVY = new Color(24, 48, 76);
    public static final Color NAVY_DARK = new Color(17, 36, 59);
    public static final Color TEAL = new Color(0, 132, 139);
    public static final Color TEAL_DARK = new Color(0, 105, 112);
    public static final Color BG = new Color(242, 246, 249);
    public static final Color PANEL = Color.WHITE;
    public static final Color TEXT = new Color(42, 54, 66);
    public static final Color MUTED = new Color(112, 126, 139);
    public static final Color BORDER = new Color(220, 228, 234);
    public static final Color GREEN = new Color(40, 145, 100);
    public static final Color RED = new Color(194, 72, 72);
    public static final Color AMBER = new Color(196, 135, 38);
    public static final Font REGULAR = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 23);

    private UITheme() { }

    public static JPanel page() {
        JPanel p = new JPanel(new BorderLayout(14, 14));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(22, 24, 22, 24));
        return p;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(PANEL);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(16, 16, 16, 16)));
        return p;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(TITLE);
        l.setForeground(NAVY);
        return l;
    }

    public static JLabel subtitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(REGULAR);
        l.setForeground(MUTED);
        return l;
    }

    public static JButton button(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(BOLD);
        b.setForeground(color.equals(new Color(235, 241, 246)) ? TEXT : Color.WHITE);
        b.setBackground(color);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 15, 9, 15));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JTextField field() {
        JTextField f = new JTextField();
        f.setFont(REGULAR);
        f.setForeground(TEXT);
        f.setBackground(Color.WHITE);
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(8, 9, 8, 9)));
        f.setPreferredSize(new Dimension(180, 36));
        return f;
    }

    public static void styleTable(JTable table) {
        table.setFont(REGULAR);
        table.setForeground(TEXT);
        table.setRowHeight(31);
        table.setSelectionBackground(new Color(214, 235, 236));
        table.setSelectionForeground(NAVY_DARK);
        table.setGridColor(new Color(232, 237, 241));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        JTableHeader h = table.getTableHeader();
        h.setFont(BOLD);
        h.setForeground(NAVY);
        h.setBackground(new Color(232, 240, 245));
        h.setPreferredSize(new Dimension(0, 38));
        h.setReorderingAllowed(false);
    }

    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BOLD);
        l.setForeground(TEXT);
        return l;
    }
}

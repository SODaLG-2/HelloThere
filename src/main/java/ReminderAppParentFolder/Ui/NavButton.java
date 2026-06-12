package ReminderAppParentFolder.Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class NavButton extends JButton {

    private boolean hovered = false;

    public NavButton(String icon, String label, Consumer<String> onClick, SessionContentPanel contentPanel) {
        super(icon + "  " + label);
        setFont(Theme.FONT_NAV);
        setForeground(Theme.TEXT_SECONDARY);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) { if ( !contentPanel.getDraftActive()) onClick.accept(label); } //if clicked and session is active, don't activate
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg = hovered
                ? new Color(99, 102, 241, 40)
                : new Color(0, 0, 0, 0);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.dispose();
        super.paintComponent(g);
    }
}
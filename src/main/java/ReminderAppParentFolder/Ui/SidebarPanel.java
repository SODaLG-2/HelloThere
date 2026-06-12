package ReminderAppParentFolder.Ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.function.Consumer;

public class SidebarPanel extends JPanel {

    private static final String[] LABELS = { "New Session", "Records", "Settings" };
    private static final String[] ICONS  = { "⊞", "≡", "⚙" };

    public SidebarPanel(Consumer<String> onNavClick, SessionContentPanel contentPanel) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.BG_SIDEBAR);
        setBorder(new EmptyBorder(20, 12, 20, 12));

        for (int i = 0; i < LABELS.length; i++) {
            add(new NavButton(ICONS[i], LABELS[i], onNavClick, contentPanel));
            add(Box.createRigidArea(new Dimension(0, 8)));
        }
        add(Box.createVerticalGlue());
    }
}
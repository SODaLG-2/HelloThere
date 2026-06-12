package ReminderAppParentFolder.Ui;
import ReminderAppParentFolder.Storage.StorageManager;
import ReminderAppParentFolder.Util.FolderPathDocumentFilter;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.*;

public class SettingsPanel extends JPanel {

    private static final StorageManager storageManager = StorageManager.getInstance();
    private static final Color DANGER         = new Color(0xaa2222);

    private JTextField field1;
    private JTextField field2;
    private JTextField field3;

    private String saved1 = "";
    private String saved2 = "";
    private String saved3 = "";

    public SettingsPanel() {
        load();
        setLayout(new GridBagLayout());
        setBackground(Theme.BG_CONTENT);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        // Icon
        JLabel icon = new JLabel("⚙");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 48));
        icon.setForeground(new Color(99, 102, 241, 80));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("Welcome to the Settings");
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 100, 120));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        JTextField[] refs = new JTextField[3];

        JPanel bar1 = buildBar("Session folder path", saved1, refs, 0);
        JPanel bar2 = buildBar("Log folder path", saved2, refs, 1);
        JPanel bar3 = buildBar("Sound folder path", saved3, refs, 2);

        field1 = refs[0];
        field2 = refs[1];
        field3 = refs[2];

        ((AbstractDocument) field1.getDocument()).setDocumentFilter(new FolderPathDocumentFilter());
        ((AbstractDocument) field2.getDocument()).setDocumentFilter(new FolderPathDocumentFilter());
        ((AbstractDocument) field3.getDocument()).setDocumentFilter(new FolderPathDocumentFilter());

        bar1.setAlignmentX(Component.CENTER_ALIGNMENT);
        bar2.setAlignmentX(Component.CENTER_ALIGNMENT);
        bar3.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Save button
        JButton saveBtn = styledButton("  Save  ", Theme.ACCENT, Color.WHITE);
        saveBtn.addActionListener(e -> save());
        JPanel saveRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        saveRow.setOpaque(false);
        saveRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveRow.setMaximumSize(new Dimension(500, 40));
        saveRow.add(saveBtn);

        inner.add(icon);
        inner.add(Box.createRigidArea(new Dimension(0, 12)));
        inner.add(subtitle);
        inner.add(Box.createRigidArea(new Dimension(0, 24)));
        inner.add(bar1);
        inner.add(Box.createRigidArea(new Dimension(0, 10)));
        inner.add(bar2);
        inner.add(Box.createRigidArea(new Dimension(0, 12)));
        inner.add(bar3);
        inner.add(Box.createRigidArea(new Dimension(0, 12)));
        inner.add(saveRow);

        add(inner);
    }

    private JPanel buildBar(String label, String initialValue, JTextField[] refs, int idx) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(Theme.TEXT_SECONDARY);
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JTextField tf = new JTextField(initialValue);
        tf.setFont(new Font("Monospaced", Font.PLAIN, 14));
        tf.setForeground(Theme.TEXT_PRIMARY);
        tf.setBackground(Theme.BG_FIELD);
        tf.setCaretColor(Theme.TEXT_PRIMARY);
        tf.setEditable(false);
        tf.setToolTipText("Click to edit");
        tf.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(0x44446a)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        tf.setPreferredSize(new Dimension(400, 36));
        tf.setMinimumSize(new Dimension(200, 36));
        tf.setMaximumSize(new Dimension(600, 36));

        tf.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!tf.isEditable()) {
                    tf.setEditable(true);
                    tf.setBackground(Theme.BG_CONTENT);
                    tf.selectAll();
                    tf.requestFocusInWindow();
                }
            }
        });

        refs[idx] = tf;

        JButton discardBtn = styledButton("✕", DANGER, Color.WHITE);
        discardBtn.setToolTipText("Discard changes");
        discardBtn.addActionListener(e -> {
            String currentSaved = (idx == 0) ? saved1 : (idx == 1) ? saved2 : saved3;
            tf.setText(currentSaved);
            tf.setEditable(false);
            tf.setBackground(Theme.BG_FIELD);
        });

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(tf);
        row.add(discardBtn);

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setPreferredSize(new Dimension(500, 70));
        wrapper.setMaximumSize(new Dimension(500, 70));
        wrapper.add(lbl);
        wrapper.add(row);

        return wrapper;
    }

    private void save() {
        saved1 = field1.getText().trim();
        saved2 = field2.getText().trim();
        saved3 = field3.getText().trim();

        for (JTextField tfElement : new JTextField[]{field1, field2, field3}) {
            tfElement.setEditable(false);
            tfElement.setBackground(Theme.BG_FIELD);
        }

        storageManager.settings().set("Sessions", saved1, false);
        storageManager.settings().set("Logs", saved2, false);
        storageManager.settings().set("Sounds", saved3, false);
    }

    public void load() {
        saved1 = storageManager.settings().get("Sessions");
        saved2 = storageManager.settings().get("Logs");
        saved3 = storageManager.settings().get("Sounds");
    }

    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); // stop L&F from painting its own bg over ours
        btn.setOpaque(true);             // but still honour our setBackground() colour
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, bg.darker()),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
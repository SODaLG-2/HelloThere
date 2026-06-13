package ReminderAppParentFolder.Ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class TaskItem extends JPanel {

    // Swapped JLabel for JTextArea to allow text wrapping
    private final JTextArea taskView;
    private final JTextField editField;

    private final JButton editBtn;
    private final JButton deleteBtn;

    private boolean editing = false;

    public TaskItem(String text, Consumer<TaskItem> onDelete) {

        // X_AXIS works, but we must let the height dynamically grow when text wraps
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(new EmptyBorder(4, 0, 4, 0));

        // 1. Configure the Text Area for Wrapping
        taskView = new JTextArea(text);
        taskView.setFont(Theme.FONT_BODY);
        taskView.setForeground(Theme.TEXT_PRIMARY);
        taskView.setOpaque(false);
        taskView.setEditable(false);
        taskView.setFocusable(false);
        taskView.setLineWrap(true);       // Enable wrapping
        taskView.setWrapStyleWord(true);  // Wrap at word boundaries

        // This ensures the JTextArea doesn't blow up horizontally
        taskView.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

        // 2. Configure the Edit Field
        editField = new JTextField(text);
        editField.setFont(Theme.FONT_BODY);
        editField.setForeground(Theme.TEXT_PRIMARY);
        editField.setBackground(new Color(40, 40, 55));
        editField.setCaretColor(Theme.TEXT_PRIMARY);
        editField.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(1, 6, 1, 6)
        ));
        editField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        editField.setVisible(false);
        editField.addActionListener(e -> confirmEdit());

        // 3. Configure Buttons
        editBtn = buildSmallButton("Edit", Theme.TEXT_SECONDARY);
        editBtn.addActionListener(e -> {
            if (editing) {
                confirmEdit();
            } else {
                startEdit();
            }
        });

        deleteBtn = buildSmallButton("✕", new Color(200, 60, 60));
        deleteBtn.setForeground(new Color(200, 60, 60));

        deleteBtn.setContentAreaFilled(false);

        deleteBtn.addActionListener(e -> onDelete.accept(this));

        // 4. Component Assembly
        // Put the JTextArea inside a panel aligned to the top so it doesn't stretch vertically
        JPanel textContainer = new JPanel(new BorderLayout());
        textContainer.setOpaque(false);
        textContainer.add(taskView, BorderLayout.CENTER);

        // Control panel for side-by-side buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        // Align buttons to the top right of the task item
        JPanel btnAligner = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnAligner.setOpaque(false);
        btnAligner.add(editBtn);
        btnAligner.add(Box.createHorizontalStrut(4));
        btnAligner.add(deleteBtn);
        buttonPanel.add(btnAligner);

        add(textContainer);
        add(editField);
        add(Box.createHorizontalGlue());
        add(buttonPanel);
    }

    // ─────────────────────────────────────────
    // Edit Logic
    // ─────────────────────────────────────────

    private void startEdit() {
        editing = true;
        editField.setText(taskView.getText());
        taskView.setVisible(false);
        editField.setVisible(true);
        editField.requestFocusInWindow();
        editBtn.setText("Done");
        revalidate();
        repaint();
    }

    private void confirmEdit() {
        String val = editField.getText().trim();
        if (!val.isEmpty()) {
            taskView.setText(val);
        }
        editing = false;
        editField.setVisible(false);
        taskView.setVisible(true);
        editBtn.setText("Edit");
        revalidate();
        repaint();
    }

    // ─────────────────────────────────────────
    // Button Builder
    // ─────────────────────────────────────────

    private JButton buildSmallButton(String label, Color returnColor) {
        JButton btn = new JButton(label);
        btn.setFont(Theme.FONT_MONO);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(Theme.TEXT_PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(returnColor);
            }
        });

        return btn;
    }

    @Override
    public Dimension getMaximumSize() {
        // Get the preferred layout size calculated by the components inside
        Dimension pref = getPreferredSize();
        // Allow horizontal stretching, but lock the vertical height to its exact needs
        return new Dimension(Integer.MAX_VALUE, pref.height);
    }

    // ─────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────

    public String getText() {
        return taskView.getText();
    }
}
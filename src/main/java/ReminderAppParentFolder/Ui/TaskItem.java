package ReminderAppParentFolder.Ui;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;

import java.util.function.Consumer;

public class TaskItem extends JPanel {

    private final JLabel taskLabel;

    private final JTextField editField;

    private final JButton editBtn;
    private final JButton deleteBtn;

    private boolean editing = false;

    private final Consumer<TaskItem> onDelete;

    public TaskItem(String text,
                    Consumer<TaskItem> onDelete) {

        this.onDelete = onDelete;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        setOpaque(false);

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        setAlignmentX(Component.LEFT_ALIGNMENT);

        setBorder(new EmptyBorder(2, 0, 2, 0));

        taskLabel = new JLabel(text);

        taskLabel.setFont(Theme.FONT_BODY);

        taskLabel.setForeground(Theme.TEXT_PRIMARY);

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

        editBtn = buildSmallButton("Edit");

        editBtn.addActionListener(e -> {

            if (editing) {
                confirmEdit();
            }
            else {
                startEdit();
            }
        });

        deleteBtn = buildSmallButton("✕");

        deleteBtn.setForeground(new Color(200, 60, 60));

        deleteBtn.addActionListener(e ->
                onDelete.accept(this)
        );

        add(taskLabel);

        add(editField);

        add(Box.createHorizontalGlue());

        add(editBtn);

        add(Box.createHorizontalStrut(4));

        add(deleteBtn);
    }

    // ─────────────────────────────────────────
    // Edit Logic
    // ─────────────────────────────────────────

    private void startEdit() {

        editing = true;

        editField.setText(taskLabel.getText());

        taskLabel.setVisible(false);

        editField.setVisible(true);

        editField.requestFocusInWindow();

        editBtn.setText("Done");
    }

    private void confirmEdit() {

        String val = editField.getText().trim();

        if (!val.isEmpty()) {
            taskLabel.setText(val);
        }

        editing = false;

        editField.setVisible(false);

        taskLabel.setVisible(true);

        editBtn.setText("Edit");
    }

    // ─────────────────────────────────────────
    // Button Builder
    // ─────────────────────────────────────────

    private JButton buildSmallButton(String label) {

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
                btn.setForeground(Theme.TEXT_SECONDARY);
            }
        });

        return btn;
    }

    // ─────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────

    public String getText() {
        return taskLabel.getText();
    }
}
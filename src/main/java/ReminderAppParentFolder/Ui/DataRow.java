package ReminderAppParentFolder.Ui;

import ReminderAppParentFolder.Util.NumericDocumentFilter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * A single notification row: [checkbox  label] ............. [field] min
 *
 * The row checkbox is disabled until the master checkbox is ticked.
 * The minute field is read-only until the row checkbox is ticked.
 */
public class DataRow extends JPanel {

    private final JTextField minField;
    private JCheckBox        rowCheck;
    private JLabel           jlabel;

    /**
     * Checkboxless variant — label + always-editable minute field.
     * Use for fields that are unconditionally active (e.g. Duration).
     */
    public DataRow(String label, int defaultMinutes) {
        this(label, defaultMinutes, null, false);
    }

    public DataRow(String label, int defaultMinutes, JCheckBox master, boolean useCheckBox) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        if (useCheckBox) {
            rowCheck = new JCheckBox(label);
            styleCheckBox(rowCheck);
        } else {
            jlabel = new JLabel(label);
;        }

        boolean hasCheckbox = (master != null);

        minField = new JTextField(String.valueOf(defaultMinutes), 4);
        minField.setFont(Theme.FONT_MONO);
        minField.setForeground(Theme.TEXT_PRIMARY);
        minField.setBackground(new Color(40, 40, 55));
        minField.setCaretColor(Theme.TEXT_PRIMARY);
        minField.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));
        minField.setHorizontalAlignment(JTextField.RIGHT);
        minField.setMaximumSize(new Dimension(56, 26));
        minField.setMinimumSize(new Dimension(56, 26));
        minField.setPreferredSize(new Dimension(56, 26));
        ((javax.swing.text.AbstractDocument) minField.getDocument())
                .setDocumentFilter(new NumericDocumentFilter());
        ;

        if (hasCheckbox) {
            // Starts locked until master enables it
            rowCheck.setEnabled(false);
            minField.setEnabled(false);
            minField.setEditable(false);

            // Row checkbox toggles its own field
            rowCheck.addActionListener(e -> setFieldActive(rowCheck.isSelected()));

            // Master gates the row checkbox; on master-off, reset everything
            master.addActionListener(e -> {
                rowCheck.setEnabled(master.isSelected());
                if (!master.isSelected()) {
                    rowCheck.setSelected(false);
                    setFieldActive(false);
                }
            });
        } else if (useCheckBox) {
            // No master — always active
            rowCheck.setSelected(false);
            rowCheck.setFocusable(false);
            rowCheck.setOpaque(false);
            rowCheck.addActionListener(e -> setFieldActive(rowCheck.isSelected()));
            minField.setEnabled(false);
        }



        JLabel minLabel = new JLabel(" min");
        minLabel.setFont(Theme.FONT_MONO);
        minLabel.setForeground(Theme.TEXT_SECONDARY);

        if (useCheckBox) {
            add(rowCheck);
        } else {
            jlabel.setFont(Theme.FONT_BODY);
            jlabel.setForeground(Theme.TEXT_PRIMARY);
            jlabel.setOpaque(false);
            jlabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(jlabel);
        }
        add(Box.createHorizontalGlue());
        add(minField);
        add(minLabel);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public boolean isRowChecked()              { return rowCheck.isSelected(); }
    public void    setRowChecked(boolean b)    { rowCheck.setSelected(b); setFieldActive(b); } //handles the minfield
    public int     getMinutes()                { return parseMinutes(); }
    public void    setMinutes(int m)           { minField.setText(String.valueOf(m)); }
    public void    setRowEnabled(boolean b)    { rowCheck.setEnabled(b); } //this is either not getting turned on correctly, or there is something wrong with the code

    /** Allows NotificationsCard to react when this row's checkbox changes. */
    public void addRowCheckListener(ActionListener l) { rowCheck.addActionListener(l); }
    public void setOnValueChanged(Runnable r) {

        minField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                r.run();
            }
        });
    }
    // ── Private ────────────────────────────────────────────────────────────────

    private void setFieldActive(boolean active) {
        minField.setEnabled(active);
        minField.setEditable(active);
    }

    private int parseMinutes() {
        try { return Integer.parseInt(minField.getText().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private void styleCheckBox(JCheckBox cb) {
        cb.setFont(Theme.FONT_BODY);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}

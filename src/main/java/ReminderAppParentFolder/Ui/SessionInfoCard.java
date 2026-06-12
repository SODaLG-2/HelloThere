package ReminderAppParentFolder.Ui;

import ReminderAppParentFolder.Session.SessionDraft;
import ReminderAppParentFolder.Util.FileNameDocumentFilter;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import java.awt.*;

/**
 * Card content for the Session Info tab.
 *
 * Layout:
 *   Session Name   [_____________]
 *   ─────────────────────────────
 *   Duration                [  ] min
 *   [✓] Idle Time Threshold [  ] min
 */
public class SessionInfoCard extends JPanel {

    private final JTextField      nameField;
    private final DataRow durationRow;
    private final DataRow idleRow;
    private SessionDraft draft;

    private final SessionContentPanel sessionContentPanel;

    public SessionInfoCard(SessionContentPanel sessionContentPanel) {
        this.sessionContentPanel = sessionContentPanel;
        //temp draft
        setLayout(new BorderLayout());
        setBackground(Theme.BG_CONTENT);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Session name row
        JPanel nameRow = new JPanel();
        nameRow.setLayout(new BoxLayout(nameRow, BoxLayout.X_AXIS));
        nameRow.setOpaque(false);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel("Session Name");
        nameLabel.setFont(Theme.FONT_BODY);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);

        nameField = new JTextField();
        nameField.setFont(Theme.FONT_BODY);
        nameField.setForeground(Theme.TEXT_PRIMARY);
        nameField.setBackground(new Color(40, 40, 55));
        nameField.setCaretColor(Theme.TEXT_PRIMARY);
        nameField.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        ((AbstractDocument) nameField.getDocument())
                .setDocumentFilter(
                        new FileNameDocumentFilter()
                );

        nameField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateDraft();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateDraft();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateDraft();
                    }
                }
        );

        nameRow.add(nameLabel);
        nameRow.add(Box.createHorizontalStrut(12));
        nameRow.add(nameField);

        // Duration — no checkbox, always editable
        durationRow = new DataRow("Duration", 0);
        durationRow.setOnValueChanged(() -> {
            draft.setExpectedDuration(durationRow.getMinutes()*60);
            sessionContentPanel.setDraftChanged(true);
        });

        // Idle threshold — has its own checkbox (acts as its own master)
        idleRow = new DataRow("Idle Time Threshold", 5, null, true);
        idleRow.addRowCheckListener(e -> { draft.useIdleThreshold(idleRow.isRowChecked()); sessionContentPanel.setDraftChanged(true); });
        idleRow.setOnValueChanged(() -> {
            draft.setIdleThreshold(idleRow.getMinutes()*60);
            sessionContentPanel.setDraftChanged(true);
        });
        idleRow.setRowEnabled(true);

        // Wrap idle row with its own master checkbox inline
        JPanel idleWrapper = new JPanel();
        idleWrapper.setLayout(new BoxLayout(idleWrapper, BoxLayout.X_AXIS));
        idleWrapper.setOpaque(false);
        idleWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        idleWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        idleWrapper.add(idleRow);

        card.add(nameRow);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(buildDivider());
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(durationRow);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(idleWrapper);

        add(card, BorderLayout.NORTH);


    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public String getSessionName()        { return nameField.getText().trim(); }
    public void   setSessionName(String n){ nameField.setText(n); }
    public int    getDurationMinutes()    { return durationRow.getMinutes(); }
    public boolean isIdleEnabled()        { return idleRow.isRowChecked(); }
    public int    getIdleThreshold()      { return idleRow.getMinutes(); }
    public void setDraft(SessionDraft draft) { this.draft = draft; }

    public void loadFromDraft() {
        setSessionName(draft.getSessionName());
        durationRow.setMinutes(draft.getExpectedDuration()/60);
        idleRow.setMinutes(draft.getIdleThreshold()/60);
        idleRow.setRowChecked(draft.isIdleUsed());
        //idleRow.setRowEnabled(draft.isIdleUsed());
    }
    public void applyToDraft(SessionDraft draft) {
        draft.setSessionInfo(getSessionName(),getDurationMinutes()*60, getIdleThreshold()*60, isIdleEnabled());
    }

    // ── Private ────────────────────────────────────────────────────────────────

    private JSeparator buildDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_COLOR);
        sep.setForeground(Theme.BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private void updateDraft() {
        sessionContentPanel.setDraftChanged(true);
        if (draft == null) return;

        draft.setSessionInfo(
                nameField.getText().trim()
        );
    }
}

package ReminderAppParentFolder.Ui;

import ReminderAppParentFolder.Session.SessionDraft;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Card content for the Notifications tab.
 *
 * Layout:
 *   [✓] Enable Notifications
 *   ────────────────────────
 *   [✓] Popup Notification       [  5] min
 *   [✓] Overlay Notification     [ 10] min
 *   [✓] Sound Notification       [  0] min
 *
 * At least one row must remain selected while master is enabled.
 * The selected count is an instance field — safe across multiple sessions.
 */
public class NotificationsCard extends JPanel {

    private final JCheckBox       masterCheck;
    private final DataRow popupRow;
    private final DataRow overlayRow;
    private final DataRow soundRow;
    private final SessionContentPanel sessionContentPanel;

    // Instance field — not static, so multiple cards don't share state
    private int selectedCount = 0;
    private SessionDraft draft;


    public NotificationsCard(SessionContentPanel sessionContentPanel) {
        this.sessionContentPanel = sessionContentPanel;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_CONTENT);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        masterCheck = new JCheckBox("Enable Notifications");
        styleCheckBox(masterCheck);

        popupRow   = new DataRow("Popup Notification",   5,  masterCheck, true);
        overlayRow = new DataRow("Overlay Notification", 10, masterCheck, true);
        soundRow   = new DataRow("Sound Notification",   0,  masterCheck, true);


        popupRow.setOnValueChanged(() -> {
            draft.setPopupInterval(popupRow.getMinutes()*60);
            sessionContentPanel.setDraftChanged(true);
        });
        overlayRow.setOnValueChanged(() -> {
            draft.setOverlayInterval(overlayRow.getMinutes()*60);
            sessionContentPanel.setDraftChanged(true);
        });
        soundRow.setOnValueChanged(() -> {
            draft.setSoundInterval(soundRow.getMinutes()*60);
            sessionContentPanel.setDraftChanged(true);
        });

        masterCheck.addActionListener(e -> { draft.setNotificationsEnabled(masterCheck.isSelected()); sessionContentPanel.setDraftChanged(true);});
        popupRow.addRowCheckListener(e -> { onRowToggled(popupRow); draft.setPopupEnabled(popupRow.isEnabled()); sessionContentPanel.setDraftChanged(true);});
        overlayRow.addRowCheckListener(e -> { onRowToggled(overlayRow); draft.setOverlayEnabled(overlayRow.isEnabled()); sessionContentPanel.setDraftChanged(true);});
        soundRow.addRowCheckListener(e -> { onRowToggled(soundRow); draft.setSoundEnabled(soundRow.isEnabled()); sessionContentPanel.setDraftChanged(true);});


        // When master turns on, ensure at least popup is selected.
        // When master turns off, reset the counter.
        masterCheck.addActionListener(e -> {
            sessionContentPanel.setDraftChanged(true);
            draft.setNotificationsEnabled(masterCheck.isSelected());
            if (masterCheck.isSelected()) {
                if (selectedCount == 0) {
                    popupRow.setRowChecked(true);
                    selectedCount = 1;
                }
            } else {
                selectedCount = 0;
            }
        });


        card.add(masterCheck);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(buildDivider());
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(popupRow);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(overlayRow);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(soundRow);

        add(card, BorderLayout.NORTH);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public boolean isEnabled()          { return masterCheck.isSelected(); }
    public DataRow popupRow()   { return popupRow; }
    public DataRow overlayRow() { return overlayRow; }
    public DataRow soundRow()   { return soundRow; }
    public void setDraft(SessionDraft draft) { this.draft = draft; }

    public void loadFromDraft() {
        boolean selectable = draft.isNotificationsEnabled();
        masterCheck.setSelected(selectable);

        if  (selectable) {
            popupRow.setRowEnabled(true);
            overlayRow.setRowEnabled(true);
            soundRow.setRowEnabled(true);
            popupRow.setRowChecked(draft.isPopupEnabled());
            overlayRow.setRowChecked(draft.isOverlayEnabled());
            soundRow.setRowChecked(draft.isSoundEnabled());

            for (DataRow row : new DataRow[]{popupRow, overlayRow, soundRow} ){
                if (row.isRowChecked())
                    selectedCount++;
            }
        }
        else {
            popupRow.setRowChecked(false);
            popupRow.setRowEnabled(false);
            overlayRow.setRowChecked(false);
            overlayRow.setRowEnabled(false);
            soundRow.setRowChecked(false);
            soundRow.setRowEnabled(false);
        }

        popupRow.setMinutes(draft.getPopupInterval()/60);
        overlayRow.setMinutes(draft.getOverlayInterval()/60);
        soundRow.setMinutes(draft.getSoundInterval()/60);

    };

    public void applyToDraft(SessionDraft draft) {
        draft.setNotificationSettings(masterCheck.isSelected(), popupRow.isRowChecked(), overlayRow.isRowChecked(), soundRow.isRowChecked(), popupRow.getMinutes()*60, overlayRow.getMinutes()*60, soundRow.getMinutes()*60);
    }

    // ── Private ────────────────────────────────────────────────────────────────

    private void onRowToggled(DataRow row) {

        if (!masterCheck.isSelected()) return;

        if (row.isRowChecked()) {
            selectedCount++;
        } else {
            // Block uncheck if this was the last selected row
            if (selectedCount <= 1) {
                row.setRowChecked(true); // revert
            } else {
                selectedCount--;
            }
        }
    }

    private void styleCheckBox(JCheckBox cb) {
        cb.setFont(Theme.FONT_BODY);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JSeparator buildDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }
}
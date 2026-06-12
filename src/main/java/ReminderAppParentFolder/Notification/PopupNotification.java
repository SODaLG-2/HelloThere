package ReminderAppParentFolder.Notification;

import javax.swing.*;
import java.awt.Component;

public class PopupNotification implements BaseNotification {
    private String message;
    private String[] options;
    private String title;

    // CRITICAL: Keep a reference to the active window so external systems can kill it
    private JDialog activeDialog;
    private JOptionPane optionPane;

    public PopupNotification() {}

    private String ModifyMessage(String message, int mType) {
        return (mType == 0)
                ? "<html><body><p style='width: 300px;'>You have stuff like :<br>" + message + "<br>So just asking...<br>Want me to bring up the list?</p></body></html>"
                : "<html><body><p style='width: 300px;'>" + message + "</p></body></html>";
    }

    public void setMessageParams(String title, String originalMessage, String[] options, int messageType) {
        this.title = title;
        this.options = options;
        this.message = ModifyMessage(originalMessage, messageType);
    }

    @Override
    public int notifyUser() {
        // 1. Create the pane object explicitly instead of calling the static shortcut
        optionPane = new JOptionPane(
                message,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                null
        );

        // 2. Create the window frame container and save it to our field
        activeDialog = optionPane.createDialog(null, title);

        activeDialog.setAlwaysOnTop(true); // Forces it above browsers/IDEs
        activeDialog.toFront();

        // 3. This blocks the calling thread completely until closed or disposed!
        activeDialog.setVisible(true);

        // 4. Once unblocked, see what happened
        Object selectedValue = optionPane.getValue();

        // If the window was destroyed by cancelNotification(), value will be UNINITIALIZED_VALUE
        if (selectedValue == null || selectedValue == JOptionPane.UNINITIALIZED_VALUE) {
            return JOptionPane.CLOSED_OPTION; // Returns -1
        }

        // Match the selection string back to the options array index
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selectedValue)) {
                return i; // Returns 0 ("Yes"), 1 ("No"), etc.
            }
        }

        return JOptionPane.CLOSED_OPTION;
    }

    /**
     * Call this from SessionManager when a session is stopped or canceled!
     */
    public void cancelNotification() {
        // SwingUtilities ensures the window teardown happens safely on the main UI thread
        SwingUtilities.invokeLater(() -> {
            if (activeDialog != null && activeDialog.isShowing()) {
                // Force set an empty value so the checker doesn't crash
                if (optionPane != null) {
                    optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                }
                activeDialog.dispose(); // Instantly tears down the popup window!
            }
        });
    }

    @Override
    public String getType() { return "Popup"; }
}
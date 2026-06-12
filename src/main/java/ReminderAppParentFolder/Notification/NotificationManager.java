package ReminderAppParentFolder.Notification;

import ReminderAppParentFolder.Ui.OverlayNotification;

import java.util.function.Consumer;

/**
 * Central notification dispatcher/manager (Asynchronous, event-driven version)
 */
public class NotificationManager {

    private final PopupNotification popupNotification;
    private final OverlayNotification overlayNotification;
    private final SoundNotification soundNotification;

    private static NotificationManager instance;

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    private NotificationManager() {
        popupNotification = new PopupNotification();
        overlayNotification = OverlayNotification.getInstance();
        soundNotification = new SoundNotification();
    }

    // ─────────────────────────────────────────────
    // POPUP NOTIFICATIONS (NOW TRULY ASYNC)
    // ─────────────────────────────────────────────

    /**
     * Triggers a task-specific popup notification asynchronously.
     * The result code (-2 for timeout, -1 for closed, 0+ for option index) is relayed via callback.
     */
    public void taskPopupNotification(String title, String message, String[] options, Consumer<Integer> callback) {
        new Thread(() -> {
            popupNotification.setMessageParams(title, message, options, 0);
            int result = popupNotification.notifyUser();

            // Relay the result back to your business logic / tracker
            if (callback != null) {
                callback.accept(result);
            }
        }).start();
    }

    // Inside NotificationManager.java

    public int defaultPopupNotification(String title, String message, String[] options) {
        popupNotification.setMessageParams(title, message, options, 1);
        return popupNotification.notifyUser(); // Blocks and returns 0, 1, or -1
    }

    public void cancelCurrentPopup() {
        popupNotification.cancelNotification();
    }

    /**
     * Allows an external system (like the Idle Tracker) to force-cancel
     * any running popup immediately.
     */
    // ─────────────────────────────────────────────
    // SOUND
    // ─────────────────────────────────────────────

    public void playSoundForSeconds(int seconds) {
        soundNotification.playForSeconds(seconds);
    }

    public boolean isSoundPlaying() {
        return soundNotification.isPlaying();
    }

    public void stopSound() {
        soundNotification.stop();
    }

    public int playSoundInteractive() {
        return soundNotification.notifyUser();
    }

    // ─────────────────────────────────────────────
    // OVERLAY (ASYNC)
    // ─────────────────────────────────────────────

    // Inside NotificationManager.java

    /**
     * Routes the overlay activation request straight into your Singleton window frame.
     */
    public void defaultOverlayNotification(String message, Consumer<NotificationResult> callback) {
        OverlayNotification.getInstance().showNotification(
                new OverlayNotification.Config(message)
                        .withButtons(
                                new OverlayNotification.NotificationButton("It's going alright", NotificationResult.YES),
                                new OverlayNotification.NotificationButton("Busy here!", NotificationResult.DISMISS)
                        ),
                callback
        );
    }

    public void blankOverlayNotification(String message, Consumer<NotificationResult> callback) {
        OverlayNotification.getInstance().showNotification(
                new OverlayNotification.Config(message).withNoButtons(),
                callback
        );
    }
    /**
     * Global cancel switch executed by SessionManager during teardowns
     */
    public void cancelActiveOverlay() {
        // Simply instruct the visual window to fold itself down and report status updates
        OverlayNotification.getInstance().forceClose();
    }
}
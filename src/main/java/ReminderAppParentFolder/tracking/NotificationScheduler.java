package ReminderAppParentFolder.tracking;

import ReminderAppParentFolder.Notification.NotificationManager;
import ReminderAppParentFolder.Session.SessionDraft;
import ReminderAppParentFolder.Notification.NotificationResult;
import ReminderAppParentFolder.Ui.MainWindow;

import javax.swing.Timer;
import java.time.LocalDateTime;

/**
 * Manages background tracking for multiple concurrent notification types.
 * Runs distinct, independent timers for Sound, Overlay, and Popup alerts
 * based on the individual configurations defined inside the SessionDraft.
 */
public class NotificationScheduler {

    // Distinct timer engines running concurrently
    private Timer soundTimer;
    private Timer overlayTimer;
    private Timer popupTimer;
    private Timer checkIdleTimer;

    private final NotificationManager notificationManager = NotificationManager.getInstance();
    private final ActivityTracker     activityTracker;
    private final SessionLog sessionLog;
    private final SessionDraft currentTask;
    private final MainWindow mainWindow;

    // Gatekeeper flag specifically for the blocking popup thread
    // Gatekeeper flags specifically to track independent blocking windows
    private boolean isPopupActive = false;
    private boolean isOverlayActive = false; // Add this!
    private boolean isIdleOverlayActive = false;

    /**
     * Constructs the scheduler and initializes separate clocks for each enabled notification type.
     */
    public NotificationScheduler(SessionDraft task, SessionLog sessionLog, MainWindow mainWindow) {
        this.currentTask = task;
        this.sessionLog = sessionLog;
        this.mainWindow = mainWindow;
        this.activityTracker = ActivityTracker.getInstance();

        initializeTimers(task);
    }

    /**
     * Inspects configuration booleans and instantiates specific interval loops.
     */
    private void initializeTimers(SessionDraft task) {
        // NOTE: If your draft methods return minutes, remember to add: * 60 * 1000

        // 1. Setup Sound Timer if enabled
        if (task.getNotifications().isSoundEnabled()) {
            int soundMs = task.getSoundInterval() * 1000;
            this.soundTimer = new Timer(soundMs, e -> fireSoundChime());
        }

        // 2. Setup Overlay Timer if enabled
        if (task.getNotifications().isOverlayEnabled()) {
            int overlayMs = task.getOverlayInterval() * 1000;
            this.overlayTimer = new Timer(overlayMs, e -> fireScreenOverlay());
        }

        // 3. Setup Popup Timer if enabled
        if (task.getNotifications().isPopupEnabled()) {
            int popupMs = task.getPopupInterval() * 1000;
            this.popupTimer = new Timer(popupMs, e -> firePopupCheck());
        }

        this.checkIdleTimer = new Timer(1000, e -> fireIdleScreenCheck(activityTracker.isUserIdle()));
    }

    /**
     * Powers up all enabled background timer wheels simultaneously.
     */
    public void start() {
        System.out.println("[Scheduler] Activating multi-interval engines for: " + currentTask.getSessionName());
        if (soundTimer != null) soundTimer.start();
        if (overlayTimer != null) overlayTimer.start();
        if (popupTimer != null) popupTimer.start();
        if (checkIdleTimer != null) checkIdleTimer.start();
    }

    /**
     * Instantly halts all active countdowns cleanly.
     */
    public void stop() {
        System.out.println("[Scheduler] Disabling all background countdown loops.");
        if (soundTimer != null) soundTimer.stop();
        if (overlayTimer != null) overlayTimer.stop();
        if (popupTimer != null) popupTimer.stop();
        if (checkIdleTimer != null) checkIdleTimer.stop();
    }

    // ─────────────────────────────────────────────────────────────
    // INDEPENDENT INTERVAL FIRE ROUTINES
    // ─────────────────────────────────────────────────────────────

    private void fireSoundChime() {
        System.out.println("[Scheduler] Sound Interval Tripped.");
        final LocalDateTime firedAt = LocalDateTime.now();

        // FIX 1: If a sound is already playing or a previous tracking trap is active,
        // explicitly log it as "Missed" or "Overwritten" before replacing it.
        if (notificationManager.isSoundPlaying()) {
            System.out.println("[Scheduler] Previous notification was still active. Logging as Missed.");

            notificationManager.stopSound();

            // Commit the missed notification record to your vault
            NotificationRecord missedRecord = new NotificationRecord(
                    firedAt, // Or pass a previously cached timestamp if you track it globally
                    "Sound",
                    "Missed",
                    -1L // Use -1 or a constant to indicate no reaction took place
            );
            sessionLog.addNotificationRecord(missedRecord);
        }

        // Start the new sound
        if (notificationManager.playSoundInteractive()!=0) return;


        // Keep your 5-second buffer timer
        Timer timer = new Timer(5000, e -> {
            final long soundEndTime = System.currentTimeMillis();

            // Prime the tracker speed trap for the NEW notification
            activityTracker.primeReactionCapture(soundEndTime, (reactionTimeMs) -> {

                // This logic runs automatically the split-second the user moves!
                notificationManager.stopSound();
                System.out.println("[Telemetry] Sound reaction caught! User moved after: " + reactionTimeMs + "ms");

                String responseInterpretation;
                if (reactionTimeMs < 1000) {
                    responseInterpretation = "Immediate";
                } else if (reactionTimeMs <= 5000) {
                    responseInterpretation = "Delayed";
                } else {
                    responseInterpretation = "Returned";
                }

                NotificationRecord soundRecord = new NotificationRecord(
                        firedAt,
                        "Sound",
                        responseInterpretation,
                        reactionTimeMs
                );

                sessionLog.addNotificationRecord(soundRecord);
                System.out.println("[Scheduler] Sound telemetry committed successfully.");
            }, Long.MAX_VALUE);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void fireScreenOverlay() {
        // 1. SAFETY GATE: Skip if a previous overlay panel is still actively rendering
        if (isOverlayActive) {
            System.out.println("[Scheduler] Overlay skipped: Previous overlay window still open.");
            return;
        }

        // 2. Lock the independent overlay gate
        this.isOverlayActive = true;
        System.out.println("[Scheduler] Overlay Interval Tripped. Deploying screen overlay layer...");

        // Capture telemetry start metrics before passing execution down
        final LocalDateTime shownAt = LocalDateTime.now();
        final long startTime = System.currentTimeMillis();

        // 3. Fire the asynchronous overlay layout manager
        notificationManager.defaultOverlayNotification(
                "Take a quick breather! Is everything going alright?",
                result -> {
                    // ─────────────────────────────────────────────────────────────
                    // THIS LAMBDA CALLBACK EXECUTES LATER WHEN THE OVERLAY CLOSES!
                    // ─────────────────────────────────────────────────────────────
                    try {
                        // Turn numerical action integer results into legible log data
                        String responseEquiv = (result == NotificationResult.YES) ? "Acknowledged" : "Close";
                        long reactionTime = System.currentTimeMillis() - startTime;

                        // Bundle data records and commit safely to history storage structures
                        NotificationRecord record = new NotificationRecord(
                                shownAt,
                                "OverlayPanel",
                                responseEquiv,
                                reactionTime
                        );
                        sessionLog.addNotificationRecord(record);

                        System.out.println("[Scheduler] Overlay Telemetry saved! Reaction time: "
                                + reactionTime + "ms. Action: " + responseEquiv);

                    } catch (Exception ex) {
                        System.err.println("[Scheduler] Error writing telemetry inside overlay callback.");
                        ex.printStackTrace();
                    } finally {
                        // 4. CRITICAL UNLOCK: Always clear the gate lock when the callback fires
                        this.isOverlayActive = false;
                        System.out.println("[Scheduler] Overlay gate cleared. Ready for next loop.");
                    }
                }
        );
    }

    private void firePopupCheck() {
        // CRITICAL SAFETY GATE: Skip if the previous popup is still open on screen
        if (isPopupActive) {
            System.out.println("[Scheduler] Popup skipped: Previous alert window still open.");
            return;
        }

        this.isPopupActive = true;

        // Spin up a background thread wrapper so the blocking dialog box
        // doesn't halt your sound/overlay loops or main UI dashboard panels
        new Thread(() -> {
            try {
                final LocalDateTime shownAt = LocalDateTime.now();
                final long startTime = System.currentTimeMillis();
                String[] options = {"Yes", "No"};

                System.out.println("[Scheduler] Launching blocking focus confirmation box...");
                int result = notificationManager.defaultPopupNotification(
                        "Focus Check~" + currentTask.getSessionName(),
                        "Just checking in~ Wanna look at the list again?",
                        options
                );

                String responseEquiv = (result == 0) ? "Yes" : (result == 1) ? "No" : "Close";
                long reactionTime = System.currentTimeMillis() - startTime;

                // Log metrics safely to database or array structures
                sessionLog.addNotificationRecord(new NotificationRecord(shownAt, "Popup", responseEquiv, reactionTime));
                System.out.println("[Scheduler] Telemetry saved. Choice: " + responseEquiv);
                System.out.println(reactionTime+"ms");
                if (responseEquiv.equals("Yes")) {
                    mainWindow.toFront();
                }

            } catch (Exception ex) {
                System.err.println("[Scheduler] Exception inside popup tracking sequence thread pipeline.");
                ex.printStackTrace();
            } finally {
                // Unlock gate frame
                isPopupActive = false;
                System.out.println("[Scheduler] Popup gate cleared. Standing by for next sequence.");
            }
        }).start();
    }

    private void fireIdleScreenCheck(boolean isUserIdle) {
        if (!isUserIdle) return;
        if (isIdleOverlayActive) {
            System.out.println("[Scheduler] Idle Overlay skipped: Previous overlay window still open.");
            return;
        }

        this.isIdleOverlayActive = true;
        System.out.println("[Scheduler] Idle overlay screen Tripped. Deploying screen overlay layer...");

        stop(); // Pauses notification timers only — ActivityTracker hooks stay alive
        notificationManager.cancelActiveOverlay();
        notificationManager.cancelCurrentPopup();
        notificationManager.stopSound();

        final LocalDateTime shownAt = LocalDateTime.now();

        notificationManager.blankOverlayNotification("IDLE THRESHOLD EXCEEDED!", result -> {});

        activityTracker.primeReactionCapture(System.currentTimeMillis(), (reactionTimeMs) -> {
            this.isIdleOverlayActive = false;
            notificationManager.cancelActiveOverlay();

            NotificationRecord record = new NotificationRecord(
                    shownAt,
                    "IdleOverlay",
                    "Returned",
                    reactionTimeMs
            );
            sessionLog.addNotificationRecord(record);
            System.out.println("[Scheduler] Idle overlay telemetry committed. User returned after: " + reactionTimeMs + "ms");

            start();
        }, Long.MAX_VALUE);
    }
}
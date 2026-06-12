package ReminderAppParentFolder.tracking;

import ReminderAppParentFolder.Notification.NotificationManager;
import ReminderAppParentFolder.Session.SessionDraft;
import ReminderAppParentFolder.Storage.StorageManager;
import ReminderAppParentFolder.Ui.MainWindow;
import ReminderAppParentFolder.Ui.SessionActivePanel;
import ReminderAppParentFolder.Ui.SessionLogPanel;
import ReminderAppParentFolder.Ui.TaskOverlayWidget;

import javax.swing.SwingUtilities;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * The core mission-control engine for an active focus session.
 * Manages background tracking threads, session lifecycles, and coordinate
 * telemetry compilation when a tracking window concludes.
 */
public class SessionManager {

    // Singleton Instance Blueprint
    private static SessionManager instance;

    // Background State Trackers
    private boolean isSessionRunning = false;
    private long sessionStartTimeMillis;
    long totalElapsedSeconds;
    private Thread clockThread;

    // Internal Subsystem Workers (Owned by this Manager)
    private NotificationScheduler currentScheduler;
    private final SessionLog sessionLog;


    // Passive UI Reference Hook
    private SessionActivePanel activeUiPanel;
    private MainWindow         mainWindow;
    private SessionLogPanel    logPanel;

    TaskOverlayWidget  taskOverlayWidget = new TaskOverlayWidget();

    private SessionDraft sessionDraft;

    /**
     * Private constructor enforces the Singleton structural pattern.
     */
    private SessionManager() {
        this.sessionLog = new SessionLog(); // Initializes central data vault
    }

    /**
     * Global access point to grab the single execution engine instance.
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Binds the passive visual UI panel to the manager engine so it knows where
     * to push ticking clock strings.
     */
    public void registerUiPanel(SessionActivePanel panel) {
        this.activeUiPanel = panel;
    }

    public void registerMainFrame(MainWindow window) {
        this.mainWindow = window;
    }

    public void registerLogPanel(SessionLogPanel logPanel) {
        this.logPanel = logPanel;
    }

    // ─────────────────────────────────────────────────────────────
    // CORE CORE LIFECYCLE CONTROLS
    // ─────────────────────────────────────────────────────────────

    /**
     * Kicks off the entire backend data engine for an active session.
     * Called up the chain by your UI panel's Start button listener.
     */
    public void startActiveSession(SessionDraft activeDraft) {
        if (isSessionRunning) {
            System.out.println("[Engine] Session already running. Aborting duplication request.");
            return;
        }

        System.out.println("[Engine] Initializing session lifecycle for: " + activeDraft.getSessionName());
        this.isSessionRunning = true;
        this.sessionStartTimeMillis = System.currentTimeMillis();

        // 1. Activate the background analytics tracker
        this.sessionDraft = activeDraft;
        if (sessionDraft.isIdleUsed())
            ActivityTracker.getInstance(sessionDraft.getIdleThreshold()*1000L).startTracking(); //this might be triggering startTracking before being properly activated... Clearing instance upon stopping might help

        // 2. Fire up and hand off parameters to the Notification Scheduler worker
        this.currentScheduler = new NotificationScheduler(activeDraft, this.sessionLog, mainWindow);
        this.currentScheduler.start();



        // 3. Spin up the Visual Timer Thread Loop
        startVisualClockLoop(activeDraft.getExpectedDuration());

        if (sessionDraft != null && sessionDraft.getTaskOverlayUsage()) {
            activeUiPanel.activateTaskOverlay();
        }

    }

    /**
     * Gracefully concludes a session, compiles telemetry analytics metrics,
     * saves records to history, and completely flushes running components.
     */
    public void stopActiveSession() {
        if (!isSessionRunning) return;
        this.isSessionRunning = false; // Tells threads to collapse and terminate

        // 1. Teardown background workers cleanly
        if (this.currentScheduler != null) {
            this.currentScheduler.stop();
        }
        // long totalIdleTime = this.idleTracker.stopAndGetTotalIdle();

        // 2. Clear any lingering popup notifications left on screen
        NotificationManager.getInstance().cancelCurrentPopup();
        NotificationManager.getInstance().stopSound();
        activeUiPanel.deactivateTaskOverlay();

        // 3. Compile the structural metadata and finalize telemetry data logs
        totalElapsedSeconds = (System.currentTimeMillis() - sessionStartTimeMillis) / 1000;
        System.out.println("[Engine] Session finished safely. Total active time: " + totalElapsedSeconds + "s");

        // sessionLog.compileSessionEffectiveness(sessionStartTimeMillis, totalIdleTime);

        convertToLog();
        ActivityTracker.getInstance().stopTracking();

        SessionSettingEvaluator evaluator = new SessionSettingEvaluator();
        evaluator.calculateSimpleIntervalAdjustments(sessionDraft, sessionLog);

        StorageManager.getInstance().logs().save(sessionLog);
        if (sessionDraft.getTaskOverlayUsage())
            activeUiPanel.deactivateTaskOverlay();
        logPanel.addSession(new SessionLogPanel.Session(sessionLog.getSessionName(), sessionLog.getCreatedAt(), logPanel.stringCompletion2Completion(sessionLog.getCompletionStatus()), sessionLog.getDuration()));
    }

    public void cancelActiveSession() {
        if (!isSessionRunning) return;
        this.isSessionRunning = false; // Tells threads to collapse and terminate

        // 1. Teardown background workers cleanly
        if (this.currentScheduler != null) {
            this.currentScheduler.stop();
        }
        // long totalIdleTime = this.idleTracker.stopAndGetTotalIdle();

        // 2. Clear any lingering popup notifications left on screen
        NotificationManager.getInstance().cancelCurrentPopup();
        NotificationManager.getInstance().stopSound();
        activeUiPanel.deactivateTaskOverlay();

        // 3. Compile the structural metadata and finalize telemetry data logs
        totalElapsedSeconds = (System.currentTimeMillis() - sessionStartTimeMillis) / 1000;
        System.out.println("[Engine] Session finished safely. Total active time: " + totalElapsedSeconds + "s");

        // sessionLog.compileSessionEffectiveness(sessionStartTimeMillis, totalIdleTime);

        convertToLog();
        ActivityTracker.getInstance().stopTracking();

        SessionSettingEvaluator evaluator = new SessionSettingEvaluator();
        evaluator.calculateSimpleIntervalAdjustments(sessionDraft, sessionLog);

        StorageManager.getInstance().logs().save(sessionLog);
        if (sessionDraft.getTaskOverlayUsage())
            activeUiPanel.deactivateTaskOverlay();
        logPanel.addSession(new SessionLogPanel.Session(sessionLog.getSessionName(), sessionLog.getCreatedAt(), logPanel.stringCompletion2Completion(sessionLog.getCompletionStatus()), sessionLog.getDuration()));
    }

    /**
     * Instantly aborts the active tracking sequence, tearing down popup
     * dialog frames without committing the data to the history log vault.
     */
    public void discardActiveSession() {
        if (!isSessionRunning) return;
        this.isSessionRunning = false;

        if (this.currentScheduler != null) {
            this.currentScheduler.stop();
        }

        NotificationManager.getInstance().cancelCurrentPopup();
        NotificationManager.getInstance().stopSound();
        ActivityTracker.getInstance().stopTracking();
        activeUiPanel.deactivateTaskOverlay();
        System.out.println("[Engine] Session completely discarded by user. History not recorded.");
    }

    // ─────────────────────────────────────────────────────────────
    // INTERNAL TIMEKEEPING MACHINERY
    // ─────────────────────────────────────────────────────────────

    /**
     * Drives the countdown clock engine on an independent background worker thread.
     */
    private void startVisualClockLoop(int totalSecondsExpected) {
        final int totalSecondsRequired = totalSecondsExpected;

        clockThread = new Thread(() -> {
            while (isSessionRunning) {
                try {
                    Thread.sleep(1000); // Process metrics strictly once a second

                    // Calculate elapsed and remaining allocations
                    long currentElapsedSeconds = (System.currentTimeMillis() - sessionStartTimeMillis) / 1000;
                    long secondsRemaining = totalSecondsRequired - currentElapsedSeconds;

                    if (secondsRemaining == 0) {
                        if (NotificationManager.getInstance().defaultPopupNotification("Time's up!", "Should I bring up the window, or would you like to simply continue?", new String[] {"Bring up the list", "I would like to continue"})==0) {
                            mainWindow.toFront();
                        }
                    }

                    // Turn raw integers into formatted HH:MM:SS text sequences
                    final String elapsedStr = formatTimeStrings(currentElapsedSeconds);

                    // Safely dispatch formatted text down to the passive UI display
                    if (activeUiPanel != null) {
                        SwingUtilities.invokeLater(() -> {
                            activeUiPanel.updateElapsedTimeDisplay(elapsedStr);
                        });
                    }

                    if (!isSessionRunning) {
                        stopActiveSession(); // Wrap up operations at expiration
                        break;
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Acknowledge early thread interruption requests
                    break;
                }
            }
        });
        clockThread.start();
    }

    /**
     * Helper logic converts total numerical seconds into an aligned monospaced string.
     */
    private String formatTimeStrings(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void convertToLog() {
        String stopMethod = activeUiPanel.sessionStopMethod();
        if (stopMethod.equalsIgnoreCase("Discard")) return;

        sessionLog.setSessionName(sessionDraft.getSessionName());
        sessionLog.setAssignedTasks(sessionDraft.getTasks());
        sessionLog.setExpectedDurationMinutes(sessionDraft.getExpectedDuration()/60);
        sessionLog.setActualDurationMinutes((int)totalElapsedSeconds/60);
        LocalDateTime startTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(sessionStartTimeMillis),
                ZoneId.systemDefault()
        );
        LocalDateTime endTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(sessionStartTimeMillis+totalElapsedSeconds*1000),
                ZoneId.systemDefault()
        );
        sessionLog.setStartTime(startTime);
        sessionLog.setEndTime(endTime); //change

        sessionLog.setTotalIdleTriggered(ActivityTracker.getInstance().getTotalIdleCount()); //set later
        System.out.println(stopMethod);
        if (stopMethod.equalsIgnoreCase("Finish") && activeUiPanel.getUnFinishedCount()==0) {
            sessionLog.setCompletionStatus("Finished");
        } else if (stopMethod.equalsIgnoreCase("Finish") && activeUiPanel.getUnFinishedCount()!=0) {
            sessionLog.setCompletionStatus("Incomplete");
        } else if (stopMethod.equalsIgnoreCase("Cancel")) {
            sessionLog.setCompletionStatus("Cancelled");
        }


        totalElapsedSeconds = 0;
    }

    // Accessor to look at history files down the road
    public SessionLog getSessionLog() { return sessionLog; }
    public boolean isSessionRunning() { return isSessionRunning; }
}
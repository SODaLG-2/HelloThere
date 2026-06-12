package ReminderAppParentFolder.tracking;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.dispatcher.VoidDispatchService;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityTracker implements NativeKeyListener, NativeMouseMotionListener {

    private static ActivityTracker instance;

    private final AtomicLong lastActivityTime = new AtomicLong();
    private volatile boolean isUserIdle = false;
    private final AtomicInteger totalIdleCount = new AtomicInteger(0);

    private static final long MOUSE_THROTTLE_MS = 100;
    private final AtomicLong lastMouseEventTime = new AtomicLong(0L);
    private volatile long idleStartTime = 0L;

    private Timer watchdogTimer;
    private final long idleThresholdMs;

    private ScheduledExecutorService expiryScheduler;
    private ScheduledFuture<?> pendingExpiry;

    private record ReactionCapture(long soundEndTimeMillis, java.util.function.Consumer<Long> callback) {}
    private final AtomicReference<ReactionCapture> pendingReaction = new AtomicReference<>(null);

    private ExecutorService activityExecutor;

    private ActivityTracker(long idleThresholdMs) {
        this.idleThresholdMs = idleThresholdMs;
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);
    }

    public static synchronized ActivityTracker getInstance(long idleThresholdMs) {
        if (instance == null) {
            instance = new ActivityTracker(idleThresholdMs);
        }
        return instance;
    }

    public static synchronized ActivityTracker getInstance() {
        return instance;
    }

    public synchronized void startTracking() {
        if (watchdogTimer != null && watchdogTimer.isRunning()) {
            return;
        }

        if (activityExecutor == null || activityExecutor.isShutdown()) {
            activityExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "activity-processor");
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            });
        }

        if (expiryScheduler == null || expiryScheduler.isShutdown()) {
            expiryScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "reaction-expiry");
                t.setDaemon(true);
                return t;
            });
        }

        try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
                GlobalScreen.setEventDispatcher(new VoidDispatchService());
            }

            GlobalScreen.addNativeKeyListener(this);
            GlobalScreen.addNativeMouseMotionListener(this);

            lastActivityTime.set(System.currentTimeMillis());
            isUserIdle = false;

            if (watchdogTimer != null) watchdogTimer.stop();
            this.watchdogTimer = new Timer(1000, e -> checkIdleStatus());
            this.watchdogTimer.start();

            System.out.println("[Tracker] Global Activity Hook initialized successfully.");
        } catch (NativeHookException ex) {
            System.err.println("[Tracker] Failed to register global native hooks: " + ex.getMessage());
        }
    }

    public synchronized void stopTracking() {
        if (watchdogTimer != null) watchdogTimer.stop();

        GlobalScreen.removeNativeKeyListener(this);
        GlobalScreen.removeNativeMouseMotionListener(this);

        if (activityExecutor != null) activityExecutor.shutdownNow();
        if (expiryScheduler != null) expiryScheduler.shutdownNow();

        try {
            if (GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (NativeHookException ex) {
            System.err.println("[Tracker] Failed to unregister native hook cleanly: " + ex.getMessage());
        }
        if (instance != null) { instance = null; }
        System.out.println("[Tracker] Global Activity Hook deactivated cleanly.");
    }

    public void primeReactionCapture(long soundEndTime, java.util.function.Consumer<Long> callback, long expiryMs) {
        if (pendingExpiry != null && !pendingExpiry.isDone()) {
            pendingExpiry.cancel(false);
        }

        ReactionCapture capture = new ReactionCapture(soundEndTime, callback);
        pendingReaction.set(capture);

        if (expiryScheduler != null && !expiryScheduler.isShutdown()) {
            pendingExpiry = expiryScheduler.schedule(
                    () -> pendingReaction.compareAndSet(capture, null), // Only clears if still ours
                    expiryMs, TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * Internal async processor. This runs entirely off the native UI/hook threads.
     */
    private void processActivityEvent(long eventTimestamp) {
        lastActivityTime.set(eventTimestamp);

        ReactionCapture capture = pendingReaction.getAndSet(null);
        if (capture != null) {
            if (pendingExpiry != null) pendingExpiry.cancel(false);
            long reactionTimeMs = eventTimestamp - capture.soundEndTimeMillis;
            SwingUtilities.invokeLater(() -> capture.callback.accept(reactionTimeMs));
        }
    }

    private void checkIdleStatus() {
        long timeElapsedSinceInput = System.currentTimeMillis() - lastActivityTime.get();
        if (!isUserIdle && timeElapsedSinceInput >= idleThresholdMs) {
            totalIdleCount.incrementAndGet();
            idleStartTime = System.currentTimeMillis();
            isUserIdle = true;
            System.out.println("[Tracker] Watchdog detected state swap: USER IS IDLE.");
        } else if (isUserIdle && timeElapsedSinceInput < idleThresholdMs) {
            isUserIdle = false;
            System.out.println("[Tracker] Watchdog detected state swap: USER RETURNED. " +
                    "Idle for ~" + (System.currentTimeMillis() - idleStartTime) + " ms.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // TELEMETRY API
    // ─────────────────────────────────────────────────────────────

   
    public boolean isUserIdle() { return isUserIdle; }
    public int getTotalIdleCount() { return totalIdleCount.get(); }


    // ─────────────────────────────────────────────────────────────
    // NATIVE OS INPUT EVENT INTERCEPTORS (MUST REMAIN LIGHTWEIGHT)
    // ─────────────────────────────────────────────────────────────

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        long now = System.currentTimeMillis();
        ExecutorService exec = this.activityExecutor;
        if (exec != null && !exec.isShutdown()) {
            exec.submit(() -> processActivityEvent(now));
        }
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        long now = System.currentTimeMillis();
        long lastTime = lastMouseEventTime.get();
        if (now - lastTime >= MOUSE_THROTTLE_MS) {
            if (lastMouseEventTime.compareAndSet(lastTime, now)) {
                ExecutorService exec = this.activityExecutor;
                if (exec != null && !exec.isShutdown()) {
                    exec.submit(() -> processActivityEvent(now));
                }
            }
        }
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
        long now = System.currentTimeMillis();
        long lastTime = lastMouseEventTime.get();
        if (now - lastTime >= MOUSE_THROTTLE_MS) {
            if (lastMouseEventTime.compareAndSet(lastTime, now)) {
                ExecutorService exec = this.activityExecutor;
                if (exec != null && !exec.isShutdown()) {
                    exec.submit(() -> processActivityEvent(now));
                }
            }
        }
    }

    @Override public void nativeKeyPressed(NativeKeyEvent e)  {}
    @Override public void nativeKeyReleased(NativeKeyEvent e) {}
}
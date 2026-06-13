package ReminderAppParentFolder.Session;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Math.max;

/**
 * Pure data container for a session configuration draft.
 * Notification config and tasks are delegated to their own classes.
 *
 * Passthrough getters/setters are provided for backwards compatibility
 * with existing call sites.
 */
public class SessionDraft {

    // ── Identity ───────────────────────────────────────────────────────────────

    private String           id;
    private transient String previousId;
    private String           sessionName;
    private String           savedFileName;

    // ── Session info ───────────────────────────────────────────────────────────

    private int           expectedDuration;   // seconds
    private boolean       useIdleThreshold;
    private int           idleThreshold;      // seconds
    private boolean       useOverlay;
    private int           previousIdleCount = -1;

    // ── Nested data ────────────────────────────────────────────────────────────

    private NotificationSettings notifications = new NotificationSettings();
    private transient TaskList   taskList      = new TaskList();

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    private transient boolean active;
    private LocalDateTime     createdAt;
    private LocalDateTime     lastModified;

    // ── Constructors ───────────────────────────────────────────────────────────

    public SessionDraft() {
        this("New Draft", LocalDateTime.now(), LocalDateTime.now());
    }

    public SessionDraft(String sessionName,
                        LocalDateTime creation,
                        LocalDateTime modified) {
        this.id           = UUID.randomUUID().toString();
        this.createdAt    = creation;
        this.lastModified = modified;
        this.active       = false;
        this.sessionName      = sessionName;
        this.expectedDuration = 6000;
        this.idleThreshold    = 600;
        this.useIdleThreshold = true;

        notifications.setEnabled(false);
        notifications.setPopupEnabled(true);
        notifications.setOverlayEnabled(false);
        notifications.setSoundEnabled(false);
        notifications.setPopupInterval(600);
        notifications.setOverlayInterval(600);
        notifications.setSoundInterval(600);
    }

    // ── Direct getters/setters ─────────────────────────────────────────────────

    public String               getId()               { return id; }
    public String               getSessionName()      { return sessionName; }
    public int                  getExpectedDuration() { return expectedDuration; }
    public boolean              isUseIdleThreshold()  { return useIdleThreshold; }
    public boolean              isIdleUsed()          { return useIdleThreshold; }
    public int                  getIdleThreshold()    { return idleThreshold; }
    public NotificationSettings getNotifications()    { return notifications; }
    public TaskList             getTaskList()         { return taskList; }
    public boolean              isActive()            { return active; }
    public LocalDateTime        getCreatedAt()        { return createdAt; }
    public LocalDateTime        getLastModified()     { return lastModified; }
    public boolean              getTaskOverlayUsage() { return useOverlay; }
    public String               getSavedFileName()    { return savedFileName; }
    public String               getPreviousId()       { return previousId; }
    public int                  getPreviousIdleCount() { return previousIdleCount; }

    public void setSavedFileName(String savedFileName) { this.savedFileName = savedFileName; }
    public void setId(String id)                              { this.id               = id; }
    public void setName(String sessionName)                   { this.sessionName      = sessionName; }
    public void setSessionInfo(String sessionName)            { this.sessionName      = sessionName; }
    public void setExpectedDuration(int expectedDuration)     { this.expectedDuration = max(60, expectedDuration); }
    public void setUseIdleThreshold(boolean useIdleThreshold) { this.useIdleThreshold = useIdleThreshold; }
    public void setIdleThreshold(int idleThreshold)           { this.idleThreshold    = max(60, idleThreshold); }
    public void setNotifications(NotificationSettings n)      { this.notifications    = n; }
    public void setTaskList(TaskList taskList)                 { this.taskList         = taskList; }
    public void setCreatedAt(LocalDateTime createdAt)         { this.createdAt        = createdAt; }
    public void setLastModified(LocalDateTime lastModified)   { this.lastModified     = lastModified; }
    public void setTaskOverlay(boolean enabled)               { this.useOverlay = enabled; }
    public void setPreviousId(String previousId)              { this.previousId = previousId; }
    public void setPreviousIdleCount(int count)               { this.previousIdleCount = max(0, count); }

    // ── setActive compatibility ────────────────────────────────────────────────

    public void setActive(boolean active){ this.active = active; }

    // ── Notification passthroughs ──────────────────────────────────────────────

    public boolean isNotificationsEnabled() { return notifications.isEnabled(); }
    public boolean isPopupEnabled()         { return notifications.isPopupEnabled(); }
    public boolean isOverlayEnabled()       { return notifications.isOverlayEnabled(); }
    public boolean isSoundEnabled()         { return notifications.isSoundEnabled(); }
    public int     getPopupInterval()       { return notifications.getPopupInterval(); }
    public int     getOverlayInterval()     { return notifications.getOverlayInterval(); }
    public int     getSoundInterval()       { return notifications.getSoundInterval(); }
    public int     getPreviousSoundInterval() { return notifications.getPreviousSoundInterval(); }
    public int     getPreviousPopupInterval() { return notifications.getPreviousPopupInterval(); }
    public int     getPreviousOverlayInterval() { return notifications.getPreviousOverlayInterval(); }

    public void setNotificationsEnabled(boolean enabled)      { notifications.setEnabled(enabled); }
    public void setPopupEnabled(boolean enabled)              { notifications.setPopupEnabled(enabled); }
    public void setOverlayEnabled(boolean enabled)            { notifications.setOverlayEnabled(enabled); }
    public void setSoundEnabled(boolean enabled)              { notifications.setSoundEnabled(enabled); }
    public void setPopupInterval(int interval)                { notifications.setPopupInterval(max(60, interval)); }
    public void setOverlayInterval(int interval)              { notifications.setOverlayInterval(max(60, interval)); }
    public void setSoundInterval(int interval)                { notifications.setSoundInterval(max(60, interval)); }
    public void setPreviousSoundInterval(int interval)        { notifications.setPreviousSoundInterval(max(60, interval)); }
    public void setPreviousPopupInterval(int interval)        { notifications.setPreviousPopupInterval(max(60, interval)); }
    public void setPreviousOverlayInterval(int interval)        { notifications.setPreviousOverlayInterval(max(60, interval)); }



    public void setNotificationSettings(boolean enabled,
                                        boolean popupEnabled,
                                        boolean overlayEnabled,
                                        boolean soundEnabled,
                                        int popupInterval,
                                        int overlayInterval,
                                        int soundInterval) {
        notifications.setEnabled(enabled);
        notifications.setPopupEnabled(popupEnabled);
        notifications.setOverlayEnabled(overlayEnabled);
        notifications.setSoundEnabled(soundEnabled);
        notifications.setPopupInterval(max(60, popupInterval));
        notifications.setOverlayInterval(max(60, overlayInterval));
        notifications.setSoundInterval(max(60, soundInterval));
    }

    // ── Task passthroughs ──────────────────────────────────────────────────────

    public List<String> getTasks()              { return taskList.getTasks(); }
    public void setTasks(List<String> tasks) {
        taskList.setTasks(tasks);
    }

    public void addTask(String task) {
        if (task != null && !task.isBlank()) taskList.getTasks().add(task);
    }

    public void removeTask(int index) {
        List<String> tasks = taskList.getTasks();
        if (index >= 0 && index < tasks.size()) tasks.remove(index);
    }

    // ── Bulk session info setter ───────────────────────────────────────────────

    public void setSessionInfo(String sessionName,
                               int expectedDuration,
                               int idleThreshold,
                               boolean useIdleThreshold) {
        this.sessionName      = sessionName;
        this.expectedDuration = max(60, expectedDuration);
        this.idleThreshold    = max(60, idleThreshold);
        this.useIdleThreshold = useIdleThreshold;
    }

    public void useIdleThreshold(boolean useIdleThreshold) {
        this.useIdleThreshold = useIdleThreshold;
    }

}
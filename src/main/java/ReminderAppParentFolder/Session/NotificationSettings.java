package ReminderAppParentFolder.Session;

/**
 * Pure data container for notification configuration.
 * No logic — only fields, getters, and setters.
 */
public class NotificationSettings {

    private boolean enabled;

    private boolean popupEnabled;
    private boolean overlayEnabled;
    private boolean soundEnabled;

    private int popupInterval;    // seconds
    private int overlayInterval;  // seconds
    private int soundInterval;    // seconds

    private int previousPopupInterval;
    private int previousOverlayInterval;
    private int previousSoundInterval;


    public NotificationSettings() {}

    // ── Getters ────────────────────────────────────────────────────────────────

    public boolean isEnabled()         { return enabled; }
    public boolean isPopupEnabled()    { return popupEnabled; }
    public boolean isOverlayEnabled()  { return overlayEnabled; }
    public boolean isSoundEnabled()    { return soundEnabled; }
    public int     getPopupInterval()  { return popupInterval; }
    public int     getOverlayInterval(){ return overlayInterval; }
    public int     getSoundInterval()  { return soundInterval; }
    public int     getPreviousPopupInterval() { return previousPopupInterval; }
    public int     getPreviousOverlayInterval() { return previousOverlayInterval; }
    public int     getPreviousSoundInterval() { return previousSoundInterval; }

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setEnabled(boolean enabled)               { this.enabled         = enabled; }
    public void setPopupEnabled(boolean popupEnabled)     { this.popupEnabled    = popupEnabled; }
    public void setOverlayEnabled(boolean overlayEnabled) { this.overlayEnabled  = overlayEnabled; }
    public void setSoundEnabled(boolean soundEnabled)     { this.soundEnabled    = soundEnabled; }
    public void setPopupInterval(int popupInterval)       { this.popupInterval   = popupInterval; }
    public void setOverlayInterval(int overlayInterval)   { this.overlayInterval = overlayInterval; }
    public void setSoundInterval(int soundInterval)       { this.soundInterval   = soundInterval; }
    public void setPreviousPopupInterval(int previousPopupInterval) { this.previousPopupInterval = previousPopupInterval; }
    public void setPreviousOverlayInterval(int previousOverlayInterval) { this.previousOverlayInterval = previousOverlayInterval; }
    public void setPreviousSoundInterval(int previousSoundInterval) { this.previousSoundInterval = previousSoundInterval; }
}

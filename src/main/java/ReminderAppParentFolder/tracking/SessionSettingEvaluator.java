package ReminderAppParentFolder.tracking;

import ReminderAppParentFolder.Session.SessionDraft;
import ReminderAppParentFolder.Storage.StorageManager;

public class SessionSettingEvaluator {
    public void calculateSimpleIntervalAdjustments(SessionDraft previous, SessionLog current) {
        if (!previous.isIdleUsed()) return;

        int prevIdles = (int) ((previous.getPreviousIdleCount() < 0)
                ? current.getTotalIdleTriggered()
                : previous.getPreviousIdleCount());
        int currentIdles = (int) current.getTotalIdleTriggered();

        int popupYes = 0;
        int popupNo = 0;
        int returnedCount = 0;
        int delayedCount = 0;
        int immediateCount = 0;

        for (NotificationRecord record : current.getNotificationHistory()) {
            if (record.getType().equalsIgnoreCase("popup")) {
                if (record.getReaction().equalsIgnoreCase("Yes")) popupYes++;
                else if (record.getReaction().equalsIgnoreCase("No")) popupNo++;
            } else if (record.getType().equalsIgnoreCase("Sound")) {
                if (record.getReaction().equalsIgnoreCase("Returned")) returnedCount++;
                else if (record.getReaction().equalsIgnoreCase("Delayed")) delayedCount++;
                else if (record.getReaction().equalsIgnoreCase("Immediate")) immediateCount++;
            }
        }

        int newPopupInterval    = previous.getPopupInterval()    / 60;
        int newOverlayInterval;
        int newSoundInterval    = previous.getSoundInterval()    / 60;

        // Delayed responses count as 1/3 of a returned response,
        // reflecting that delayed is a weaker signal of distraction than returned.
        int weightedSlowCount = returnedCount + Math.floorDiv(delayedCount, 3);


        // ─────────────────────────────────────────────────────────────
        // PART 1: Popup Interval Adjustments (Popup Response vs Idle Count)
        // ─────────────────────────────────────────────────────────────
        if (popupYes > popupNo) {
            if (currentIdles <= prevIdles) {
                newPopupInterval = Math.min(previous.getExpectedDuration(),
                        Math.abs(previous.getExpectedDuration() - previous.getPopupInterval())/ 2);
                // Rule A: User is engaged and less distracted — increase interval
            } else {
                newPopupInterval = Math.max(300,
                        Math.abs(Math.abs(previous.getPopupInterval() - previous.getPreviousPopupInterval())/2));
                // Rule B: User is engaged but more distracted — decrease interval
            }
        } else if (popupYes < popupNo) {
            if (currentIdles < prevIdles) {
                newPopupInterval = Math.min(previous.getExpectedDuration(),
                        Math.abs(previous.getExpectedDuration() - previous.getPopupInterval())/2);
                // Rule C: User is dismissing popups but less distracted — increase interval
            } else if (currentIdles > prevIdles) {
                newPopupInterval = Math.max(300,
                        Math.abs(previous.getPopupInterval() - previous.getPreviousPopupInterval())/2);
                // Rule D: User is dismissing popups and more distracted — decrease interval
            }
            // popupYes == popupNo: ambiguous signal, no change
        }
        // popupYes == popupNo (top level): ambiguous signal, no change


        // ─────────────────────────────────────────────────────────────
        // PART 2: Overlay Interval Adjustments
        // Note: overlay and popup intervals are adjusted independently.
        // Consider reviewing if they drift too close or invert relative to each other.
        // ─────────────────────────────────────────────────────────────
        if (currentIdles > prevIdles) {
            newOverlayInterval = Math.max(300,
                    Math.abs(previous.getOverlayInterval() - previous.getPreviousOverlayInterval())/2);
            // Distractions rose: make overlay appear sooner
        } else {
            newOverlayInterval = Math.min(previous.getExpectedDuration(),
                            + Math.abs(previous.getExpectedDuration() - previous.getOverlayInterval())/2);
            // Distractions fell or held steady: give more space
        }


        // ─────────────────────────────────────────────────────────────
        // PART 3: Sound Interval Adjustments (Reaction Speed vs Idle Count)
        // Both conditions must align to avoid adjusting on mixed signals.
        // ─────────────────────────────────────────────────────────────
        if (currentIdles > prevIdles && weightedSlowCount > immediateCount) {
            newSoundInterval = Math.max(300, Math.abs(previous.getSoundInterval() - previous.getPreviousSoundInterval())/2);
            // Sluggish response + more distractions: tighten interval (alert sooner)
        } else if (currentIdles < prevIdles && weightedSlowCount < immediateCount) {
            newSoundInterval = Math.min(previous.getExpectedDuration(),
                    Math.abs(previous.getExpectedDuration() - previous.getSoundInterval())/2);
            // Rapid response + fewer distractions: loosen interval (give more breathing room)
        }
        // Mixed signals: no change


        // ─────────────────────────────────────────────────────────────
        // PART 4: Boundary Guards
        // ─────────────────────────────────────────────────────────────
        if (newPopupInterval < 300)   newPopupInterval   = 300;    // Floor: don't spam faster than every 5 mins
        if (newPopupInterval > 3600)  newPopupInterval   = 3600;   // Ceiling: don't space out beyond 1 hour
        if (newOverlayInterval < 120) newOverlayInterval = 120;    // Overlay minimum boundary

        previous.setPreviousPopupInterval(previous.getPopupInterval());
        previous.setPreviousOverlayInterval(previous.getOverlayInterval());
        previous.setPreviousSoundInterval(previous.getSoundInterval());
        previous.setPreviousIdleCount(currentIdles);

        previous.setPopupInterval(newPopupInterval / 60 * 60);
        previous.setOverlayInterval(newOverlayInterval / 60 * 60);
        previous.setSoundInterval(newSoundInterval / 60 * 60);

        StorageManager.getInstance().sessions().save(previous);
    }
}
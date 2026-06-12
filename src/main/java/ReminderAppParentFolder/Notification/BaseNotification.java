package ReminderAppParentFolder.Notification;

/**
 * Base contract for all notification types.
 */

public interface BaseNotification {

    int notifyUser();

    String getType();
}
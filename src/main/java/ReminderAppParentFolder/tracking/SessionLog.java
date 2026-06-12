package ReminderAppParentFolder.tracking;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionLog {

    private String sessionName;
    private List<String> assignedTasks;
    private int expectedDurationMinutes;
    private int actualDurationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long totalIdleTriggered;
    private List<NotificationRecord> notificationHistory;
    private String completionStatus;
    private LocalDateTime createdAt;
    //Add a UUID method for future usage

    public SessionLog() {

        notificationHistory = new ArrayList<>();

        createdAt = LocalDateTime.now();
    }

    public void addNotificationRecord(
            NotificationRecord record) {

        notificationHistory.add(record);
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
    public void setAssignedTasks(List<String> assignedTasks) {
        this.assignedTasks = assignedTasks;
    }
    public void setExpectedDurationMinutes(int expectedDurationMinutes) {
        this.expectedDurationMinutes = expectedDurationMinutes;
    }
    public void setActualDurationMinutes(int actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public void setTotalIdleTriggered(long totalIdleTriggered) {
        this.totalIdleTriggered = totalIdleTriggered;
    }

    public long getTotalIdleTriggered() {
        return this.totalIdleTriggered;
    }
    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public String getSessionName() {
        return sessionName;
    }
    public LocalDateTime  getCreatedAt() {
        return createdAt;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public long getDuration() {
        return actualDurationMinutes;
    }

    public List<NotificationRecord> getNotificationHistory() {
        return notificationHistory;
    }
}

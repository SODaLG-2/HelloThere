package ReminderAppParentFolder.tracking;

import java.time.LocalDateTime;

public class NotificationRecord {

    private LocalDateTime timestamp;

    private String type;

    private String reaction;

    private long reactionSpeedMs;

    public NotificationRecord(
            LocalDateTime timestamp,
            String type,
            String reaction,
            long reactionSpeedMs) {

        this.timestamp = timestamp;
        this.type = type;
        this.reaction = reaction;
        this.reactionSpeedMs = reactionSpeedMs;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getReaction() {
        return reaction;
    }

    public long getReactionSpeedMs() {
        return reactionSpeedMs;
    }
}
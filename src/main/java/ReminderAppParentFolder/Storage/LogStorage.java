package ReminderAppParentFolder.Storage;

import ReminderAppParentFolder.tracking.SessionLog;

import java.nio.file.Path;
import java.util.List;

/**
 * Manager hub for session log CRUD operations.
 * Knows the log folder (provided by StorageManager via SettingsStorage).
 * Delegates all JSON parsing to JsonLogStorage.
 */
public class LogStorage {

    private final JsonLogStorage jsonStorage;

    public LogStorage(Path logFolder) {
        this.jsonStorage = new JsonLogStorage(logFolder);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public void save(SessionLog log) {
        if (log == null || log.getSessionName() == null || log.getSessionName().isBlank())
            throw new IllegalArgumentException("SessionLog must have a session name before saving.");
        jsonStorage.save(log);
    }

    public SessionLog load(String sessionName) {
        if (sessionName == null || sessionName.isBlank()) return null;
        return jsonStorage.load(sessionName);
    }

    /** Returns all logs, sorted by createdAt descending (most recent first). */
    public List<SessionLog> loadAll() {
        List<SessionLog> all = jsonStorage.loadAll();
        all.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null)
                return 0;
            if (a.getCreatedAt() == null)
                return 1;
            if (b.getCreatedAt() == null)
                return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        return all;
    }

    public void delete(String sessionName) {
        if (sessionName == null || sessionName.isBlank()) return;
        jsonStorage.delete(sessionName);
    }

    public void setPath(String path) {
        jsonStorage.setNewPath(Path.of(path));
    }
    public boolean exists(String sessionName) {
        return jsonStorage.load(sessionName) != null;
    }
}

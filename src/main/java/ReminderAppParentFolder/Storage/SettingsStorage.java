package ReminderAppParentFolder.Storage;

import java.nio.file.Path;
import java.util.Map;

/**
 * Manager hub for application settings.
 * Owns the settings file path, delegates all reads/writes to JsonSettingsStorage.
 * Provides a clean key/value API to the rest of the app.
 */

public class SettingsStorage {

    private final JsonSettingsStorage jsonStorage;
    private Map<String, String>       cache;

    public SettingsStorage(Path settingsFile) {
        this.jsonStorage = new JsonSettingsStorage(settingsFile);
        this.cache       = jsonStorage.loadAll(); // warm cache on construction
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public String get(String key) {
        return cache.getOrDefault(key, null);
    }

    public String get(String key, String defaultValue) {
        return cache.getOrDefault(key, defaultValue);
    }

    public void set(String key, String value, boolean init) {
        cache.put(key, value);
        jsonStorage.saveAll(cache);
        if (!init) {
            switch (key) {
                case "Sessions":
                    StorageManager.getInstance().sessions().setPath(value);
                    break;
                case "Logs":
                    StorageManager.getInstance().logs().setPath(value);
                    break;
                case "Sounds":
                    StorageManager.getInstance().sounds().setNewPath(Path.of(value));
            }}
    }

    /** Returns the folder path stored under the given key, or the fallback if absent. */
    public Path getPath(String key, Path fallback) {
        String val = get(key);
        return val != null ? Path.of(val) : fallback;
    }


}

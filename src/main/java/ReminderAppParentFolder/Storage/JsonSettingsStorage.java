package ReminderAppParentFolder.Storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Low-level JSON handler for application settings using Gson.
 * Settings are stored as a flat key-value map in a single file.
 * Only knows how to serialize/deserialize — file path comes from SettingsStorage.
 */

public class JsonSettingsStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>(){}.getType();

    private final Path settingsFile;

    public JsonSettingsStorage(Path settingsFile) {
        this.settingsFile = settingsFile;
        try { Files.createDirectories(settingsFile.getParent()); }
        catch (IOException e) { throw new RuntimeException("Cannot create settings folder", e); }
    }

    /** Write the full settings map to disk. */
    public void saveAll(Map<String, String> settings) {
        try (Writer w = Files.newBufferedWriter(settingsFile)) {
            GSON.toJson(settings, w);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings", e);
        }
    }

    /** Read the full settings map from disk. Returns empty map if file doesn't exist. */
    public Map<String, String> loadAll() {
        if (!Files.exists(settingsFile)) return new HashMap<>();
        try (Reader r = Files.newBufferedReader(settingsFile)) {
            Map<String, String> result = GSON.fromJson(r, MAP_TYPE);
            return result != null ? result : new HashMap<>();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load settings", e);
        }
    }
}


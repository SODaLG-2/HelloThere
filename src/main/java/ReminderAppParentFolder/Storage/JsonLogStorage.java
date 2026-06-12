package ReminderAppParentFolder.Storage;

import ReminderAppParentFolder.Util.GsonProvider;
import ReminderAppParentFolder.Util.ReservedKeywordCheck;
import ReminderAppParentFolder.tracking.SessionLog;
import com.google.gson.Gson;


import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Low-level JSON handler for SessionLog files using Gson.
 * Only knows how to serialize/deserialize — folder path comes from LogStorage.
 */
public class JsonLogStorage {

    private static final Gson GSON = GsonProvider.getGson();

    private Path folder;

    public JsonLogStorage(Path folder) {
        this.folder = folder.normalize();

        try {
            Files.createDirectories(this.folder);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Cannot create log folder: " + this.folder, e
            );
        }
    }

    public void save(SessionLog log) {
        if (ReservedKeywordCheck.InReservedKeyword(log.getSessionName())) {
            throw new RuntimeException("Failed to save log: " + log.getSessionName());
        }

        String baseName = sanitize(log.getSessionName());
        Path file = resolveUniquePath(baseName);

        System.out.println("Saving file to: " + file.toAbsolutePath());

        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(log, w);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save log: " + log.getSessionName(), e);
        }
    }

    private Path resolveUniquePath(String baseName) {
        Path file = folder.resolve(baseName + ".json");
        if (!Files.exists(file)) {
            return file;
        }

        int counter = 1;
        while (true) {
            Path candidate = folder.resolve(baseName + "-" + counter + ".json");
            if (!Files.exists(candidate)) {
                return candidate;
            }
            counter++;
        }
    }

    public SessionLog load(String sessionName) {
        Path file = folder.resolve(sanitize(sessionName) + ".json");
        if (!Files.exists(file)) return null;
        try (Reader r = Files.newBufferedReader(file)) {
            return GSON.fromJson(r, SessionLog.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load log: " + sessionName, e);
        }
    }

    public List<SessionLog> loadAll() {
        List<SessionLog> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.json")) {
            for (Path file : stream) {
                try (Reader r = Files.newBufferedReader(file)) {
                    SessionLog log = GSON.fromJson(r, SessionLog.class);
                    if (log != null) result.add(log);
                } catch (IOException e) {
                    System.err.println("Skipping unreadable log file: " + file + " — " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to open log folder: " + e.getMessage());
        }
        return result;
    }

    public void delete(String sessionName) {
        Path file = folder.resolve(sanitize(sessionName) + ".json");
        try { Files.deleteIfExists(file); }
        catch (IOException e) { throw new RuntimeException("Failed to delete log: " + sessionName, e); }
    }

    public void setNewPath(Path path) {
        this.folder = path;
    }

    private String sanitize(String name) {
        return name.trim().replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}

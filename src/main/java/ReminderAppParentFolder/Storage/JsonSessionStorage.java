package ReminderAppParentFolder.Storage;

import ReminderAppParentFolder.Util.GsonProvider;
import com.google.gson.Gson;
import ReminderAppParentFolder.Session.SessionDraft;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Low-level JSON handler for SessionDraft files using Gson.
 * Only knows how to serialize/deserialize — folder path comes from SessionStorage.
 */
public class JsonSessionStorage implements StorageProvider<SessionDraft> {

    Gson GSON = GsonProvider.getGson();

    private Path folder;

    public JsonSessionStorage(Path folder) {
        this.folder = folder;
        try { Files.createDirectories(folder); }
        catch (IOException e) { throw new RuntimeException("Cannot create session folder: " + folder, e); }
    }


    @Override
    public int save(SessionDraft draft) {
        if (draft == null) return -1;
        //Get old name
        String oldName = draft.getSavedFileName();
        //Get new name
        String newName = draft.getSessionName();
        //edit old name

        draft.setPreviousId(draft.getId());
        draft.setSavedFileName(sanitize(newName)); // lock in sanitized name, not display name
        draft.setLastModified(LocalDateTime.now());

        Path file = folder.resolve(oldName + ".json");

        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(draft, w);
            //rename to new name
            Files.move(file, folder.resolve(newName + ".json"), StandardCopyOption.REPLACE_EXISTING);
            return 0;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save session: " + draft.getSessionName(), e);
        }

    }


    @Override
    public SessionDraft load(String sessionName) {
        Path file = folder.resolve(sanitize(sessionName) + ".json");
        if (!Files.exists(file)) return null;
        try (Reader r = Files.newBufferedReader(file)) {
            SessionDraft draft = GSON.fromJson(r, SessionDraft.class);

            return draft;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load session: " + sessionName, e);
        }
    }
    @Override
    public List<String> loadAllNames() {
        List<String> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.json")) {
            for (Path file : stream) {
                try (Reader r = Files.newBufferedReader(file)) {
                    SessionDraft draft = GSON.fromJson(r, SessionDraft.class);
                    if (draft != null && draft.getSessionName() != null) {
                        result.add(draft.getSessionName());
                    } else {
                        // Fallback to filename if JSON is malformed
                        String filename = file.getFileName().toString();
                        result.add(filename.substring(0, filename.length() - 5));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read session file: " + file, e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load all sessions from: " + folder, e);
        }
        return result;
    }

    @Override
    public int delete(String sessionName) {
        Path file = folder.resolve(sanitize(sessionName) + ".json");
        try {
            Files.deleteIfExists(file);
            return 0;
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete session: " + sessionName, e);
        }
    }

    public void setNewPath(Path path) {
        this.folder = path;
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\- ]", "_");
    }


}

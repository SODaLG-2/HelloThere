package ReminderAppParentFolder.Storage;

import ReminderAppParentFolder.Session.SessionDraft;
import ReminderAppParentFolder.Util.ReservedKeywordCheck;

import java.nio.file.Path;
import java.util.*;

/**
 * Manager hub for session CRUD operations.
 * Knows the session folder (provided by StorageManager via SettingsStorage).
 * Delegates all JSON parsing to JsonSessionStorage.
 */
public class SessionStorage {

    private final JsonSessionStorage jsonStorage;

    public SessionStorage(Path sessionFolder) {
        this.jsonStorage = new JsonSessionStorage(sessionFolder);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public int save(SessionDraft draft) {
        if (draft == null || draft.getSessionName() == null || draft.getSessionName().isBlank())
            throw new IllegalArgumentException("SessionDraft must have a name before saving.");
        if (ReservedKeywordCheck.InReservedKeyword(draft.getSessionName())) {
            throw new IllegalArgumentException("SessionDraft must not be OS reserved keyword.");
        }
        if (exists(draft.getSessionName())) {
            updateNameForDuplicates(draft);
            //throw new IllegalArgumentException("SessionDraft already exists.");
        }
        //add a file savepoint check to assign a new UUID in case something fucks up here, also, it justifies having the timestamps lol.
        if (draft.getCreatedAt().equals(draft.getLastModified())) {
            draft.setId(UUID.randomUUID().toString());
        }
        return jsonStorage.save(draft);
    }

    public int saveAs(SessionDraft draft) {
        if (draft == null) return -1;
        draft.setSessionInfo(draft.getSessionName() + "-copy");
        updateNameForDuplicates(draft);
        draft.setId(UUID.randomUUID().toString());
        draft.setSavedFileName(draft.getSessionName());
        return save(draft);
    }

    public SessionDraft load(String sessionName) {
        if (sessionName == null || sessionName.isBlank()) return null;
        return jsonStorage.load(sessionName);
    }

    /** Returns all saved sessions, sorted by name. */
    public List<String> loadAllNames() {
        List<String> all = jsonStorage.loadAllNames();
        return all;
    }

    public String findUUIDSessionName(String UUID) {
        List<String> all  = jsonStorage.loadAllNames();
        for (String s : all) {
            if (load(s).getId().equals(UUID)) {
                return s;
            }
        }
        return "";
    }


    public int delete(String sessionName) {
        if (sessionName == null || sessionName.isBlank()) return -1;
        return jsonStorage.delete(sessionName);
    }

    public boolean exists(String sessionName) {
        return jsonStorage.load(sessionName) != null;
    }



    private void replace(List<String> strings)
    {
        ListIterator<String> iterator = strings.listIterator();
        while (iterator.hasNext())
        {
            iterator.set(iterator.next().toLowerCase());
        }
    }

    private void updateNameForDuplicates(SessionDraft draft) {
        List<String> fileList = loadAllNames();
        replace(fileList);

        // Build a set of existing names, excluding this draft's own file
        Set<String> otherNames = new HashSet<>();
        for (String name : fileList) {
            SessionDraft loaded = load(name);
            if (loaded != null && !draft.getId().equals(loaded.getId())) {
                otherNames.add(name.toLowerCase());
            }
        }

        String uniqueName = resolveUniqueName(draft.getSessionName(), otherNames);
        if (!uniqueName.equals(draft.getSessionName())) {
            draft.setSessionInfo(uniqueName);
        }
    }

    private String resolveUniqueName(String baseName, Set<String> existingNames) {
        if (!existingNames.contains(baseName.toLowerCase())) {
            return baseName;
        }

        int counter = 1;
        while (true) {
            String candidate = baseName + "-" + counter;
            if (!existingNames.contains(candidate.toLowerCase())) {
                return candidate;
            }
            counter++;
        }
    }

    public void setPath(String path) {
        jsonStorage.setNewPath(Path.of(path));
    }
}



package ReminderAppParentFolder.Storage;

import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * Singleton top-level storage hub.
 *
 * Responsibilities:
 *   - Holds the app data root path
 *   - Initialises SettingsStorage first (it knows where other folders live)
 *   - Constructs SessionStorage and LogStorage using paths from SettingsStorage
 *   - Provides a single access point for the rest of the app
 *
 * Usage:
 *   StorageManager.getInstance().sessions().save(draft);
 *   StorageManager.getInstance().logs().loadAll();
 */
public class StorageManager {

    // ── Settings keys ──────────────────────────────────────────────────────────
    public static final String KEY_SESSION_FOLDER_LOCATION = "Sessions";
    public static final String KEY_LOG_FOLDER_LOCATION     = "Logs";
    public static final String KEY_AUDIO_FOLDER_LOCATION = "Sounds";

    // ── Default relative paths <Fallback folder locations except for Settings.> (resolved against app root) ────────────────────
    private static final Path DEFAULT_ROOT           = Paths.get(System.getProperty("user.home"), "HelloThere");
    //private static final Path DEFAULT_ROOT           = Paths.get("C:\\ReminderApp");
    static final Path DEFAULT_SETTINGS_FILE  = DEFAULT_ROOT.resolve("Settings.json");
    static final Path DEFAULT_SESSION_FOLDER = DEFAULT_ROOT.resolve("sessions");
    static final Path DEFAULT_LOG_FOLDER     = DEFAULT_ROOT.resolve("logs");
    static final Path DEFAULT_AUDIO_FOLDER = DEFAULT_ROOT.resolve("sounds");

    // ── Single Instance ──────────────────────────────────────────────────────────────
    private static StorageManager instance;

    public static StorageManager getInstance() {
        if (instance == null)
            instance = new StorageManager(DEFAULT_SETTINGS_FILE);
        return instance;
    }

    // ── Instance ───────────────────────────────────────────────────────────────
    private final SettingsStorage settingsStorage;
    private final SessionStorage  sessionStorage;
    private final LogStorage      logStorage;
    private final AudioStorage      audioStorage;

    private StorageManager(Path settingsFile) {
        settingsStorage = new SettingsStorage(settingsFile);

        Path sessionFolder = settingsStorage.getPath(KEY_SESSION_FOLDER_LOCATION, DEFAULT_SESSION_FOLDER);
        Path logFolder     = settingsStorage.getPath(KEY_LOG_FOLDER_LOCATION,     DEFAULT_LOG_FOLDER);
        Path audioFolder  = settingsStorage.getPath(KEY_AUDIO_FOLDER_LOCATION, DEFAULT_AUDIO_FOLDER);

        settingsStorage.set(KEY_SESSION_FOLDER_LOCATION, sessionFolder.toString(), true);
        settingsStorage.set(KEY_LOG_FOLDER_LOCATION, logFolder.toString(), true);
        settingsStorage.set(KEY_AUDIO_FOLDER_LOCATION, audioFolder.toString(), true);


        sessionStorage = new SessionStorage(sessionFolder); //Not yet looked at
        logStorage     = new LogStorage(logFolder);         //Same here
        audioStorage = new AudioStorage(audioFolder);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public SettingsStorage settings() { return settingsStorage; }
    public SessionStorage  sessions() { return sessionStorage; }
    public LogStorage      logs()     { return logStorage; }
    public AudioStorage sounds()   { return audioStorage; }
}

package ReminderAppParentFolder.Storage;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple audio file storage/provider.
 *
 * Responsibilities:
 *   - Owns the audio folder path
 *   - Scans for supported audio formats
 *   - Returns audio filenames
 *   - Returns full Paths usable by javax.sound.sampled
 *
 * Currently supports:
 *   - WAV
 */
public class AudioStorage {

    // ── Supported Formats ───────────────────────────────────────────

    private static final String[] SUPPORTED_EXTENSIONS = {
            ".wav"
    };

    // ── Instance ───────────────────────────────────────────────────

    private Path folder;

    public AudioStorage(Path folder) {

        this.folder = folder;

        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Cannot create audio folder: " + folder,
                    e
            );
        }
    }

    // ── Public API ─────────────────────────────────────────────────

    /**
     * Returns all valid audio filenames.
     *
     * Example:
     *   alarm.wav
     *   notification.wav
     */
    public List<String> getAudioFileNames() {

        List<String> result = new ArrayList<>();

        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(folder)) {

            for (Path file : stream) {

                if (!Files.isRegularFile(file)) {
                    continue;
                }

                String name =
                        file.getFileName().toString();

                if (isSupported(name)) {
                    result.add(name);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to scan audio folder: " + folder,
                    e
            );
        }

        return result;
    }

    /**
     * Returns the full Path for an audio file.
     *
     * Example:
     *   getAudioPath("alarm.wav")
     */
    public Path getAudioPath(String fileName) {

        if (!isSupported(fileName)) {
            throw new IllegalArgumentException(
                    "Unsupported audio format: " + fileName
            );
        }

        Path file = folder.resolve(fileName);

        if (!Files.exists(file)) {
            throw new IllegalArgumentException(
                    "Audio file does not exist: " + fileName
            );
        }

        return file;
    }

    public void setNewPath(Path path) {
        this.folder = path;
    }
    // ── Internal Helpers ───────────────────────────────────────────

    private boolean isSupported(String fileName) {

        String lower = fileName.toLowerCase();

        for (String ext : SUPPORTED_EXTENSIONS) {

            if (lower.endsWith(ext)) {
                return true;
            }
        }

        return false;
    }
}
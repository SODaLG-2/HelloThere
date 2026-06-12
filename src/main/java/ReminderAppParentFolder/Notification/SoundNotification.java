
package ReminderAppParentFolder.Notification;

import ReminderAppParentFolder.Storage.StorageManager;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

public class SoundNotification implements BaseNotification {

    private static final StorageManager storageManager =
            StorageManager.getInstance();

    private final Random random = new Random();

    private Clip clip;
    private AudioInputStream audioInputStream;

    @Override
    public int notifyUser() {
        try {
            stop();
            List<String> sounds =
                    storageManager.sounds()
                            .getAudioFileNames();
            if (sounds.isEmpty()) {
                System.err.println(
                        "No sound files available.");
                return -2;
            }

            Path selected =
                    storageManager.sounds()
                            .getAudioPath(
                                    sounds.get(
                                            random.nextInt(sounds.size())
                                    )
                            );

            File soundFile =
                    selected.toFile()
                            .getAbsoluteFile();

            System.out.println("Playing: " + soundFile.getAbsolutePath());

            audioInputStream =
                    AudioSystem.getAudioInputStream(
                            soundFile
                    );

            clip = AudioSystem.getClip();

            clip.open(audioInputStream);

            clip.addLineListener(event -> {
                if (event.getType()
                        == LineEvent.Type.STOP) {
                    if (clip != null
                            && clip.getMicrosecondPosition()
                            >= clip.getMicrosecondLength()) {
                        cleanup();
                    }
                }
            });

            clip.start();

            return 0;

        } catch (Exception e) {

            System.err.println(
                    "Failed to play sound notification: "
                            + e.getMessage()
            );

            e.printStackTrace();

            cleanup();

            return -2;
        }
    }

    public void playForSeconds(int seconds) {
        notifyUser();
        Timer timer = new Timer(
                seconds * 1000,
                e -> stop()
        );
        timer.setRepeats(false);
        timer.start();
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            cleanup();
        }
    }

    public boolean isPlaying() {
        return clip != null
                && clip.isRunning();
    }

    private void cleanup() {
        try {
            if (clip != null) {
                clip.close();
                clip = null;
            }
            if (audioInputStream != null) {
                audioInputStream.close();
                audioInputStream = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getType() {
        return "Sound";
    }
}
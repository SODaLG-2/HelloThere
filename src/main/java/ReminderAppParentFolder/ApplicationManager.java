package ReminderAppParentFolder;
import ReminderAppParentFolder.Storage.StorageManager;
import ReminderAppParentFolder.Ui.MainWindow;
import ReminderAppParentFolder.tracking.ActivityTracker;

import javax.swing.*;

public class ApplicationManager {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

//        for (String name : StorageManager.getInstance().sessions().loadAllNames()) {
//            System.out.println(name);
//        }
        StorageManager.getInstance();
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ActivityTracker.getInstance().stopTracking();
        }));
    }

}


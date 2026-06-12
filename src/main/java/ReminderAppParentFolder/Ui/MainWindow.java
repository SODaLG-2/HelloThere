package ReminderAppParentFolder.Ui;

import ReminderAppParentFolder.Notification.NotificationManager;
import ReminderAppParentFolder.Session.SessionDraft;
import ReminderAppParentFolder.Storage.StorageManager;
import ReminderAppParentFolder.Util.SessionAction;
import ReminderAppParentFolder.tracking.SessionManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private JLabel              statusLabel;
    private SessionContentPanel sessionContentPanel;
    private SessionsPanel       sessionsPanel;
    private SettingsPanel       settingsPanel;
    private SessionLogPanel     sessionLogPanel;

    private JPanel        cardContainer;
    private CardLayout    cardLayout;

    private       SessionDraft        currentDraft;
    private final NotificationManager notificationManager = NotificationManager.getInstance();
    private final StorageManager      storageManager = StorageManager.getInstance();

    private static final String WELCOME_MESSAGE = "Good to see you!";

    public MainWindow() {
        setTitle("How's it goin'?");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 620);
        setMinimumSize(new Dimension(800, 480));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BG_DARK);
        setContentPane(root);


        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        this.sessionContentPanel.displayNoneSelectedPanel();
        this.toFront();
        SessionManager.getInstance().registerMainFrame(this);
    }

    // ── Header ─────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARK);
        header.setPreferredSize(new Dimension(0, 56));
        header.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR));

        JLabel title = new JLabel(WELCOME_MESSAGE);
        title.setBorder(new EmptyBorder(5, 15, 5, 0));
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JLabel tag = new JLabel("v1.0  ");
        tag.setFont(Theme.FONT_MONO);
        tag.setForeground(Theme.TEXT_SECONDARY);
        header.add(tag, BorderLayout.EAST);

        return header;
    }

    // ── Center ─────────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        sessionContentPanel = new SessionContentPanel();
        sessionsPanel       = new SessionsPanel(sessionContentPanel, this::onSessionAction);
        sessionsPanel.reloadSessions(true);
        sessionContentPanel.setSessionsPanel(sessionsPanel);


        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.add(sessionsPanel,       BorderLayout.NORTH);
        mainArea.add(sessionContentPanel, BorderLayout.CENTER);


        settingsPanel = new SettingsPanel(); // initialise it here
        sessionLogPanel = new SessionLogPanel();
        sessionLogPanel.forceLoadSessionLog();
        SessionManager.getInstance().registerLogPanel(sessionLogPanel);

        cardLayout    = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.add(mainArea,    "Sessions");   // key for the sessions view
        cardContainer.add(settingsPanel, "Settings"); // key for settings
        cardContainer.add(sessionLogPanel,"Logs");


        JPanel center = new JPanel(new BorderLayout());
        center.add(new SidebarPanel(this::onNavClick, sessionContentPanel), BorderLayout.WEST);
        center.add(cardContainer,                      BorderLayout.CENTER);
        return center;
    }

    // ── Footer ─────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.BG_DARK);
        footer.setPreferredSize(new Dimension(0, 32));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));

        statusLabel = new JLabel("  Ready");
        statusLabel.setFont(Theme.FONT_MONO);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        footer.add(statusLabel, BorderLayout.WEST);

        return footer;
    }

    // ── Handlers ───────────────────────────────────────────────────────────────
    private void onNavClick(String section) {
        if (!confirmDiscardChanges()) return;
        sessionContentPanel.setDraftChanged(false);
        switch (section) {
            case "New Session" -> {
                sessionsPanel.reloadSessions(true);
                cardLayout.show(cardContainer, "Sessions");
                sessionContentPanel.displayNoneSelectedPanel();
            }
            case "Settings" -> cardLayout.show(cardContainer, "Settings");
            case "Records" -> cardLayout.show(cardContainer, "Logs");
            // add more cases as your sidebar grows
            default          -> cardLayout.show(cardContainer, "Sessions");
        }

        statusLabel.setText("  Viewing: " + section);
    }


    private boolean onSessionAction(
            String sessionName,
            SessionAction action) {
        boolean result;
        result = switch (action) {
            case SELECT -> handleSessionSelection(sessionName); //unsure, probably does (It does, but for some reason, it also handles discarding new sessions when switching sessions now?)
            case NEW_SESSION -> handleNewSession(sessionName); //works
            case SAVE, SAVE_AS -> handleSaves(); //gets called, but doesn't function correctly right now. as of 6/6/2026 it mostly does. aBit jarring, but it works
            case DISCARD -> handleDiscard(); //Not even fucking called, bugger (Also, it is probably for deleting)
            default -> false;
        };

        if (result)
            sessionContentPanel.loadSession(currentDraft);
        return result;
    }

    //NOTHING IS TRIGGERING THE ACTUAL SESSION CHANGE. SessionContentPanel.loadSession is NOT being triggered!
    private boolean handleSaves() {
        System.out.println("Saving sessions...");
        String newUUID = currentDraft.getPreviousId();
        if (sessionsPanel.reloadSessions(false)) {
        sessionsPanel.selectUUIDMatch(newUUID); //might be getting cucked, it was...
        return true;
        } else
            return false;
    }

    private boolean handleSessionSelection( //weirdly enough, clicking off of sessions and selecting a new one is managed here.
            String sessionName) {
        if (sessionContentPanel.isDraftChanged()) {
            if (!confirmDiscardChanges()) {
                return false;
            }
            sessionContentPanel.setDraftChanged(false);
            if (currentDraft.getCreatedAt().equals(currentDraft.getLastModified()))
                sessionsPanel.removeLastSession();
        }
        currentDraft = storageManager.sessions().load(sessionName);
        if (currentDraft == null) {
            currentDraft = new SessionDraft();
            currentDraft.setSessionInfo(sessionName);
            currentDraft.setSavedFileName(null);
        }


        return true;
    }

    private boolean handleNewSession(
            String sessionName) {
        currentDraft = new SessionDraft();
        currentDraft.setSessionInfo(sessionName);
        currentDraft.setSavedFileName(null);

        return true;
    }

    private boolean handleDiscard() {
        if (sessionsPanel.reloadSessions(true)) {
            currentDraft = null;
            sessionContentPanel.displayNoneSelectedPanel();

        }
        return false;
    }

    private boolean confirmDiscardChanges() {
        if (!sessionContentPanel.isDraftChanged()) {
            return true;
        }

        String[] options = {"Yes", "No"};
        int result = notificationManager.defaultPopupNotification(
                "UNSAVED EDITS DETECTED!",
                "Are you sure you want to change sessions?",
                options);
        return result == 0;
    }

}

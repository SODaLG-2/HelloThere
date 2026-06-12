package ReminderAppParentFolder.Ui;

import ReminderAppParentFolder.Session.SessionDraft;
import ReminderAppParentFolder.Storage.StorageManager;
import ReminderAppParentFolder.Util.ButtonOperations;
import ReminderAppParentFolder.tracking.SessionManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Layout:
 *   Before session selected : placeholder view (centred prompt)
 *   After session selected  : top bar | vertical tabs | card area
 */
public class SessionContentPanel extends JPanel {

    private boolean draftChanged;
    private final StorageManager storageManager = StorageManager.getInstance();

    private static final String[] TAB_LABELS = { "Session Info", "Notifications", "Tasks" };

    private static final String VIEW_PLACEHOLDER = "placeholder";
    private static final String VIEW_SESSION      = "session";
    private static final String VIEW_ACTIVE_SESSION = "activeSession";

    // Top-level switcher
    private final CardLayout outerLayout    = new CardLayout();
    private final JPanel     outerContainer = new JPanel(outerLayout);

    // Inner tab/card area
    private final CardLayout cardLayout;
    private final JPanel     cardContainer;
    private final JButton[]  tabButtons = new JButton[TAB_LABELS.length];
    private final SessionActivePanel activePanel;

    private String       currentSession = null;
    private SessionDraft currentDraft   = new SessionDraft();

    private final SessionInfoCard   sessionInfoCard;
    private final NotificationsCard notificationsCard;
    private final TasksCard         tasksCard;

    private SessionsPanel onSessionSaved;

    public SessionContentPanel() {
        sessionInfoCard   = new SessionInfoCard(this);
        notificationsCard = new NotificationsCard(this);
        tasksCard         = new TasksCard(this);

        setLayout(new BorderLayout());
        setBackground(Theme.BG_CONTENT);

        // ── Session view ───────────────────────────────────────────────────────
        JPanel sessionView = new JPanel(new BorderLayout());
        sessionView.setBackground(Theme.BG_CONTENT);
        sessionView.add(buildTopBar(),       BorderLayout.NORTH);
        sessionView.add(buildVerticalTabs(), BorderLayout.WEST);

        cardLayout    = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(Theme.BG_CONTENT);
        cardContainer.setBorder(new EmptyBorder(20, 24, 20, 24));
        for (String label : TAB_LABELS) {
            cardContainer.add(buildTabCard(label), label);
        }
        sessionView.add(cardContainer, BorderLayout.CENTER);

        // -- Active Panel view --------------------------------------------------

        activePanel = new SessionActivePanel();
        activePanel.setContentPanel(this);

        SessionManager.getInstance().registerUiPanel(activePanel);

        // ── Placeholder view ───────────────────────────────────────────────────
        outerContainer.setBackground(Theme.BG_CONTENT);
        outerContainer.add(buildPlaceholderView(), VIEW_PLACEHOLDER);
        outerContainer.add(sessionView,            VIEW_SESSION);
        outerContainer.add(activePanel,    VIEW_ACTIVE_SESSION); //This should be where SessionActivePanel should be shown. And handled.
        outerLayout.show(outerContainer, VIEW_PLACEHOLDER);

        add(outerContainer, BorderLayout.CENTER);

        selectTab(0);
    }

    // ── Placeholder view ───────────────────────────────────────────────────────
    private JPanel buildPlaceholderView() {
        JPanel view = new JPanel(new GridBagLayout());
        view.setBackground(Theme.BG_CONTENT);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel icon = new JLabel("⊞");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 48));
        icon.setForeground(new Color(99, 102, 241, 80));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel line1 = new JLabel("No session selected");
        line1.setFont(new Font("Georgia", Font.ITALIC, 16));
        line1.setForeground(Theme.TEXT_SECONDARY);
        line1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel line2 = new JLabel("Press + to create one or select one above");
        line2.setFont(Theme.FONT_MONO);
        line2.setForeground(new Color(100, 100, 120));
        line2.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(icon);
        inner.add(Box.createRigidArea(new Dimension(0, 12)));
        inner.add(line1);
        inner.add(Box.createRigidArea(new Dimension(0, 6)));
        inner.add(line2);

        view.add(inner);
        return view;
    }

    // ── Top bar ────────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_DARK);
        bar.setPreferredSize(new Dimension(0, 40));
        bar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                new EmptyBorder(0, 16, 0, 10)
        ));

        JLabel settingsLabel = new JLabel("Session Settings");
        settingsLabel.setFont(Theme.FONT_NAV);
        settingsLabel.setForeground(Theme.TEXT_SECONDARY);
        bar.add(settingsLabel, BorderLayout.WEST);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 5));
        btnGroup.setOpaque(false);

        JButton disclaimer = buildActionButton("Disclaimer: the save buttons DON'T save the task list");
        JButton deleteButton = buildActionButton("Delete");
        JButton saveButton   = buildActionButton("Save");
        JButton saveAsButton = buildActionButton("Save As");
        JButton startButton = buildActionButton("Start");

        deleteButton.addActionListener(e -> { deleteCurrentSession(); });
        saveButton.addActionListener(e -> saveCurrentSession());
        saveAsButton.addActionListener(e -> saveCurrentSessionAsNew());
        startButton.addActionListener(e ->  startCurrentSession());

        btnGroup.add(disclaimer);
        btnGroup.add(deleteButton);
        btnGroup.add(saveButton);
        btnGroup.add(saveAsButton);
        btnGroup.add(startButton);
        bar.add(btnGroup, BorderLayout.EAST);

        return bar;
    }

    // ── Vertical tab strip ─────────────────────────────────────────────────────
    private JPanel buildVerticalTabs() {
        JPanel strip = new JPanel();
        strip.setLayout(new BoxLayout(strip, BoxLayout.Y_AXIS));
        strip.setBackground(Theme.BG_SIDEBAR);
        strip.setPreferredSize(new Dimension(150, 0));
        strip.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, Theme.BORDER_COLOR),
                new EmptyBorder(12, 0, 12, 0)
        ));

        for (int i = 0; i < TAB_LABELS.length; i++) {
            final int idx = i;
            JButton tab = buildVerticalTabButton(TAB_LABELS[i]);
            tab.addActionListener(e -> selectTab(idx));
            tabButtons[i] = tab;
            strip.add(tab);
            strip.add(Box.createRigidArea(new Dimension(0, 2)));
        }
        strip.add(Box.createVerticalGlue());
        return strip;
    }

    private JButton buildVerticalTabButton(String label) {
        JButton btn = new JButton(label) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(new Color(99, 102, 241, 50));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(Theme.ACCENT);
                    g2.fillRect(0, 0, 3, getHeight());
                } else if (hovered) {
                    g2.setColor(new Color(99, 102, 241, 25));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.FONT_NAV);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(0, 16, 0, 12));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void selectTab(int idx) {
        cardLayout.show(cardContainer, TAB_LABELS[idx]);
        for (int i = 0; i < tabButtons.length; i++) {
            boolean active = (i == idx);
            tabButtons[i].setSelected(active);
            tabButtons[i].setForeground(active ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
        }
    }

    // ── Card routing ───────────────────────────────────────────────────────────
    private JPanel buildTabCard(String tabName) {
        return switch (tabName) {
            case "Session Info"  -> sessionInfoCard;
            case "Notifications" -> notificationsCard;
            case "Tasks"         -> tasksCard;
            default -> {
                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setBackground(Theme.BG_CONTENT);
                JLabel lbl = new JLabel("<html><span style='color:#8c8ca0'>"
                        + tabName + " content renders here.</span></html>");
                lbl.setFont(Theme.FONT_BODY);
                lbl.setBorder(new EmptyBorder(20, 24, 20, 24));
                wrapper.add(lbl, BorderLayout.NORTH);
                yield wrapper;
            }
        };
    }

    // ── Session load/save ──────────────────────────────────────────────────────
    private void saveCurrentSession() {
        if (currentDraft == null) return;
        System.out.println("current Draft" + currentDraft.getId());
        sessionInfoCard.applyToDraft(currentDraft);
        notificationsCard.applyToDraft(currentDraft);
        tasksCard.applyToDraft(currentDraft);
        List<String> tasks = currentDraft.getTasks();
        onSessionSaved.saveSaveAsDeleteOp(ButtonOperations.SAVE, currentDraft);
        currentDraft.setTasks(tasks);
    }

    private void saveCurrentSessionAsNew() {
        if (currentDraft == null) return;
        sessionInfoCard.applyToDraft(currentDraft);
        notificationsCard.applyToDraft(currentDraft);
        tasksCard.applyToDraft(currentDraft);
        onSessionSaved.saveSaveAsDeleteOp(ButtonOperations.SAVE_AS, currentDraft);
    }

    private void deleteCurrentSession() {
        if (currentDraft == null) return;
        onSessionSaved.saveSaveAsDeleteOp(ButtonOperations.DELETE, currentDraft);
    }

    private void startCurrentSession() { //Sessionflashpoint is here
        //this might be causing a force reload
        saveCurrentSession();

        activePanel.setTasks(currentDraft.getTasks());
        currentDraft.setActive(true);

        int totalSeconds = currentDraft.getExpectedDuration();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        activePanel.setEstimatedTime(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        SessionManager.getInstance().startActiveSession(currentDraft);
        outerLayout.show(outerContainer, VIEW_ACTIVE_SESSION);
    }


    // ── Button helpers ─────────────────────────────────────────────────────────
    private JButton buildActionButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(Theme.FONT_MONO);
        btn.setForeground(Theme.TEXT_BLACK);
        btn.setBackground(new Color(99, 102, 241, 60));
        btn.setBorder(new CompoundBorder(
                new LineBorder(Theme.ACCENT, 1, true),
                new EmptyBorder(2, 10, 2, 10)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(99, 102, 241, 110)); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(new Color(99, 102, 241, 60));  }
        });
        return btn;
    }

    // ── Public API ─────────────────────────────────────────────────────────────
    public void loadSession(SessionDraft draft) { //so this is fucking up the draft changed system
        List<String> tasks = new ArrayList<>();
        if (currentDraft!=null) {
            tasks = currentDraft.getTasks();
        }
        currentSession = draft.getSessionName();
        currentDraft   = draft;
        sessionInfoCard.setDraft(currentDraft);
        notificationsCard.setDraft(currentDraft);
        tasksCard.setDraft(currentDraft);
        if (tasks != null && !tasks.isEmpty())
            currentDraft.setTasks(tasks);
        sessionInfoCard.loadFromDraft();
        notificationsCard.loadFromDraft();
        tasksCard.loadFromDraft();
        // Tasks intentionally not reset — persisted on the draft across selections
        outerLayout.show(outerContainer, VIEW_SESSION);
        draftChanged = false;

        activePanel.setSessionsPanel(onSessionSaved);
        selectTab(0);
    }

    public String getCurrentSession() { return currentSession; }

    public void clearTaskList() {
        tasksCard.clearTasks();
        currentDraft.getTasks().clear();
    }

    public void displayNoneSelectedPanel() {
        outerLayout.show(outerContainer, VIEW_PLACEHOLDER);
    }

    public void displayCurrentDraft()      {outerLayout.show(outerContainer, VIEW_SESSION);}

    public boolean isDraftChanged() {
        return draftChanged;
    }

    public void setDraftChanged(boolean value) {
        draftChanged = value;
    }

    public void setDraftActive(boolean value) { currentDraft.setActive(value); }

    public boolean getDraftActive() { return this.currentDraft.isActive(); }

    public void setSessionsPanel(SessionsPanel sessionsPanel) {
        this.onSessionSaved = sessionsPanel;
    }
}

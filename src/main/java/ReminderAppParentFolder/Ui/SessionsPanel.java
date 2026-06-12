package ReminderAppParentFolder.Ui;

import ReminderAppParentFolder.Notification.NotificationManager;
import ReminderAppParentFolder.Session.SessionDraft;
import ReminderAppParentFolder.Storage.StorageManager;
import ReminderAppParentFolder.Util.ButtonOperations;
import ReminderAppParentFolder.Util.SessionAction;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;


/**
 * Horizontal top bar: [+] [S1] [S2] [S3] [S4] [◄ ►]
 *
 * Creates a SessionDraft per session and passes it to onSessionSelected.
 */
public class SessionsPanel extends JPanel {

    private static final int BAR_HEIGHT  = 50;
    private static final int PAGE_SIZE   = 4;
    private static final int LEFT_OFFSET = 150;

    private final List<String>              sessions   = new ArrayList<>();
    private final List<JButton>             tabButtons = new ArrayList<>();
    private final JPanel                    tabRow;
    private final SessionContentPanel sessionContentPanel;

    private int selectedIndex = -1;
    private int pageStart     = 0;

    private final BiPredicate<String, SessionAction> onSessionSelected;
    private final BasicArrowButton prevBtn;
    private final BasicArrowButton nextBtn;

    private final StorageManager storageManager = StorageManager.getInstance();
    private final NotificationManager notificationManager =  NotificationManager.getInstance();

    public SessionsPanel(SessionContentPanel sessionContentPanel, BiPredicate<String, SessionAction> onSessionSelected) {
        this.onSessionSelected = onSessionSelected;
        this.sessionContentPanel = sessionContentPanel;

        System.out.println("is this triggered?");
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(Theme.BG_DARK);
        setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR));
        setPreferredSize(new Dimension(0, BAR_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, BAR_HEIGHT));

        // [+] button
        JButton addBtn = new JButton("+");
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        addBtn.setForeground(Theme.ACCENT);
        addBtn.setContentAreaFilled(false);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setPreferredSize(new Dimension(48, BAR_HEIGHT));
        addBtn.setMinimumSize(new Dimension(48, BAR_HEIGHT));
        addBtn.setMaximumSize(new Dimension(48, BAR_HEIGHT));
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addBtn.addActionListener(e -> { if (!sessionContentPanel.getDraftActive()) { addSession("New Session", true); sessionContentPanel.setDraftChanged(true);}});

        // Left offset spacer
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(LEFT_OFFSET, BAR_HEIGHT));
        spacer.setMinimumSize(new Dimension(LEFT_OFFSET, BAR_HEIGHT));
        spacer.setMaximumSize(new Dimension(LEFT_OFFSET, BAR_HEIGHT));

        // Tab row
        int btnW    = 200;
        int btnH    = 34;
        int hgap    = 6;
        int tabRowW = PAGE_SIZE * btnW + (PAGE_SIZE + 1) * hgap;
        int vgap    = (BAR_HEIGHT - btnH) / 2;

        tabRow = new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, vgap));
        tabRow.setBackground(Theme.BG_DARK);
        tabRow.setOpaque(true);
        tabRow.setPreferredSize(new Dimension(tabRowW, BAR_HEIGHT));

        // Arrow buttons
        int arrowH = (int)(BAR_HEIGHT * 0.80);
        int arrowW = arrowH;

        prevBtn = new BasicArrowButton(SwingConstants.WEST,
                Theme.BG_DARK, Theme.BG_CONTENT, Theme.TEXT_SECONDARY, Theme.BG_SIDEBAR);
        nextBtn = new BasicArrowButton(SwingConstants.EAST,
                Theme.BG_DARK, Theme.BG_CONTENT, Theme.TEXT_SECONDARY, Theme.BG_SIDEBAR);

        for (BasicArrowButton ab : new BasicArrowButton[]{ prevBtn, nextBtn }) {
            ab.setPreferredSize(new Dimension(arrowW, arrowH));
            ab.setMinimumSize(new Dimension(arrowW, arrowH));
            ab.setMaximumSize(new Dimension(arrowW, arrowH));
            ab.setBorder(new MatteBorder(1, 1, 1, 1, Theme.BORDER_COLOR));
            ab.setFocusPainted(false);
            ab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        prevBtn.addActionListener(e -> shiftPage(-PAGE_SIZE, true));
        nextBtn.addActionListener(e -> shiftPage(+PAGE_SIZE, true));

        int arrowGroupW = arrowW * 2 + 10 + 8;
        JPanel arrowGroup = new JPanel(new GridLayout(1, 2, 6, 0));
        arrowGroup.setOpaque(false);
        arrowGroup.setBorder(new EmptyBorder(
                (BAR_HEIGHT - arrowH) / 2, 4,
                (BAR_HEIGHT - arrowH) / 2, 8
        ));
        arrowGroup.setPreferredSize(new Dimension(arrowGroupW, BAR_HEIGHT));
        arrowGroup.setMinimumSize(new Dimension(arrowGroupW, BAR_HEIGHT));
        arrowGroup.setMaximumSize(new Dimension(arrowGroupW, BAR_HEIGHT));
        arrowGroup.add(prevBtn);
        arrowGroup.add(nextBtn);

        add(spacer);
        add(addBtn);
        add(Box.createHorizontalStrut(8));
        add(arrowGroup);
        add(tabRow);
        refreshPage(false);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public void addSession(String name, boolean selectIndex) {
        if (sessionContentPanel.isDraftChanged()) {
            String[] options = { "Yes", "No" };
            int result = NotificationManager.getInstance().defaultPopupNotification(
                    "UNSAVED EDITS DETECTED!",
                    "Are you sure you want to change session?",
                    options);
            System.out.println(result);
            if (result == 1 || result == -1) return;
            else {//remove the current session
                removeLastSession();
                sessionContentPanel.setDraftChanged(false);
            }
        }


        final int idx = sessions.size();

        sessions.add(name);

        JButton btn = new JButton(name) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (!sessionContentPanel.getDraftActive() && selectedIndex != idx)
                            selectIndex(idx, SessionAction.SELECT);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );
                boolean active = (idx == selectedIndex); //this is where selection happens?
                Color bg = active ? new Color(99, 102, 241, 80) : hovered
                                ? new Color(99, 102, 241, 35)
                                : new Color(40, 40, 55, 180);
                g2.setColor(bg);
                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        6,
                        6
                );

                if (active) {
                    g2.setColor(Theme.ACCENT);
                    g2.fillRect(
                            0,
                            getHeight() - 2,
                            getWidth(),
                            2
                    );
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(Theme.FONT_MONO);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        // ── Dynamic Width ─────────────────────────────────────────────

        FontMetrics fm =
                btn.getFontMetrics(Theme.FONT_MONO);
        int textWidth =
                fm.stringWidth(name);
        int horizontalPadding = 28;
        btn.setPreferredSize(
                new Dimension(Math.max(100,Math.min(textWidth + horizontalPadding, 220)),
                        34
                )
        );

        // ── Store Button ─────────────────────────────────────────────

        tabButtons.add(btn);

        if (idx >= pageStart + PAGE_SIZE) {
            pageStart = (idx / PAGE_SIZE) * PAGE_SIZE;
        }

        refreshPage(false);
        if (selectIndex) {
            selectIndex(idx, SessionAction.NEW_SESSION);
            sessionContentPanel.setDraftChanged(true);
        }
        else
            selectIndexSilently(idx);  // don't fire onSessionSelected for new tabs
    }

    // Selects visually without firing the callback
    protected void selectIndexSilently(int idx) {
        selectedIndex = idx;
        tabRow.repaint();
    }

    public void saveSaveAsDeleteOp(ButtonOperations operation, SessionDraft currentDraft) { //OVER HERE!
        //add the save, saveAs, and Delete operation logic here.

        switch(operation) {
            case SAVE:
                //Actual save operation happens, AND THEN the rest of Action should be passed
                if (storageManager.sessions().save(currentDraft) == 0) {
                    sessionContentPanel.setDraftChanged(false);
                }
                onSessionSelected.test("",SessionAction.SAVE);
                break;
            case SAVE_AS:
                if (storageManager.sessions().saveAs(currentDraft) == 0) {
                    sessionContentPanel.setDraftChanged(false);
                }
                onSessionSelected.test("",SessionAction.SAVE_AS);
                break;
            case DELETE:
                String[] options = {"Yes", "No"};
                int result = notificationManager.defaultPopupNotification(
                        "DELETE BUTTON SELECTED!",
                        "Are you ABSOLUTELY certain you wish to delete this session?",
                        options);
                if (storageManager.sessions().delete(currentDraft.getSessionName()) == 0)
                    sessionContentPanel.setDraftChanged(false);
                onSessionSelected.test("", SessionAction.DISCARD);
        }
    }

    public boolean reloadSessions(boolean initialize) { //this gets called during the save
        tabRow.removeAll();
        tabButtons.clear();
        sessions.clear();

        List<String> sessionNames = StorageManager.getInstance().sessions().loadAllNames();
        for (String name : sessionNames) {
            addSession(name, false);  // now purely visual, no load triggered
        }
        pageStart = 0;
        //shiftPage(0);
        if (initialize && !sessions.isEmpty()) {
            shiftPage(0, false);
            selectedIndex = -1;
        }

        return true;
    }
    public void selectUUIDMatch(String useUUID) {
        if (useUUID != null) {
//            System.out.println(locateUUIDMatch(useUUID));
            int UUIDPos = locateUUIDMatch(useUUID);
            selectedIndex = UUIDPos;
            pageStart = 0;
            refreshPage(false);
            System.out.print("So this is triggered?");
            selectIndex(UUIDPos, SessionAction.SELECT);
        }
    }


    // ── Private ────────────────────────────────────────────────────────────────

    private void shiftPage(int delta, boolean blink) {
        int maxStart = Math.max(0, (sessions.size() / PAGE_SIZE) * PAGE_SIZE);
        pageStart = Math.max(0, Math.min(pageStart + delta, maxStart));
        refreshPage(blink);
    }

    private void refreshPage(boolean blink) {
        tabRow.removeAll();
        int end = Math.min(pageStart + PAGE_SIZE, sessions.size());
        for (int i = pageStart; i < end; i++) {
            tabRow.add(tabButtons.get(i));
        }

        if (blink) {
            tabRow.setVisible(false);
            Timer timer = new Timer(50, e -> {
                tabRow.setVisible(true);
                tabRow.revalidate();
                tabRow.repaint();
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            tabRow.revalidate();
            tabRow.repaint();
        }

        prevBtn.setEnabled(pageStart > 0);
        nextBtn.setEnabled(pageStart + PAGE_SIZE < sessions.size());
    }

    protected void selectIndex(int idx, SessionAction action) {
        int previous = selectedIndex;
        selectedIndex = idx;
        tabRow.repaint();
        if (!onSessionSelected.test(sessions.get(idx), action)) {
            selectedIndex = previous;
            tabRow.repaint();
        }
    }

    protected int locateUUIDMatch(String UUID) {
        if  (UUID == null) {
            return -1;
        }
        System.out.println(UUID);
        String sm = StorageManager.getInstance().sessions().findUUIDSessionName(UUID);
        System.out.println(sm);
        return sessions.indexOf(sm);

    }

    protected void removeLastSession() {
        sessions.removeLast();
        tabButtons.removeLast();
        refreshPage(false);
    }

    public void selectNone() {
        selectedIndex = -1;
        refreshPage(false);
    }
}
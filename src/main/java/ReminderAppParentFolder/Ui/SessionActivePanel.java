package ReminderAppParentFolder.Ui;

import ReminderAppParentFolder.tracking.SessionManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SessionActivePanel extends JPanel {

    // ── Colours ────────────────────────────────────────────────────────────────
    private static final Color STOP_COLOR    = new Color(0x2a7a2a);
    private static final Color CANCEL_COLOR  = new Color(0xaa6600);
    private static final Color DISCARD_COLOR = new Color(0xaa2222);

    private static final int   ROW_ARC     = 12;
    private static final Color ROW_BG      = new Color(0x2a2a3e);
    private static final Color ROW_BG_DONE = new Color(0x22222e);

    private long nextPriority = 1;
    private int unCheckedCount = 0;

    private String sessionStopMethod;

    // ── Task model ─────────────────────────────────────────────────────────────
    public static class Task {
        public String text;
        public boolean done;

        public long sortPriority;

        public Task(String text) {
            this.text = text;
            this.done = false;
            this.sortPriority = 0;
        }
    }

    // ── State ──────────────────────────────────────────────────────────────────
    private final List<Task> tasks = new ArrayList<>();
    private JScrollPane scroll;

    // ── UI references ──────────────────────────────────────────────────────────
    private JPanel  listContainer;
    private JLabel  estimatedTimeLabel;
    private JLabel  elapsedTimeLabel;
    private JButton sortBtn;
    private SessionContentPanel contentPanel;
    private SessionsPanel sessionsPanel;
    private TaskOverlayWidget floatingWidget;
    private int currentWidgetIndex = 0;

    // NOTE: editBtn is NOT a field — it must be local to buildRow so each row
    //       has its own independent button reference. A shared field caused every
    //       row to overwrite the same reference, breaking refreshRow for all but
    //       the last row.

    public SessionActivePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_CONTENT);

        add(buildLeftPanel(),  BorderLayout.CENTER);
        add(buildRightPanel(), BorderLayout.EAST);
        refreshList();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LEFT — task list
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(0, 0));
        left.setBackground(Theme.BG_CONTENT);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        topBar.setBackground(Theme.BG_DARK);
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR));

        sortBtn = topBarButton("Sort: Checked to last");
        sortBtn.addActionListener(e -> sortTasks());
        topBar.add(sortBtn);
        left.add(topBar, BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Theme.BG_CONTENT);
        listContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.setBackground(Theme.BG_CONTENT);
        listWrapper.add(listContainer, BorderLayout.NORTH);

        scroll = new JScrollPane(listWrapper);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(4);
        scroll.getViewport().setBackground(Theme.BG_CARD);
        scroll.getVerticalScrollBar().setPreferredSize(
                new Dimension(12, 0)
        );
        left.add(scroll, BorderLayout.CENTER);

        return left;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RIGHT — session controls
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Theme.BG_DARK);
        right.setBorder(new MatteBorder(0, 1, 0, 0, Theme.BORDER_COLOR));
        right.setPreferredSize(new Dimension(260, 0));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(24, 20, 24, 20));

        inner.add(buildTimeBlock("Estimated time", true));
        inner.add(Box.createRigidArea(new Dimension(0, 14)));
        inner.add(buildTimeBlock("Elapsed time", false));
        inner.add(Box.createRigidArea(new Dimension(0, 32)));

        JButton finishBtn = buildActionButton("Finish Session",    STOP_COLOR);
        inner.add(finishBtn);
        inner.add(Box.createRigidArea(new Dimension(0, 10)));
        finishBtn.addActionListener(e -> {
            sessionStopMethod = "Finish";
            elapsedTimeLabel.setText("00:00:00");
            SessionManager.getInstance().stopActiveSession();

            elapsedTimeLabel.setText("00:00:00");
            contentPanel.setDraftActive(false);
            contentPanel.clearTaskList();
            sessionsPanel.selectNone();
            contentPanel.displayNoneSelectedPanel();
        });

        JButton cancelBtn = buildActionButton("Cancel Session",  CANCEL_COLOR);
        inner.add(cancelBtn);
        inner.add(Box.createRigidArea(new Dimension(0, 10)));
        cancelBtn.addActionListener(e -> {
            sessionStopMethod = "Cancel";
            elapsedTimeLabel.setText("00:00:00");

            SessionManager.getInstance().stopActiveSession();

            contentPanel.setDraftActive(false);
            contentPanel.displayCurrentDraft();
        });

        JButton discardBtn = buildActionButton("Discard Session",   DISCARD_COLOR);
        inner.add(discardBtn);
        discardBtn.addActionListener(e -> {
            sessionStopMethod = "Discard";
            elapsedTimeLabel.setText("00:00:00");
            SessionManager.getInstance().cancelActiveSession();


            contentPanel.setDraftActive(false);
            contentPanel.clearTaskList();
            contentPanel.displayCurrentDraft();
        });

        right.add(inner, BorderLayout.NORTH);
        return right;
    }

    private JPanel buildTimeBlock(String labelText, boolean isEstimated) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.FONT_MONO);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel display = new JLabel("00:00:00");
        display.setFont(new Font("Monospaced", Font.BOLD, 20));
        display.setForeground(Theme.TEXT_PRIMARY);
        display.setOpaque(true);
        display.setBackground(Theme.BG_FIELD);
        display.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, Theme.BORDER_COLOR),
                new EmptyBorder(6, 12, 6, 12)
        ));
        display.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (isEstimated) estimatedTimeLabel = display;
        else             elapsedTimeLabel   = display;

        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(lbl);
        block.add(Box.createRigidArea(new Dimension(0, 4)));
        block.add(display);
        return block;
    }

    private JButton buildActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, bg.darker()),
                new EmptyBorder(6, 14, 6, 14)
        ));
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Sort
    // ══════════════════════════════════════════════════════════════════════════
    public void sortTasks() {
        tasks.sort(
                Comparator.comparing((Task t) -> t.done)
                        .thenComparingLong(t -> -t.sortPriority)
        );
        if (floatingWidget != null) {
            currentWidgetIndex = 0;
            updateFloatingWidgetView();
        }
        refreshList();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Task management
    // ══════════════════════════════════════════════════════════════════════════
    public void addTask(Task task) {
        tasks.add(task);
        refreshList();
        // Force a rapid validation pass to ensure component layout dimensions exist
        SwingUtilities.invokeLater(() -> {
            listContainer.revalidate();
            listContainer.repaint();
        });
    }

    private void refreshList() {
        listContainer.removeAll();
        for (Task t : tasks)
            listContainer.add(buildRow(t));
        listContainer.revalidate();
        listContainer.repaint();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Row builder
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildRow(Task task) {

        JPanel row = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(task.done ? ROW_BG_DONE : ROW_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ROW_ARC, ROW_ARC);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 10, 8, 10));

        // ── Checkbox ───────────────────────────────────────────────────────────
        JCheckBox check = new JCheckBox();
        check.setSelected(task.done);
        check.setOpaque(false);
        check.setFocusPainted(false);

        // ── Label ──────────────────────────────────────────────────────────────
        JTextArea label = new JTextArea(task.text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(task.done ? Theme.TEXT_SECONDARY : Theme.TEXT_PRIMARY);
        label.setOpaque(false);
        label.setEditable(false);
        label.setFocusable(false);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setBorder(new EmptyBorder(0, 6, 0, 6));
        label.setMargin(new Insets(0, 0, 0, 0));
        label.setCaretColor(Color.LIGHT_GRAY);

        // ── Edit button — LOCAL variable, one per row ──────────────────────────
        JButton editBtn = new JButton("Edit");
        editBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        editBtn.setForeground(Theme.TEXT_SECONDARY);
        editBtn.setBackground(Theme.BG_FIELD);
        editBtn.setFocusPainted(false);
        editBtn.setContentAreaFilled(false);
        editBtn.setOpaque(true);
        editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editBtn.setPreferredSize(new Dimension(54, 26));
        editBtn.setMinimumSize(new Dimension(54, 26));
        editBtn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, Theme.BORDER_COLOR),
                new EmptyBorder(2, 6, 2, 6)
        ));

        // ── DocumentListener (editBtn is in scope here) ────────────────────────
        label.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { refreshRow(label, row, editBtn); }
            public void removeUpdate(DocumentEvent e)  { refreshRow(label, row, editBtn); }
            public void changedUpdate(DocumentEvent e) { refreshRow(label, row, editBtn); }
        });

        // ── Checkbox action ────────────────────────────────────────────────────
        check.addActionListener(e -> {
            boolean wasChecked = task.done;
            task.done = check.isSelected();
            updateFloatingWidgetView();
            if (check.isSelected()) {
                unCheckedCount--;
            } else
                unCheckedCount++;

            if (wasChecked && !task.done) {
                task.sortPriority = nextPriority++;
            }

            refreshList();
        });

        // ── Edit button action ─────────────────────────────────────────────────

        editBtn.addActionListener(e -> {
            boolean editing = label.isEditable();
            if (editing) {
                task.text = label.getText().trim();
                label.setText(task.text);
                label.setEditable(false);
                label.setFocusable(false);
                label.setOpaque(false); // Make transparent again when done
                label.setCaretPosition(0);
                editBtn.setText("Edit");
                refreshRow(label, row, editBtn);
            } else {
                label.setEditable(true);
                label.setFocusable(true);
                label.setOpaque(true); // Make opaque to show editing region
                label.setBackground(Theme.BG_FIELD); // Use your theme field color
                label.requestFocusInWindow();
                label.selectAll();
                editBtn.setText("Done");
            }
            row.repaint();
        });

        // ── Layout ────────────────────────────────────────────────────────────
        JPanel checkWrap = new JPanel(new BorderLayout());
        checkWrap.setOpaque(false);
        checkWrap.add(check, BorderLayout.NORTH);

        JPanel editWrap = new JPanel(new BorderLayout());
        editWrap.setOpaque(false);
        editWrap.setPreferredSize(new Dimension(54, 1));
        editWrap.setMinimumSize(new Dimension(54, 1));
        editWrap.add(editBtn, BorderLayout.NORTH);

        row.add(checkWrap, BorderLayout.WEST);
        row.add(label,     BorderLayout.CENTER);
        row.add(editWrap,  BorderLayout.EAST);

        // ── Resize listener ────────────────────────────────────────────────────
        row.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                refreshRow(label, row, editBtn);
            }
        });

        JPanel rowWrapper = new JPanel(new BorderLayout());
        rowWrapper.setOpaque(false);
        rowWrapper.setBorder(new EmptyBorder(0, 0, 6, 0));
        rowWrapper.add(row, BorderLayout.CENTER);

        return rowWrapper;
    }

    // ── Live height recalc ─────────────────────────────────────────────────────
// ── Live height recalc ─────────────────────────────────────────────────────
    private void refreshRow(JTextArea label, JPanel row, JButton editBtn) {
        if (label.getWidth() <= 0) return;

        // 1. Reset sizes to clear Swing's internal layout caches
        label.setPreferredSize(null);

        // 2. Query the underlying Document UI View framework for the accurate wrapped layout height
        int textWidth = label.getWidth();
        label.setSize(new Dimension(textWidth, Integer.MAX_VALUE));
        int textH = (int) Math.ceil(
                label.getUI()
                        .getRootView(label)
                        .getPreferredSpan(javax.swing.text.View.Y_AXIS)
        );

        // 3. Set the calculated height explicitly
        label.setPreferredSize(new Dimension(textWidth, textH));

        int minH  = editBtn.getPreferredSize().height;
        Insets ins = row.getInsets();
        int rowH  = Math.max(textH, minH) + ins.top + ins.bottom;

        // 4. Force updates up to the JScrollPane viewport layer
        row.setPreferredSize(new Dimension(row.getWidth(), rowH));

        // Revalidate components from the row level all the way to the list container
        row.revalidate();
        listContainer.revalidate();

        // Repaint to clean up any visual artifacts from moving rows
        row.repaint();
        listContainer.repaint();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Public API
    // ══════════════════════════════════════════════════════════════════════════
    public void setEstimatedTime(String hms) { estimatedTimeLabel.setText(hms); }

    public void setTasks(List<String> taskList) {
        for (String sTask : taskList) {
            System.out.println(sTask);
            addTask(new Task(sTask));
        }
        sortTasks();
        unCheckedCount = taskList.size();
        SwingUtilities.invokeLater(() ->
                scroll.getVerticalScrollBar().setValue(0)
        );
    }

    public int getUnFinishedCount() {
        return unCheckedCount;
    }

    public String sessionStopMethod() {
        return sessionStopMethod;
    }

    public void updateElapsedTimeDisplay(String formattedTime) {
        elapsedTimeLabel.setText(formattedTime);
    }

    public void activateTaskOverlay() {
        floatingWidget = new TaskOverlayWidget();

        // Wire up listeners to manipulate the active data tracking array here!
        floatingWidget.setControllerCallbacks(
                () -> navigateWidgetIndex(-1), // On Prev
                () -> navigateWidgetIndex(1),  // On Next
                (isChecked) -> handleTaskCheckToggle(isChecked) // On Checkbox Clicked
        );

        updateFloatingWidgetView();
        floatingWidget.setVisible(true);
    }

    public void deactivateTaskOverlay() {
        floatingWidget.setVisible(false);
    }

    private void navigateWidgetIndex(int shift) {
        if (tasks.isEmpty()) return;

        currentWidgetIndex += shift;
        if (currentWidgetIndex < 0) currentWidgetIndex = tasks.size() - 1;
        if (currentWidgetIndex >= tasks.size()) currentWidgetIndex = 0;

        updateFloatingWidgetView();
    }

    private void handleTaskCheckToggle(boolean isChecked) {
        Task targetedTask = tasks.get(currentWidgetIndex);
        System.out.println("Boss component update! Toggled status of: " + targetedTask + " to " + isChecked);

        targetedTask.done = isChecked;
        refreshList();
        // Process your sorting updates, strike-throughs, or DB calls inside the panel here...
    }

    public void updateFloatingWidgetView() {
        if (floatingWidget == null) return;

        if (tasks == null || tasks.isEmpty()) {
            floatingWidget.updateTaskDisplay("No Tasks Found!", false);
        } else {
            Task activeTaskText = tasks.get(currentWidgetIndex);
            // Pass text along with status checked directly out of your master panel track
            floatingWidget.updateTaskDisplay(activeTaskText.text, activeTaskText.done);
        }
    }

    public void setContentPanel(SessionContentPanel contentPanel) {
        this.contentPanel = contentPanel;
    }

    public void setSessionsPanel(SessionsPanel sessionsPanel) {
        this.sessionsPanel = sessionsPanel;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private JButton topBarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setBackground(Theme.BG_FIELD);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, Theme.BORDER_COLOR),
                new EmptyBorder(4, 10, 4, 10)
        ));
        return btn;
    }
}
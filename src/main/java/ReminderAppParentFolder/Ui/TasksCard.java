package ReminderAppParentFolder.Ui;

import ReminderAppParentFolder.Session.SessionDraft;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class TasksCard extends JPanel {

    private final JTextField taskInput;
    private final JPanel taskList;
    private final JCheckBox overlayUsage;
    private final List<String> taskListStorage = new ArrayList<>();

    private SessionDraft draft;

    // We keep track of the structural vertical glue at the bottom
    private Component verticalGlue;

    public TasksCard(SessionContentPanel sessionContentPanel) {

        setLayout(new BorderLayout());
        setBackground(Theme.BG_CONTENT);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.BG_CARD);

        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // --------------
        // Task overlay selection
        // --------------

        overlayUsage = new JCheckBox("Use task overlay");
        overlayUsage.setFont(Theme.FONT_BODY);
        overlayUsage.setForeground(Theme.TEXT_PRIMARY);
        overlayUsage.setOpaque(false);
        overlayUsage.setFocusPainted(false);
        overlayUsage.setAlignmentX(Component.LEFT_ALIGNMENT);
        overlayUsage.setSelected(false);
        overlayUsage.setFocusable(false);
        overlayUsage.addActionListener(e -> sessionContentPanel.setDraftChanged(true));

        // ─────────────────────────────────────────
        // Input Row
        // ─────────────────────────────────────────

        JPanel inputRow = new JPanel();
        inputRow.setLayout(new BoxLayout(inputRow, BoxLayout.X_AXIS));
        inputRow.setOpaque(false);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        taskInput = new JTextField();
        taskInput.setFont(Theme.FONT_BODY);
        taskInput.setForeground(Theme.TEXT_PRIMARY);
        taskInput.setBackground(new Color(40, 40, 55));
        taskInput.setCaretColor(Theme.TEXT_PRIMARY);
        taskInput.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(2, 8, 2, 8)
        ));
        taskInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        taskInput.addActionListener(e -> addTask());

        JButton addBtn = new JButton("Add");
        addBtn.setFont(Theme.FONT_MONO);
        addBtn.setForeground(Theme.TEXT_BLACK);
        addBtn.setBackground(new Color(99, 102, 241, 60));
        addBtn.setBorder(new CompoundBorder(
                new LineBorder(Theme.ACCENT, 1, true),
                new EmptyBorder(2, 10, 2, 10)
        ));
        addBtn.setFocusPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> addTask());
        addBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                addBtn.setBackground(new Color(99, 102, 241, 110));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                addBtn.setBackground(new Color(99, 102, 241, 60));
            }
        });

        inputRow.add(overlayUsage);
        inputRow.add(Box.createVerticalStrut(4));
        inputRow.add(taskInput);
        inputRow.add(Box.createHorizontalStrut(8));
        inputRow.add(addBtn);

        // ─────────────────────────────────────────
        // Divider
        // ─────────────────────────────────────────

        JSeparator divider = new JSeparator();
        divider.setForeground(Theme.BORDER_COLOR);

        // ─────────────────────────────────────────
        // Task List
        // ─────────────────────────────────────────

        taskList = new JPanel();
        taskList.setLayout(new BoxLayout(taskList, BoxLayout.Y_AXIS));
        taskList.setBackground(Theme.BG_CARD);

        // Initialize our persistent glue component
        verticalGlue = Box.createVerticalGlue();
        taskList.add(verticalGlue);

        JScrollPane scroll = new JScrollPane(taskList);
        scroll.setBorder(null);
        scroll.setBackground(Theme.BG_CARD);
        scroll.getViewport().setBackground(Theme.BG_CARD);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // ─────────────────────────────────────────
        // Assemble
        // ─────────────────────────────────────────

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        top.add(inputRow);
        top.add(Box.createRigidArea(new Dimension(0, 10)));
        top.add(divider);
        top.add(Box.createRigidArea(new Dimension(0, 6)));

        card.add(top, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    // ----------
    // public API
    // ----------

    public void setDraft(SessionDraft draft) { this.draft = draft; }

    public void loadFromDraft() {
        //Is this where the
        overlayUsage.setSelected(draft.getTaskOverlayUsage());
        taskList.removeAll();
        taskListStorage.clear();

        for (String task : draft.getTasks()) {
            TaskItem item = new TaskItem(task, this::removeTask);
            // Injecting a clean margin directly to the component layout loop
            item.setBorder(new EmptyBorder(4, 0, 4, 0));

            taskList.add(item);
            taskListStorage.add(task);
        }

        // Always make sure vertical glue is refreshed at the very bottom
        verticalGlue = Box.createVerticalGlue();
        taskList.add(verticalGlue);

        taskList.revalidate();
        taskList.repaint();
    }

    public void applyToDraft(SessionDraft draft) {
        draft.setTaskOverlay(overlayUsage.isSelected());
        draft.setTasks(taskListStorage);
    }

    public void clearTasks() {
        taskList.removeAll();
        taskListStorage.clear();

        verticalGlue = Box.createVerticalGlue();
        taskList.add(verticalGlue);

        taskList.revalidate();
        taskList.repaint();

        draft.setTasks(new ArrayList<>());
    }

    // ─────────────────────────────────────────
    // Task Logic
    // ─────────────────────────────────────────

    private void addTask() {
        String text = taskInput.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        TaskItem item = new TaskItem(text, this::removeTask);
        item.setBorder(new EmptyBorder(4, 0, 4, 0));

        taskListStorage.add(text);

        // Remove the glue, add the item, push glue back down to the bottom
        taskList.remove(verticalGlue);
        taskList.add(item);
        taskList.add(verticalGlue);

        taskList.revalidate();
        taskList.repaint();

        taskInput.setText("");
        taskInput.requestFocusInWindow();
        this.draft.setTasks(taskListStorage);
    }

    private void removeTask(TaskItem item) {
        // Because everything is 1:1 mapped now (no extra separator subcomponents),
        // the internal layout Z-Order directly maps to our list indices!
        int idx = taskList.getComponentZOrder(item);

        if (idx != -1) {
            taskList.remove(item);
            taskListStorage.remove(idx);

            // Sync with your backend storage object safe limits
            if (idx < draft.getTasks().size()) {
                draft.removeTask(idx);
            }

            taskList.revalidate();
            taskList.repaint();
        }
    }
}
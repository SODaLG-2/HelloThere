package ReminderAppParentFolder.Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class TaskOverlayWidget extends JFrame {

    // ✅ SWAPPED: Changed from JLabel to JTextArea for native wrap support
    private final JTextArea taskDisplayArea;
    private final JCheckBox completionCheckbox;

    private Point mouseClickOffset = null;

    private Runnable onNextRequested;
    private Runnable onPrevRequested;
    private java.util.function.Consumer<Boolean> onCheckToggled;

    public TaskOverlayWidget() {
        setUndecorated(true);
        setAlwaysOnTop(true);
        setFocusableWindowState(true);
        setType(Window.Type.UTILITY);

        setSize(320, 380);
        setBackground(new Color(25, 25, 25, 195));
        setLayout(new BorderLayout(0, 0));
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90, 120), 1));

        // ─────────────────────────────────────────────────────────────
        // 1. Upgraded JTextArea Setup (Handles extreme text gracefully)
        // ─────────────────────────────────────────────────────────────
        taskDisplayArea = new JTextArea("No Active Task Selected");
        taskDisplayArea.setFont(new Font("SansSerif", Font.PLAIN, 18));
        taskDisplayArea.setForeground(Color.WHITE);
        taskDisplayArea.setOpaque(false);          // Makes it transparent to show frame color
        taskDisplayArea.setEditable(false);        // Stops the user from typing over it
        taskDisplayArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));


        // 🎯 THE CRITICAL SETTINGS: Native word splitting
        taskDisplayArea.setLineWrap(true);       // Wraps sentences to the next line
        taskDisplayArea.setWrapStyleWord(false);   // FALSE tells Java: "Break apart single long words mid-character if needed!"

        // Wrap it in a padding layout so the text doesn't slam against the edges
        JPanel textPaddingPanel = new JPanel(new BorderLayout());
        textPaddingPanel.setOpaque(false);
        textPaddingPanel.setBorder(BorderFactory.createEmptyBorder(45, 25, 25, 25));
        textPaddingPanel.add(taskDisplayArea, BorderLayout.CENTER);
        add(textPaddingPanel, BorderLayout.CENTER);

        // ─────────────────────────────────────────────────────────────
        // WIRE UP DRAG MECHANICS ON THE NEW TEXT AREA PANEL
        // ─────────────────────────────────────────────────────────────
        MouseAdapter dragListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseClickOffset = e.getPoint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                mouseClickOffset = null;
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseClickOffset != null) {
                    Point currentScreenPos = e.getLocationOnScreen();
                    setLocation(currentScreenPos.x - mouseClickOffset.x, currentScreenPos.y - mouseClickOffset.y);
                }
            }
        };
        // Adding it to both ensures you can drag by clicking the text OR the blank space
        taskDisplayArea.addMouseListener(dragListener);
        taskDisplayArea.addMouseMotionListener(dragListener);
        textPaddingPanel.addMouseListener(dragListener);
        textPaddingPanel.addMouseMotionListener(dragListener);

        // ─────────────────────────────────────────────────────────────
        // 2. Control Dock (Bottom Row remains exactly the same)
        // ─────────────────────────────────────────────────────────────
        JPanel controlContainer = new JPanel(new GridLayout(1, 3, 10, 0));
        controlContainer.setOpaque(false);
        controlContainer.setBorder(BorderFactory.createEmptyBorder(0, 15, 20, 15));

        JButton prevButton = createStyledButton("◀  Prev");
        prevButton.setForeground(Color.BLACK);

        JPanel checkWrapper = new JPanel(new GridBagLayout());
        checkWrapper.setOpaque(false);
        completionCheckbox = new JCheckBox("Done");
        completionCheckbox.setForeground(Color.WHITE);
        completionCheckbox.setFont(new Font("SansSerif", Font.BOLD, 13));
        completionCheckbox.setOpaque(false);
        checkWrapper.add(completionCheckbox);

        JButton nextButton = createStyledButton("Next  ▶");
        nextButton.setForeground(Color.BLACK);

        controlContainer.add(prevButton);
        controlContainer.add(checkWrapper);
        controlContainer.add(nextButton);
        add(controlContainer, BorderLayout.SOUTH);

        resetPositionToTopRight();

        prevButton.addActionListener(e -> { if (onPrevRequested != null) onPrevRequested.run(); });
        nextButton.addActionListener(e -> { if (onNextRequested != null) onNextRequested.run(); });

        completionCheckbox.addActionListener(e -> {
            if (onCheckToggled != null) {
                onCheckToggled.accept(completionCheckbox.isSelected());
            }
        });
    }

    // ✅ CLEAN REFLECTION UPDATE: No weird HTML parsing strings required (Keeping this note, because this will probably fuck up down the line)
    public void updateTaskDisplay(String taskText, boolean isCompleted) {
        taskDisplayArea.setText(taskText != null ? taskText : "");
        completionCheckbox.setSelected(isCompleted);
    }

    public void setControllerCallbacks(Runnable onPrev, Runnable onNext, java.util.function.Consumer<Boolean> onCheck) {
        this.onPrevRequested = onPrev;
        this.onNextRequested = onNext;
        this.onCheckToggled = onCheck;
    }

    public void resetPositionToTopRight() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width - getWidth() - 40, 40);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 60, 60, 220));
        btn.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

}
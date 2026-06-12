package ReminderAppParentFolder.Ui;

import ReminderAppParentFolder.Notification.NotificationResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Full-screen overlay notification window.
 *
 * <p>Fully customizable via {@link Config}. Construct a config, pass it to
 * {@link #showNotification}, and the overlay adapts its button bar, message,
 * and layout accordingly. Call {@link #forceClose()} from any thread, any
 * package, to tear it down instantly.
 *
 * <pre>{@code
 * // Minimal — message only, no buttons
 * OverlayNotification.getInstance().showNotification(
 *     new OverlayNotification.Config("Take a break.")
 *         .withNoButtons(),
 *     result -> {}
 * );
 *
 * // Full custom button set
 * OverlayNotification.getInstance().showNotification(
 *     new OverlayNotification.Config("Stand up and stretch.")
 *         .withButtons(
 *             new OverlayNotification.NotificationButton("Done",      NotificationResult.DISMISS),
 *             new OverlayNotification.NotificationButton("5 min",     NotificationResult.SNOOZE),
 *             new OverlayNotification.NotificationButton("Skip",      NotificationResult.SKIP)
 *         ),
 *     result -> handleResult(result)
 * );
 * }</pre>
 */
public class OverlayNotification extends JFrame {

    // ─────────────────────────────────────────────────────────────
    // PUBLIC CONFIGURATION API
    // ─────────────────────────────────────────────────────────────

    /**
     * Maps a button label to the {@link NotificationResult} it delivers to
     * the callback when clicked.
     */
    public record NotificationButton(String label, NotificationResult result) {}

    /**
     * Immutable configuration object for a single notification display.
     * Use the {@code with*()} builder methods to customise.
     */
    public static final class Config {

        private final String message;
        private final List<NotificationButton> buttons;
        private final boolean showButtonBar;

        /** Defaults: message only, standard Dismiss + Snooze button bar visible. */
        public Config(String message) {
            this.message = message;
            this.buttons = List.of(
                    new NotificationButton("Dismiss", NotificationResult.DISMISS),
                    new NotificationButton("Snooze",  NotificationResult.SNOOZE)
            );
            this.showButtonBar = true;
        }

        private Config(String message, List<NotificationButton> buttons, boolean showButtonBar) {
            this.message      = message;
            this.buttons      = buttons;
            this.showButtonBar = showButtonBar;
        }

        /** Replace the button set with any number of custom buttons. */
        public Config withButtons(NotificationButton... buttons) {
            return new Config(message, List.of(buttons), true);
        }

        /** Hide the entire bottom tab and all buttons. Overlay dismisses only via forceClose(). */
        public Config withNoButtons() {
            return new Config(message, List.of(), false);
        }

        /** Show a button bar but with no buttons in it (empty bar still rendered). */
        public Config withEmptyButtonBar() {
            return new Config(message, List.of(), true);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SINGLETON
    // ─────────────────────────────────────────────────────────────

    private static volatile OverlayNotification instance;

    public static OverlayNotification getInstance() {
        if (instance == null) {
            synchronized (OverlayNotification.class) {
                if (instance == null) {
                    instance = new OverlayNotification();
                }
            }
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────────
    // INTERNAL STATE
    // ─────────────────────────────────────────────────────────────

    private final MessagePanel messagePanel;
    private volatile Consumer<NotificationResult> currentCallback;

    private OverlayNotification() {
        setUndecorated(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        fitToScreen();

        // Re-fit if display resolution changes at runtime
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { fitToScreen(); }
        });

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // 2. Add the window listener to handle the close event
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                messagePanel.setVisible(false);
                repaint();
                Consumer<NotificationResult> cb = currentCallback;
                if (cb != null) {
                    currentCallback = null;
                    cb.accept(NotificationResult.DISMISS);
                }
                dispose();
            }
        });

        setLayout(new BorderLayout());
        messagePanel = new MessagePanel();
        messagePanel.setVisible(false);
        add(messagePanel, BorderLayout.CENTER);
    }

    private void fitToScreen() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen);
        setLocation(0, 0);
    }

    // ─────────────────────────────────────────────────────────────
    // PUBLIC DISPLAY API
    // ─────────────────────────────────────────────────────────────

    /**
     * Shows the overlay with the given configuration.
     * Safe to call from any thread.
     *
     * @param config   Display configuration (message, buttons, layout).
     * @param callback Receives the {@link NotificationResult} when dismissed.
     *                 Always called exactly once — either by a button or by {@link #forceClose()}.
     */
    public void showNotification(Config config, Consumer<NotificationResult> callback) {
        SwingUtilities.invokeLater(() -> {
            this.currentCallback = callback;
            messagePanel.apply(config, this::handleResult);
            messagePanel.setVisible(true);
            setVisible(true);
            toFront();
            repaint();
        });
    }

    /**
     * Hides the overlay without firing any callback.
     * Safe to call from any thread.
     */
    public void hideNotification() {
        SwingUtilities.invokeLater(() -> {
            messagePanel.setVisible(false);
            repaint();
        });
    }

    /**
     * Tears down the overlay instantly and fires the pending callback with
     * {@link NotificationResult#DISMISS}. Safe to call from any thread in any package.
     *
     * <p>Idempotent — calling multiple times is safe; the callback fires only once.
     */
    public void forceClose() {
        SwingUtilities.invokeLater(() -> {
            messagePanel.setVisible(false);
            repaint();
            Consumer<NotificationResult> cb = currentCallback;
            if (cb != null) {
                currentCallback = null;
                cb.accept(NotificationResult.DISMISS);
            }
            System.out.println("[OverlayNotification] Force-closed.");
        });
    }

    // ─────────────────────────────────────────────────────────────
    // INTERNAL RESULT HANDLER
    // ─────────────────────────────────────────────────────────────

    private void handleResult(NotificationResult result) {
        hideNotification();
        Consumer<NotificationResult> cb = currentCallback;
        if (cb != null) {
            currentCallback = null;
            cb.accept(result);
        }
        dispose();
    }

    // ─────────────────────────────────────────────────────────────
    // INNER PANEL — builds itself from Config each show()
    // ─────────────────────────────────────────────────────────────

    private static final class MessagePanel extends JPanel {

        private static final Color OVERLAY_BG   = new Color(0, 0, 0, 160);
        private static final Color CARD_BG       = new Color(30, 30, 30, 230);
        private static final Color TEXT_COLOR    = new Color(240, 240, 240);
        private static final Color BUTTON_BG     = new Color(55, 55, 55);
        private static final Color BUTTON_HOVER  = new Color(80, 80, 80);
        private static final Color BUTTON_BORDER = new Color(100, 100, 100);
        private static final Font  MSG_FONT      = new Font("Segoe UI", Font.PLAIN, 22);
        private static final Font  BTN_FONT      = new Font("Segoe UI", Font.PLAIN, 15);

        // Card dimensions
        private static final int CARD_PAD_X    = 48;
        private static final int CARD_PAD_Y    = 36;
        private static final int BTN_HEIGHT    = 40;
        private static final int BTN_MIN_W     = 110;
        private static final int BTN_GAP       = 12;
        private static final int BTN_BAR_TOP   = 20;

        private String message = "";
        private final List<NotificationButton> activeButtons = new ArrayList<>();
        private boolean showButtonBar = true;
        private Rectangle cachedCardBounds = null;

        MessagePanel() {
            setOpaque(false);
            setLayout(null); // manual layout for precise card centering
        }

        /** Reconfigure this panel for a new notification without rebuilding the component tree. */
        void apply(Config config, Consumer<NotificationResult> handler) {
            this.message = config.message;
            this.showButtonBar = config.showButtonBar;
            this.activeButtons.clear();
            this.activeButtons.addAll(config.buttons);
            this.cachedCardBounds = null;

            removeAll();

            if (showButtonBar && !activeButtons.isEmpty()) {
                JPanel bar = buildButtonBar(handler);
                add(bar);
                // Position during paint via doLayout
                putClientProperty("buttonBar", bar);
            } else {
                putClientProperty("buttonBar", null);
            }

            revalidate();
            repaint();
        }

        @Override
        public void doLayout() {
            super.doLayout();
            JPanel bar = (JPanel) getClientProperty("buttonBar");
            if (bar == null) return;

            // Place button bar inside the card; card is centred — compute card bounds first
            Rectangle card = cardBounds();
            int barW = bar.getPreferredSize().width;
            int barH = BTN_HEIGHT;
            int barX = card.x + (card.width - barW) / 2;
            int barY = card.y + card.height - CARD_PAD_Y - barH;
            bar.setBounds(barX, barY, barW, barH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            // Dim full screen
            g2.setColor(OVERLAY_BG);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Centred card
            Rectangle card = cardBounds();
            g2.setColor(CARD_BG);
            g2.fillRoundRect(card.x, card.y, card.width, card.height, 18, 18);

            // Message text — wrapped inside card width minus padding
            g2.setColor(TEXT_COLOR);
            g2.setFont(MSG_FONT);
            int textY = card.y + CARD_PAD_Y + g2.getFontMetrics().getAscent();
            drawWrappedText(g2, message, card.x, textY, card.width);

            g2.dispose();
        }

        private Rectangle cardBounds() {
            if (cachedCardBounds != null) return cachedCardBounds;
            FontMetrics fm = getFontMetrics(MSG_FONT);
            int maxTextW = Math.min(getWidth() - 120, 560);
            int textH = wrappedTextHeight(fm, message, maxTextW);

            int btnBarH = showButtonBar
                    ? BTN_BAR_TOP + BTN_HEIGHT + 8   // +8 bottom breathing room
                    : 0;

            int cardW = maxTextW + CARD_PAD_X * 2;
            int cardH = CARD_PAD_Y + textH + btnBarH + CARD_PAD_Y;

            // Ensure card is never too small to show its contents
            int minCardH = CARD_PAD_Y * 2 + 40 + (showButtonBar ? BTN_BAR_TOP + BTN_HEIGHT + 8 : 0);
            cardH = Math.max(cardH, minCardH);

            int cx = (getWidth() - cardW) / 2;
            int cy = (getHeight() - cardH) / 2;
            cachedCardBounds = new Rectangle(cx, cy, cardW, cardH);
            return cachedCardBounds;
        }

        private JPanel buildButtonBar(Consumer<NotificationResult> handler) {
            // Measure all button labels to keep widths symmetrical
            // Use a more reliable metrics source, and increase padding
            FontMetrics fm = getFontMetrics(BTN_FONT) != null
                    ? getFontMetrics(BTN_FONT)
                    : new JLabel().getFontMetrics(BTN_FONT);
            int maxLabelW = activeButtons.stream()
                    .mapToInt(b -> fm.stringWidth(b.label()) + 48) // 24px each side instead of 16
                    .max().orElse(BTN_MIN_W);
            int btnW = Math.max(maxLabelW, BTN_MIN_W);

            JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, BTN_GAP, 0));
            bar.setOpaque(false);

            for (NotificationButton btn : activeButtons) {
                JButton b = styledButton(btn.label(), btnW);
                b.addActionListener(e -> handler.accept(btn.result()));
                bar.add(b);
            }

            int totalW = activeButtons.size() * btnW
                    + (activeButtons.size() - 1) * BTN_GAP
                    + BTN_GAP * 2; // FlowLayout adds hgap on left and right edges too
            //Huh, so that's the problem
            bar.setPreferredSize(new Dimension(totalW, BTN_HEIGHT));
            return bar;
        }

        private JButton styledButton(String label, int width) {
            JButton b = new JButton(label) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? BUTTON_HOVER : BUTTON_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(BUTTON_BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            b.setFont(BTN_FONT);
            b.setForeground(TEXT_COLOR);
            b.setPreferredSize(new Dimension(width, BTN_HEIGHT));
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }

        // ── Text wrapping helpers ──────────────────────────────────

        private void drawWrappedText(Graphics2D g2, String text, int cardX, int y, int cardW) {
            FontMetrics fm = g2.getFontMetrics();
            int lineH = fm.getHeight();
            int textW = cardW - CARD_PAD_X * 2;
            for (String line : wrapText(fm, text, textW)) {
                int lineW = fm.stringWidth(line);
                int x = cardX + (cardW - lineW) / 2; // center each line within card
                g2.drawString(line, x, y);
                y += lineH;
            }
        }

        private int wrappedTextHeight(FontMetrics fm, String text, int maxW) {
            return wrapText(fm, text, maxW).size() * fm.getHeight();
        }

        private List<String> wrapText(FontMetrics fm, String text, int maxW) {
            List<String> lines = new ArrayList<>();
            for (String paragraph : text.split("\n", -1)) {
                String[] words = paragraph.split(" ");
                StringBuilder current = new StringBuilder();
                for (String word : words) {
                    String candidate = current.isEmpty() ? word : current + " " + word;
                    if (fm.stringWidth(candidate) > maxW && !current.isEmpty()) {
                        lines.add(current.toString());
                        current = new StringBuilder(word);
                    } else {
                        current = new StringBuilder(candidate);
                    }
                }
                lines.add(current.toString());
            }
            return lines;
        }
    }
}

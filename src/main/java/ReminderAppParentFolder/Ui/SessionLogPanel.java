package ReminderAppParentFolder.Ui;
import ReminderAppParentFolder.Storage.StorageManager;
import ReminderAppParentFolder.tracking.SessionLog;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * SessionLogPanel — a dark-themed scrollable list of session cards.
 * Each card shows: session name (top-left), creation date (top-right),
 * completion status (bottom-left), duration (bottom-right).
 * Sessions are sorted by creation date (most recent first).
 */
public class SessionLogPanel extends JPanel {

    // ── Palette ─────────────────────────────────────────────────────────────
    private static final Color BG_PANEL     = new Color(0x1A, 0x1A, 0x2B);
    private static final Color TEXT_DIM     = new Color(0x7A, 0x7A, 0xA8);

    private static final Color STATUS_DONE  = new Color(0x4C, 0xC9, 0x8A);
    private static final Color STATUS_PROG  = new Color(0xF5, 0xA6, 0x23);
    private static final Color STATUS_PEND  = new Color(0x7A, 0x7A, 0xA8);

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm");

    // ── Data model ───────────────────────────────────────────────────────────
    public enum CompletionStatus { FINISHED, INCOMPLETE, CANCELED, UNKNOWN}

     public CompletionStatus stringCompletion2Completion(String text) {
        if (text.equalsIgnoreCase("FINISHED")) {
            return CompletionStatus.FINISHED;
        }  else if (text.equalsIgnoreCase("INCOMPLETE")) {
            return CompletionStatus.INCOMPLETE;
        } else if (text.equalsIgnoreCase("CANCELED")) {
            return CompletionStatus.CANCELED;
        }
        return CompletionStatus.UNKNOWN;
    }
    public static class Session {
        final String          name;
        final LocalDateTime   createdAt;
        final CompletionStatus status;
        final long            durationSeconds;

        public Session(String name, LocalDateTime createdAt,
                       CompletionStatus status, long durationSeconds) {
            this.name            = name;
            this.createdAt       = createdAt;
            this.status          = status;
            this.durationSeconds = durationSeconds;
        }

        String formattedDuration() {
            long h = durationSeconds / 3600;
            long m = (durationSeconds % 3600) / 60;
            long s = durationSeconds % 60;
            if (h > 0) return String.format("%dh %02dm %02ds", h, m, s);
            if (m > 0) return String.format("%dm %02ds", m, s);
            return String.format("%ds", s);
        }
    }

    // ── Session card component ────────────────────────────────────────────────
    private static class SessionCard extends JPanel {

        private boolean hovered = false;
        private final Session session;

        SessionCard(Session s) {
            this.session = s;
            setLayout(new GridBagLayout());
            setOpaque(true);
            setBackground(Theme.BG_CARD);
            setBorder(new CompoundBorder(
                    new MatteBorder(0, 2, 0, 0, Theme.BORDER_COLOR),
                    new EmptyBorder(10, 12, 10, 12)));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            build();
            wireHover();
        }

        private void build() {
            GridBagConstraints gbc = new GridBagConstraints();

            // ── Row 1: session name (left) + creation date (right) ──────────
            JLabel nameLabel = styledLabel(session.name, Theme.TEXT_PRIMARY, 13f, Font.BOLD);
            JLabel dateLabel = styledLabel(session.createdAt.format(DISPLAY_FMT),
                    Theme.TEXT_SECONDARY, 11f, Font.PLAIN);

            gbc.gridx = 0; gbc.gridy = 0;
            gbc.weightx = 1.0; gbc.weighty = 0.5;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill   = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 2, 8);
            add(nameLabel, gbc);

            gbc.gridx = 1; gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.fill   = GridBagConstraints.NONE;
            gbc.insets = new Insets(0, 0, 2, 0);
            add(dateLabel, gbc);

            // ── Row 2: completion status (left) + duration (right) ──────────
            JLabel statusLabel = buildStatusLabel(session.status);
            JLabel durLabel    = styledLabel(session.formattedDuration(),
                    TEXT_DIM, 11f, Font.PLAIN);

            gbc.gridx = 0; gbc.gridy = 1;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill   = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 0, 8);
            add(statusLabel, gbc);

            gbc.gridx = 1; gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.fill   = GridBagConstraints.NONE;
            gbc.insets = new Insets(0, 0, 0, 0);
            add(durLabel, gbc);
        }

        private JLabel buildStatusLabel(CompletionStatus st) {
            String text; Color color;
            switch (st) {
                case FINISHED:
                    text = "● Finished"; color = STATUS_DONE;  break;
                case INCOMPLETE:
                    text = "● Incomplete"; color = STATUS_PROG; break;
                case CANCELED:
                    text = "● Cancelled";    color = STATUS_PEND; break;
                default:
                    text = "● Unknown";    color = Theme.BORDER_COLOR; break;
            }
            return styledLabel(text, color, 11f, Font.PLAIN);
        }

        private JLabel styledLabel(String text, Color fg, float size, int style) {
            JLabel l = new JLabel(text);
            l.setForeground(fg);
            l.setFont(new Font("Segoe UI", style, (int) size));
            l.setOpaque(false);
            return l;
        }

        private void wireHover() {
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    setBackground(Theme.BG_FIELD);
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    hovered = false;
                    setBackground(Theme.BG_CARD);
                    repaint();
                }
            });
        }
    }

    // ── Panel plumbing ────────────────────────────────────────────────────────
    private final JPanel   listPanel;
    private final List<Session> sessions = new ArrayList<>();

    public SessionLogPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_CONTENT);

        // header
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // scrollable card list
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Theme.BG_CONTENT);
        listPanel.setBorder(new EmptyBorder(6, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Theme.BG_CONTENT);
        scroll.getViewport().setBackground(Theme.BG_CONTENT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setBackground(Theme.BG_CARD);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Theme.BG_CONTENT);
        h.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BG_CARD),
                new EmptyBorder(10, 14, 10, 14)));

        JLabel title = new JLabel("Session Log");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Sorted by creation date");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(TEXT_DIM);

        h.add(title, BorderLayout.WEST);
        h.add(sub,   BorderLayout.EAST);
        return h;
    }

    /** Add a session and re-render the sorted list. */
    public void addSession(Session s) {
        sessions.add(s);
        refresh();
    }

    /** Replace all sessions at once. */
    public void setSessions(List<Session> list) {
        sessions.clear();
        sessions.addAll(list);
        refresh();
    }

    private void refresh() {
        // sort newest first
        sessions.sort(Comparator.comparing((Session s) -> s.createdAt).reversed());

        listPanel.removeAll();
        if (sessions.isEmpty()) {
            listPanel.add(emptyState());
        } else {
            for (Session s : sessions) {
                listPanel.add(new SessionCard(s));
                listPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel emptyState() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG_CARD);
        p.setPreferredSize(new Dimension(300, 200));

        JLabel icon = new JLabel("⊞");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        icon.setForeground(Theme.BORDER_COLOR);

        JLabel msg = new JLabel("No sessions recorded");
        msg.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        msg.setForeground(TEXT_DIM);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0,0,8,0);
        p.add(icon, gbc);
        gbc.gridy = 1;
        p.add(msg,  gbc);
        return p;
    }

    public void forceLoadSessionLog() {
        List<SessionLog> logs = StorageManager.getInstance().logs().loadAll();
        for (SessionLog s : logs) {
            addSession(new Session(s.getSessionName(), s.getCreatedAt(), stringCompletion2Completion(s.getCompletionStatus()), s.getDuration()));
        }

    }
}
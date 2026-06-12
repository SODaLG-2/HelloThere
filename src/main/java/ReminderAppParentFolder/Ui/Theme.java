/*
package main.java.ReminderAppParentFolder.Ui;
import java.awt.Color;

public interface Theme {
    static final Color BG_DARK       = new Color(18, 18, 24);
    static final Color BG_SIDEBAR     = new Color(26, 26, 36);
    static final Color BG_CONTENT     = new Color(22, 22, 30);
    static final Color ACCENT         = new Color(99, 102, 241);   // indigo
    static final Color ACCENT_HOVER   = new Color(129, 132, 255);
    static final Color TEXT_PRIMARY   = new Color(230, 230, 240);
    static final Color TEXT_SECONDARY = new Color(140, 140, 160);
    //static final Color BORDER_COLOR  = new Color(45, 45, 60);
    static final Color BORDER_COLOR  = new Color(180, 30, 50);
}
*/


package ReminderAppParentFolder.Ui;
import java.awt.Font;
import java.awt.Color;

public final class Theme {

    private Theme() {}

    // Backgrounds
    public static final Color BG_DARK       = new Color(18, 18, 24);
    public static final Color BG_SIDEBAR    = new Color(26, 26, 36);
    public static final Color BG_CONTENT    = new Color(22, 22, 30);
    public static final Color BG_CARD       = new Color(30, 30, 42);
    public static final Color BG_FIELD      = new Color(40, 40, 55);

    // Accent
    public static final Color ACCENT        = new Color(99, 102, 241);   // indigo
    public static final Color ACCENT_HOVER  = new Color(129, 132, 255);

    // Text
    public static final Color TEXT_PRIMARY   = new Color(230, 230, 240);
    public static final Color TEXT_SECONDARY = new Color(140, 140, 160);
    public static final Color TEXT_BLACK = new Color(0,0,0);

    // Borders — intentional red accent, not a debug leftover
    public static final Color BORDER_COLOR  = new Color(255, 0, 0);

    // Fonts
    public static final Font FONT_TITLE     = new Font("NanumGothic Coding",    Font.ITALIC, 18);
    public static final Font FONT_HEADING   = new Font("Dialog",    Font.BOLD,   20);
    public static final Font FONT_NAV       = new Font("SansSerif",  Font.PLAIN,  14);
    public static final Font FONT_MONO      = new Font("Monospaced", Font.PLAIN,  11);
    public static final Font FONT_BODY      = new Font("SansSerif",  Font.PLAIN,  14);
}
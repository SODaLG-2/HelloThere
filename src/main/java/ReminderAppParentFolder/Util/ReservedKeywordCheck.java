package ReminderAppParentFolder.Util;

import java.util.Set;

public class ReservedKeywordCheck {
    private static final Set<String> RESERVED =
            Set.of(
                    "CON", "PRN", "AUX", "NUL",
                    "COM1", "COM2", "COM3",
                    "LPT1", "LPT2"
            );

    public static boolean InReservedKeyword(String keyword) {
        return RESERVED.contains(keyword.toUpperCase());
    }
}

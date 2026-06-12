package ReminderAppParentFolder.Util;

import javax.swing.text.*;

public class FileNameDocumentFilter extends DocumentFilter {

    // Windows illegal filename chars
    private static final String ILLEGAL =
            "\\\\/:*?\"<>|";

    @Override
    public void insertString(FilterBypass fb,
                             int offset,
                             String string,
                             AttributeSet attr)
            throws BadLocationException {

        if (string != null) {

            string = sanitize(string);

            super.insertString(
                    fb,
                    offset,
                    string,
                    attr
            );
        }
    }

    @Override
    public void replace(FilterBypass fb,
                        int offset,
                        int length,
                        String text,
                        AttributeSet attrs)
            throws BadLocationException {

        if (text != null) {

            text = sanitize(text);

            super.replace(
                    fb,
                    offset,
                    length,
                    text,
                    attrs
            );
        }
    }

    private String sanitize(String text) {

        StringBuilder sb =
                new StringBuilder();

        for (char c : text.toCharArray()) {

            if (ILLEGAL.indexOf(c) == -1) {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
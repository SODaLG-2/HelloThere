package ReminderAppParentFolder.Util;

import javax.swing.text.*;

/**
 * A DocumentFilter that only allows numeric (digit) characters.
 * Attach to any JTextField via:
 *   ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumericDocumentFilter());
 */
public class NumericDocumentFilter extends DocumentFilter {

    @Override
    public void insertString(FilterBypass fb, int offset,
                             String text, AttributeSet attr)
                             throws BadLocationException {
        if (isNumeric(text))
            super.insertString(fb, offset, text, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length,
                        String text, AttributeSet attr)
                        throws BadLocationException {
        if (isNumeric(text))
            super.replace(fb, offset, length, text, attr);
    }

    private boolean isNumeric(String text) {
        return text != null && text.chars().allMatch(Character::isDigit);
    }
}

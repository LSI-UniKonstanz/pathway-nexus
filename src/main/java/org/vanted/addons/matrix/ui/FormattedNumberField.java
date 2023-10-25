package org.vanted.addons.matrix.ui;

import java.text.NumberFormat;
import javax.swing.JFormattedTextField;
import java.awt.event.FocusEvent;

/**
 * A JFormattedTextField for decimal numbers that is allowed to be empty.
 */

public class FormattedNumberField extends JFormattedTextField {
    public FormattedNumberField(NumberFormat format) {
        super(format);
    }

    @Override
    protected void processFocusEvent(final FocusEvent e) {
        if (e.isTemporary())
            return;
        // If the user wants to clear the field let them.
        if (e.getID() == FocusEvent.FOCUS_LOST && (getText() == null  || getText().isEmpty()))
            setValue(null);

        super.processFocusEvent(e);
    }
}

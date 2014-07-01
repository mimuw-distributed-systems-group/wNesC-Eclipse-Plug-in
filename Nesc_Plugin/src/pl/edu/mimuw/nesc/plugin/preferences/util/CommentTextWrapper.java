package pl.edu.mimuw.nesc.plugin.preferences.util;

import com.google.common.base.Preconditions;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static org.eclipse.swt.SWT.*;

/**
 * Class that represents a wrapper for a Text object that is adjusted
 * to contain a comment text.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public final class CommentTextWrapper {
    /**
     * Name of the font that will be used in the Text control.
     */
    private static final String TEXT_FONT_NAME = "Courier New";

    /**
     * The <code>Text</code> object of the wrapper.
     */
    private final Text text;

    /**
     * Creates and configures the Text object. It is then added to the given
     * composite. If the layout data object is not null, it is associated with
     * the created Text control.
     *
     * @param parent Composite that will contain the created Text control.
     * @param layoutData Layout data object that will be associated with the
     *                   created Text control (if and only if it is not null).
     * @throws NullPointerException <code>parent</code> is null.
     */
    public CommentTextWrapper(Composite parent, Object layoutData) {
        // Check the arguments
        Preconditions.checkNotNull(parent, "parent cannot be null");

        // Create and configure the Text control
        text = new Text(parent, BORDER | MULTI | H_SCROLL | V_SCROLL);
        if (layoutData != null) {
            text.setLayoutData(layoutData);
        }

        // Set the font to a monospaced one
        final Font curFont = text.getFont();
        final FontData[] curFontData = curFont.getFontData();
        for (FontData fontData : curFontData) {
            fontData.setName(TEXT_FONT_NAME);
        }
        text.setFont(new Font(curFont.getDevice(), curFontData));
    }

    /**
     * @return The contents of the Text control wrapped by this object. It does
     *         not contain whitespace at the beginning and at the end.
     */
    public String getContents() {
        return text.getText().trim();
    }

    /**
     * Sets the contents of the underlying Text control to the given string.
     *
     * @param contents String that indicates the new contents of the underlying
     *                 Text control. Must not be null.
     * @throws NullPointerException Given argument is null.
     */
    public void setContents(String contents) {
        Preconditions.checkNotNull(contents, "contents cannot be null");
        text.setText(contents);
    }

    /**
     * Enables the controls in this wrapper if the given argument is true.
     * Otherwise, disables the controls.
     */
    public void setEnabled(boolean enabled) {
        text.setEnabled(enabled);
    }

    /**
     * @return True if and only if the comment entered by the user is valid.
     */
    public boolean validate() {
        return NescCommentValidator.getInstance().validate(getContents());
    }

    /**
     * Registers the given listener in the underlying Text control.
     *
     * @param listener Listener to be registered.
     */
    public void addModifyListener(ModifyListener listener) {
        text.addModifyListener(listener);
    }
}

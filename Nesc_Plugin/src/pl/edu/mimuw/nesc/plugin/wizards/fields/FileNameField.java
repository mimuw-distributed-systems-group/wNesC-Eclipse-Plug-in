package pl.edu.mimuw.nesc.plugin.wizards.fields;

import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Composite;

/**
 * Field that allows entering a name of a file.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public final class FileNameField extends TextField {
    /**
     * Initializes this field and creates all its controls.
     *
     * @param parent Composite that will contain controls of this field.
     * @param fieldName Name of the field.
     * @param layoutData Object that affects layout of the composite created by
     *                   this field.
     * @throws NullPointerException One of the arguments (except
     *                              <code>layoutData</code>) is null.
     */
    public FileNameField(Composite parent, String fieldName, Object layoutData) {
        super(parent, fieldName, layoutData);
    }

    @Override
    public String getErrorStatus() {
        final String name = getName();
        final String value = getValue();

        if (value.isEmpty()) {
            return name + " is empty.";
        }

        // Check if the entered value is a valid file name
        if (!new Path(value).isValidSegment(value)) {
            return "Entered " + name.toLowerCase() + " is ill-formed.";
        }

        return null;
    }
}

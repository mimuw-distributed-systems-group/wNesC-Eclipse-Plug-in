package pl.edu.mimuw.nesc.plugin.wizards.fields;

import org.eclipse.swt.widgets.Composite;

/**
 * Class that represents a wizard field that allows entering an identifier,
 * e.g. a nesC interface, module or component name.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public final class IdentifierField extends TextField {
    /**
     * Regular expression that defines the language of the valid values in this
     * field.
     */
    private static final String IDENTIFIER_REGEXP = "^[A-Za-z_]\\w*$";

    /**
     * Initializes the field with given values and creates its controls.
     *
     * @param parent Composite this one will be in.
     * @param fieldName Description of the value that this field will allow to
     *                  enter.
     * @param layoutData Layout data object to associate with the created
     *                   composite. It can be null.
     * @throws NullPointerException One of the arguments (other than
     *                              <code>layoutData</code>) is null.
     */
    public IdentifierField(Composite parent, String fieldName, Object layoutData) {
        super(parent, fieldName, layoutData);
    }

    @Override
    public String getErrorStatus() {
        final String fieldName = getName();
        final String value = getValue();

        if (!value.matches(IDENTIFIER_REGEXP)) {
            if (value.isEmpty()) {
                return fieldName + " cannot be empty.";
            } else if (Character.isDigit(value.charAt(0))) {
                return "The first character of the " + fieldName.toLowerCase() + " cannot be a digit.";
            } else {
                return "Only letters, digits and underscores are allowed in the "
                        + fieldName.toLowerCase() + ".";
            }
        }

        return null;
    }
}

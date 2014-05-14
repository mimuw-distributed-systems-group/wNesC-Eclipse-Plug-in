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

        switch(IdentifierValidator.getSyntaxInstance().validate(value)) {
        case SUCCESS:
            return null;
        case EMPTY:
            return fieldName + " cannot be empty.";
        case FIRST_CHAR_DIGIT:
            return "The first character of the " + fieldName.toLowerCase() + " cannot be a digit.";
        case FORBIDDEN_CHAR:
            return "Only letters, digits and underscores are allowed in the "
                + fieldName.toLowerCase() + ".";
        default:
            throw new RuntimeException("Unsupported identifier validation result.");
        }
    }
}

package pl.edu.mimuw.nesc.plugin.wizards.fields;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static org.eclipse.swt.SWT.*;

/**
 * Class that represents a wizard field that allows entering an identifier,
 * e.g. a nesC interface, module or component name.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public final class IdentifierField extends AbstractField {
    /**
     * Regular expression that defines the language of the valid values in this
     * field.
     */
    private static final String IDENTIFIER_REGEXP = "^[A-Za-z_]\\w*$";

    /**
     * Number of columns for this field in the grid layout.
     */
    private static final int FIELD_COLUMNS_COUNT = 2;

    /**
     * Control where user can enter the identifier. It is never null.
     */
    private final Text text;

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
        super(parent, fieldName, FIELD_COLUMNS_COUNT, layoutData);
        this.text = createText(getComposite());
    }

    @Override
    public void setFocus() {
        text.forceFocus();
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

    @Override
    public String getValue() {
        return text.getText();
    }

    /**
     * Adds a modify listener that will be notified when the value in this field
     * changed.
     *
     * @param listener Listener to add.
     */
    public void addModifyListener(ModifyListener listener) {
        text.addModifyListener(listener);
    }

    /**
     * Creates and returns a text control that is created as the child of the
     * given parent.
     *
     * @param parent Composite that will be the parent for the returned text
     *               control.
     * @return Newly created Text control.
     */
    private static Text createText(Composite parent) {
        final Text result = new Text(parent, SINGLE | BORDER);
        result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return result;
    }
}

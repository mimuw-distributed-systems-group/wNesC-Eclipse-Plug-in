package pl.edu.mimuw.nesc.plugin.wizards.fields;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static org.eclipse.swt.SWT.*;

/**
 * Class that represents a text field - a field with only a label and a text
 * controls.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public abstract class TextField extends AbstractField {
    /**
     * Number of columns in the grid layout for this field.
     */
    private static final int FIELD_COLUMNS_COUNT = 2;

    /**
     * The text control for this field.
     */
    private final Text text;

    /**
     * Creates all controls for this field.
     *
     * @param parent Composite that will contain controls of this field.
     * @param fieldName Name of the field.
     * @param layoutData Object that affects layout of the composite created for
     *                   this field.
     * @throws NullPointerException One of the arguments (except
     *                              <code>layoutData</code>) is null.
     */
    protected TextField(Composite parent, String fieldName, Object layoutData) {
        super(parent, fieldName, FIELD_COLUMNS_COUNT, layoutData);
        text = createText(getComposite());
    }

    @Override
    public final void setFocus() {
        text.forceFocus();
    }

    @Override
    public final String getValue() {
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

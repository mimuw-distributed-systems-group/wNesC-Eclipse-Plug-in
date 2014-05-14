package pl.edu.mimuw.nesc.plugin.wizards.fields;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

/**
 * Class that represents a field for choosing generic parameters of a component.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public final class GenericParametersField extends TableField<GenericParametersField.GenericParameter> {
    /**
     * Name of the field.
     */
    private static final String FIELD_NAME = "Generic parameters";

    /**
     * Texts used for the title of the generic parameter dialog.
     */
    private static final String TITLE_ADD = "New generic parameter";
    private static final String TITLE_EDIT = "Edit generic parameter";

    /**
     * Text used in the table.
     */
    private static final String CELL_TYPE_PARAM  = "typedef";
    private static final String CELL_NOT_APPLICABLE = "-";
    private static final String CELL_ATTRIBUTE_NONE = "none";
    private static final String CELL_ATTRIBUTE_INTEGER = "@integer()";
    private static final String CELL_ATTRIBUTE_NUMBER = "@number()";

    /**
     * Columns specification for this field.
     */
    private static final ColumnSpecification[] FIELD_COLUMN_SPEC = {
        new ColumnSpecification("Type", 80, "Type of the generic parameter"),
        new ColumnSpecification("Name", 120, "Name of the generic parameter"),
        new ColumnSpecification("Attribute", 80, "Attribute applied to the generic parameter")
    };

    /**
     * Shell that will be used as the parent for dialogs. Never null.
     */
    private final Shell shell;

    /**
     * @param parent Control that the field will be placed in.
     * @param layoutData Object that affects the layout of the composite
     *                   created for this field.
     * @param parentShell Parent of the data dialog for this field.
     * @throws NullPointerException A parameter is null (except
     *                              <code>layoutData</code>).
     */
    public GenericParametersField(Composite parent, Object layoutData, Shell parentShell) {
        super(parent, FIELD_NAME, layoutData, FIELD_COLUMN_SPEC);

        if (parentShell == null) {
            throw new NullPointerException("Parent shell is null.");
        }

        shell = parentShell;
    }

    @Override
    protected void addItemOperation() {
        // Prepare the dialog
        final GenericParameterDialog dialog = new GenericParameterDialog(shell, TITLE_ADD,
                prepareForbiddenNames(null));

        // Add new generic parameter if the user chooses so
        if (dialog.open() == Dialog.OK) {
            final GenericParameter newParam = dialog.getData();
            final TableItem newEntry = newItem(newParam);
            newEntry.setText(prepareRow(newParam));
        }
    }

    @Override
    protected void editItemOperation(TableItem tableItem, GenericParameter data) {
        // Prepare the dialog
        final GenericParameterDialog dialog = new GenericParameterDialog(shell, TITLE_EDIT,
                prepareForbiddenNames(data.getName()), data);

        // Modify the generic parameter if the user chooses so
        if (dialog.open() == Dialog.OK) {
            dialog.insertData(data);
            tableItem.setText(prepareRow(data));
        }
    }

    @Override
    public String getErrorStatus() {
        // Value in this field is always valid
        return null;
    }

    /**
     * Prepares the array with forbidden names for the data dialog.
     *
     * @param allowedName Name that must not be on the returned list or null is
     *                    there is no such name.
     * @return Array with forbidden names. It does not contain any string equal
     *         to the argument. Forbidden names are those that are names of
     *         other arguments.
     */
    private String[] prepareForbiddenNames(String allowedName) {
        List<String> forbiddenNames = new ArrayList<>();

        for (GenericParameter genericParam : getValue()) {
            if (!genericParam.name.equals(allowedName)) {
                forbiddenNames.add(genericParam.name);
            }
        }

        return forbiddenNames.toArray(new String[forbiddenNames.size()]);
    }

    /**
     * @param param Generic parameter to process.
     * @return String array with text for subsequent columns in the table for
     *         the given parameter data.
     */
    private String[] prepareRow(GenericParameter param) {
        final String[] result = new String[FIELD_COLUMN_SPEC.length];

        switch (param.type) {
        case TYPE_PARAM:
            result[0] = CELL_TYPE_PARAM;
            result[1] = param.getName();

            String attribute;
            if (param.getIntegerAttributeFlag() && param.getNumberAttributeFlag()) {
                throw new RuntimeException("Both attributes set for a generic parameter.");
            } else if (param.getIntegerAttributeFlag()) {
                attribute = CELL_ATTRIBUTE_INTEGER;
            } else if (param.getNumberAttributeFlag()) {
                attribute = CELL_ATTRIBUTE_NUMBER;
            } else {
                attribute = CELL_ATTRIBUTE_NONE;
            }
            result[2] = attribute;

            break;
        case CONSTANT_PARAM:
            result[0] = param.getConstantType();
            result[1] = param.getName();
            result[2] = CELL_NOT_APPLICABLE;
            break;
        default:
            throw new RuntimeException("Unsupported generic parameter type.");
        }

        return result;
    }

    /**
     * Class that represents a single generic parameter of a component. All
     * member variables and constructors can be accessed only by the package.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    public static class GenericParameter {
        /**
         * Type of the generic parameter.
         */
        Type type;

        /**
         * Type of the constant that will be the value of the generic parameter.
         * Valid only for CONSTANT_PARAM generic parameter type.
         */
        String constantType;

        /**
         * Name of the parameter.
         */
        String name;

        /**
         * True if and only the number attribute is applied to the parameter.
         * Valid only for TYPE_PARAM type.
         */
        boolean numberAttribute;

        /**
         * True if and only if the parameter has the integer attribute. Valid
         * only for TYPE_PARAM type.
         */
        boolean integerAttribute;

        /**
         * Initializes the object setting all its member fields to given values.
         */
        GenericParameter(Type type, String constantType, String name,
                boolean numberAttribute, boolean integerAttribute) {
            this.type = type;
            this.constantType = constantType;
            this.name = name;
            this.numberAttribute = numberAttribute;
            this.integerAttribute = integerAttribute;
        }

        /**
         * @return Type of the generic parameter.
         */
        public Type getType() {
            return type;
        }

        /**
         * @return Type of the constant that will be the value of the generic
         *         parameter. Valid only for constant (non-type) generic
         *         parameters.
         */
        public String getConstantType() {
            return constantType;
        }

        /**
         * @return Name of the generic parameter.
         */
        public String getName() {
            return name;
        }

        /**
         * @return True if and only if the parameter has the number attribute.
         *         Valid only for type parameters.
         */
        public boolean getNumberAttributeFlag() {
            return numberAttribute;
        }

        /**
         * @return True if and only if the parameter has the integer attribute.
         *         Valid only for type parameters.
         */
        public boolean getIntegerAttributeFlag() {
            return integerAttribute;
        }

        /**
         * Enumeration type that represents the type of a generic parameter of
         * a component.
         *
         * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
         */
        public enum Type {
            TYPE_PARAM,
            CONSTANT_PARAM
        }
    }
}

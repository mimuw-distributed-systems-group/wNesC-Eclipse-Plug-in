package pl.edu.mimuw.nesc.plugin.wizards.fields;

import pl.edu.mimuw.nesc.plugin.wizards.fields.GenericParametersField.GenericParameter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import static org.eclipse.swt.SWT.*;

/**
 * Dialog that is used for specifying properties of generic parameters.
 * Only for use in this package.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
final class GenericParameterDialog extends Dialog {
    /**
     * Some sizes for the dialog.
     */
    private static final int MAXIMUM_WIDTH_LABEL = 260;
    private static final int MINIMUM_HEIGHT_VALIDATION_LABEL = 34;

    /**
     * Indent that will be used for the attribute radios.
     */
    private static final int INDENT_ATTRIBUTE_RADIOS = 20;

    /**
     * String constant with 'char[]' value.
     */
    private static final String TYPE_CHAR_ARRAY = "char[]";

    /**
     * Contents of labels used in the dialog.
     */
    private static final String LABEL_GROUP_PARAM_TYPE = "Type of the parameter";
    private static final String LABEL_GROUP_ATTRIBUTES = "Attribute";
    private static final String LABEL_GROUP_NAME = "Name of the parameter";
    private static final String LABEL_GROUP_CONSTANT_TYPE = "Name of the constant type";
    private static final String LABEL_GROUP_VALIDATION = "Correctness information";
    private static final String LABEL_PARAM_TYPE_TYPE = "Type parameter";
    private static final String LABEL_PARAM_TYPE_CONSTANT = "Constant parameter";
    private static final String LABEL_ATTRIBUTE_CHECKBOX = "Use an attribute to limit values of "
            + "the type parameter";
    private static final String LABEL_NUMBER_ATTRIBUTE = "@number()";
    private static final String LABEL_INTEGER_ATTRIBUTE = "@integer()";
    private static final String ERR_MSG_NAME_EMPTY = "Name of the parameter is empty.";
    private static final String ERR_MSG_NAME_FIRST_CHAR_DIGIT = "First character of the parameter "
            + "name cannot be a digit.";
    private static final String ERR_MSG_NAME_FORBIDDEN_CHAR = "Only letters, digits and underscores "
            + "are allowed in the parameter name.";
    private static final String ERR_MSG_NAME_DUPLICATE = "A parameter with chosen name already exists.";
    private static final String ERR_MSG_CONSTANT_TYPE_EMPTY = "Name of the constant type is empty.";
    private static final String ERR_MSG_CONSTANT_TYPE_FIRST_CHAR_DIGIT = "First character of the "
            + "constant type name cannot be a digit.";
    private static final String ERR_MSG_CONSTANT_TYPE_FORBIDDEN_CHAR = "Only letters, digits and "
            + "underscores are allowed in the constant type name or value '" + TYPE_CHAR_ARRAY + "'.";

    /**
     * Indices of specific radio buttons in <code>typeRadios</code> and
     * <code>attributeRadios</code>.
     */
    private static final int RADIO_INDEX_TYPE = 0;
    private static final int RADIO_INDEX_CONSTANT = 1;
    private static final int RADIO_INDEX_NUMBER = 0;
    private static final int RADIO_INDEX_INTEGER = 1;

    /**
     * Names of the generic parameters that cause an error. Must not be null.
     */
    private final String[] forbiddenNames;

    /**
     * Object to take the initial values for the controls from. It may be
     * null. If so, default values are used.
     */
    private final GenericParameter initialValues;

    /**
     * Title of the dialog window. Must not be null.
     */
    private final String windowTitle;

    /**
     * Object that contains values that controls have right before closing
     * the dialog. Null if the dialog hasn't been closed yet.
     */
    private GenericParameter finalValues;

    /**
     * Controls to choose the type of the generic parameter.
     */
    private Group typeGroup;
    private final Button[] typeRadios = new Button[2];

    /**
     * Control for entering the type of the constant that will be the value
     * of the parameter.
     */
    private Group constantTypeGroup;
    private Text constantTypeText;

    /**
     * Control for entering the name of the generic parameter.
     */
    private Group nameGroup;
    private Text nameText;

    /**
     * Controls for choosing the attributes of the parameter.
     */
    private Group attributeGroup;
    private Button attributeCheckbox;
    private Button[] attributeRadios = new Button[2];

    /**
     * Label to show the information about validity of the entered data.
     */
    private Label validationInfoLabel;

    /**
     * Initializes the dialog with given parent shell. All controls have
     * default values.
     *
     * @param parent Parent of the window or null if no parent.
     * @param title Title of the dialog window.
     * @param forbiddenNames Names of the generic parameter that will not be
     *                       accepted by the dialog.
     * @throws NullPointerException <code>forbiddenNames</code> is null.
     * @throws IllegalArgumentException One of the elements from
     *                                  <code>forbiddenNames</code> is null.
     */
    GenericParameterDialog(Shell parent, String title, String[] forbiddenNames) {
        this(parent, title, forbiddenNames, null);
    }

    /**
     * Initializes the dialog with given parent shell. Before opening the
     * dialog controls have values corresponding to those from
     * <code>initialValues</code>. If this argument is null, then default
     * values are used.
     *
     * @param parent Parent of the window or null if no parent.
     * @param title Title of the dialog window.
     * @param forbiddenNames Names of the generic parameter that will not be
     *                       accepted by the dialog.
     * @param initialValues Object to take the values to show in the dialog.
     * @throws NullPointerException <code>forbiddenNames</code> is null.
     *                              <code>title</code> is null.
     * @throws IllegalArgumentException One of the elements from
     *                                  <code>forbiddenNames</code> is null.
     */
    GenericParameterDialog(Shell parent, String title, String[] forbiddenNames,
            GenericParameter initialValues) {
        super(parent);

        // Validate arguments
        if (forbiddenNames == null) {
            throw new NullPointerException("Forbidden names argument is null.");
        }
        for (String forbiddenName : forbiddenNames) {
            if (forbiddenName == null) {
                throw new IllegalArgumentException("Forbidden names array contains null.");
            }
        }
        if (title == null) {
            throw new NullPointerException("Title of the window is null.");
        }

        this.initialValues = initialValues;
        this.forbiddenNames = forbiddenNames;
        this.windowTitle = title;
    }

    @Override
    protected Composite createDialogArea(Composite parent) {
        final Composite dialogArea = (Composite) super.createDialogArea(parent);
        dialogArea.setLayoutData(new GridData(FILL, FILL, true, true));
        dialogArea.setLayout(new GridLayout());

        // Create all groups and their controls
        createGroupType(dialogArea);
        createGroupConstantType(dialogArea);
        createGroupName(dialogArea);
        createGroupAttribute(dialogArea);
        createGroupValidationInfo(dialogArea);

        // Create listeners to update error status
        createErrorStatusListeners();

        return dialogArea;
    }

    private void createGroupType(Composite dialogArea) {
        typeGroup = new Group(dialogArea, NONE);
        typeGroup.setLayoutData(new GridData(FILL, CENTER, true, true));
        typeGroup.setLayout(new GridLayout());
        typeGroup.setText(LABEL_GROUP_PARAM_TYPE);

        typeRadios[RADIO_INDEX_TYPE] = new Button(typeGroup, RADIO);
        typeRadios[RADIO_INDEX_TYPE].setText(LABEL_PARAM_TYPE_TYPE);
        typeRadios[RADIO_INDEX_TYPE].setSelection(true);
        typeRadios[RADIO_INDEX_TYPE].setLayoutData(new GridData(FILL, CENTER, true, true));

        typeRadios[RADIO_INDEX_CONSTANT] = new Button(typeGroup, RADIO);
        typeRadios[RADIO_INDEX_CONSTANT].setText(LABEL_PARAM_TYPE_CONSTANT);
        typeRadios[RADIO_INDEX_CONSTANT].setSelection(false);
        typeRadios[RADIO_INDEX_CONSTANT].setLayoutData(new GridData(FILL, CENTER, true, true));
    }

    private void createGroupConstantType(Composite dialogArea) {
        constantTypeGroup = new Group(dialogArea, NONE);
        constantTypeGroup.setLayoutData(new GridData(FILL, CENTER, true, true));
        constantTypeGroup.setLayout(new GridLayout());
        constantTypeGroup.setText(LABEL_GROUP_CONSTANT_TYPE);

        constantTypeText = new Text(constantTypeGroup, SINGLE | BORDER);
        constantTypeText.setSize(1000, constantTypeText.getSize().y);
        constantTypeText.setLayoutData(new GridData(FILL, CENTER, true, true));
    }

    private void createGroupName(Composite dialogArea) {
        nameGroup = new Group(dialogArea, NONE);
        nameGroup.setLayoutData(new GridData(FILL, CENTER, true, true));
        nameGroup.setLayout(new GridLayout());
        nameGroup.setText(LABEL_GROUP_NAME);

        nameText = new Text(nameGroup, SINGLE | BORDER);
        nameText.setLayoutData(new GridData(FILL, CENTER, true, true));
    }

    private void createGroupAttribute(Composite dialogArea) {
        final GridData checkboxLayoutData = new GridData(FILL, CENTER, true, true);
        checkboxLayoutData.widthHint = MAXIMUM_WIDTH_LABEL;
        final GridData attributeGroupLayoutData = new GridData(FILL, CENTER, true, true);
        attributeGroupLayoutData.minimumHeight = 100;

        attributeGroup = new Group(dialogArea, NONE);
        attributeGroup.setLayoutData(attributeGroupLayoutData);
        attributeGroup.setLayout(new GridLayout());
        attributeGroup.setText(LABEL_GROUP_ATTRIBUTES);

        attributeCheckbox = new Button(attributeGroup, CHECK | WRAP);
        attributeCheckbox.setSelection(false);
        attributeCheckbox.setText(LABEL_ATTRIBUTE_CHECKBOX);
        attributeCheckbox.setLayoutData(checkboxLayoutData);

        final Composite attributeRadiosComposite = new Composite(attributeGroup, NONE);
        final GridData attributeRadiosLayoutObject = new GridData(FILL, CENTER, true, true);
        attributeRadiosLayoutObject.horizontalIndent = INDENT_ATTRIBUTE_RADIOS;
        attributeRadiosComposite.setLayoutData(attributeRadiosLayoutObject);
        attributeRadiosComposite.setLayout(new GridLayout(2, true));

        attributeRadios[RADIO_INDEX_NUMBER] = new Button(attributeRadiosComposite, RADIO);
        attributeRadios[RADIO_INDEX_NUMBER].setText(LABEL_NUMBER_ATTRIBUTE);
        attributeRadios[RADIO_INDEX_NUMBER].setSelection(true);
        attributeRadios[RADIO_INDEX_NUMBER].setLayoutData(new GridData(FILL, CENTER, true, true));

        attributeRadios[RADIO_INDEX_INTEGER] = new Button(attributeRadiosComposite, RADIO);
        attributeRadios[RADIO_INDEX_INTEGER].setText(LABEL_INTEGER_ATTRIBUTE);
        attributeRadios[RADIO_INDEX_INTEGER].setSelection(false);
        attributeRadios[RADIO_INDEX_INTEGER].setLayoutData(new GridData(FILL, CENTER, true, true));
    }

    private void createGroupValidationInfo(Composite dialogArea) {
        final GridData labelLayoutData = new GridData(FILL, FILL, true, true);
        labelLayoutData.minimumHeight = MINIMUM_HEIGHT_VALIDATION_LABEL;

        final Group validationInfoGroup = new Group(dialogArea, NONE);
        validationInfoGroup.setLayout(new GridLayout());
        validationInfoGroup.setLayoutData(new GridData(FILL, CENTER, true, true));
        validationInfoGroup.setText(LABEL_GROUP_VALIDATION);

        validationInfoLabel = new Label(validationInfoGroup, WRAP);
        validationInfoLabel.setLayoutData(labelLayoutData);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(windowTitle);
    }

    @Override
    protected void okPressed() {
        finalValues = getDataFromControls();
        super.okPressed();
    }

    @Override
    protected void cancelPressed() {
        finalValues = getDataFromControls();
        super.cancelPressed();
    }

    @Override
    public int open() {
        create();
        setInitialValues();
        validateData();

        // Center the window if the parent shell is not null
        final Shell parentShell = getParentShell();
        if (parentShell != null) {
            final Shell shell = getShell();
            int x = parentShell.getLocation().x + parentShell.getSize().x / 2,
                y = parentShell.getLocation().y + parentShell.getSize().y / 2;
            x -= shell.getSize().x / 2;
            y -= shell.getSize().y / 2;
            shell.setLocation(x, y);
        }

        return super.open();
    }

    /**
     * Inserts initial values into the controls.
     */
    private void setInitialValues() {
        if (initialValues != null) {
            setData(initialValues);
        } else {
            clean();
        }
    }

    /**
     * Sets the data shown in the dialog to the ones given in parameter.
     */
    private void setData(GenericParameter data) {
        // Set the parameter type
        switch(data.getType()) {
        case TYPE_PARAM:
            typeRadios[RADIO_INDEX_TYPE].setSelection(true);;
            typeRadios[RADIO_INDEX_CONSTANT].setSelection(false);
            break;
        case CONSTANT_PARAM:
            typeRadios[RADIO_INDEX_CONSTANT].setSelection(true);
            typeRadios[RADIO_INDEX_TYPE].setSelection(false);
            break;
        default:
            throw new RuntimeException("Unknown generic parameter type.");
        }

        // Set the other values
        constantTypeText.setText(data.getConstantType());
        nameText.setText(data.getName());
        if (data.getIntegerAttributeFlag() || data.getNumberAttributeFlag()) {
            attributeCheckbox.setSelection(true);
            attributeRadios[RADIO_INDEX_NUMBER].setSelection(data.getNumberAttributeFlag());
            attributeRadios[RADIO_INDEX_INTEGER].setSelection(data.getIntegerAttributeFlag());
        } else {
            attributeCheckbox.setSelection(false);
            attributeRadios[RADIO_INDEX_NUMBER].setSelection(true);
            attributeRadios[RADIO_INDEX_INTEGER].setSelection(false);
        }
    }

    /**
     * Removes all values from the controls and sets them to the default
     * ones.
     */
    private void clean() {
       typeRadios[RADIO_INDEX_TYPE].setSelection(true);
       typeRadios[RADIO_INDEX_CONSTANT].setSelection(false);
       constantTypeText.setText("");
       nameText.setText("");
       attributeCheckbox.setSelection(false);
       attributeRadios[RADIO_INDEX_NUMBER].setSelection(true);
       attributeRadios[RADIO_INDEX_INTEGER].setSelection(false);
    }

    /**
     * Creates listener that triggers validation of the entered data and
     * sets it for all proper controls.
     */
    private void createErrorStatusListeners() {
        // Create listeners
        final ModifyListener modifyListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateData();
            }
        };
        final SelectionListener selectionListener = new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                validateData();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        };

        // Assign listeners
        for (Button typeRadio : typeRadios) {
            typeRadio.addSelectionListener(selectionListener);
        }
        for (Button attributeRadio : attributeRadios) {
            attributeRadio.addSelectionListener(selectionListener);
        }
        attributeCheckbox.addSelectionListener(selectionListener);
        nameText.addModifyListener(modifyListener);
        constantTypeText.addModifyListener(modifyListener);
    }

    /**
     * @return <code>GenericParameter</code> object that contains data that
     *         are currently contained in the controls.
     */
    private GenericParameter getDataFromControls() {
        final boolean numberAttribute = attributeCheckbox.getSelection()
                && attributeRadios[RADIO_INDEX_NUMBER].getSelection();

        final boolean integerAttribute = attributeCheckbox.getSelection()
                && attributeRadios[RADIO_INDEX_INTEGER].getSelection();

        return new GenericParameter(getParameterType(), getConstantType(),
                getParameterName(), numberAttribute, integerAttribute);
    }

    /**
     * @return Enumeration constant that corresponds to the parameter type
     *         that is currently selected in the dialog.
     */
    private GenericParameter.Type getParameterType() {
        if (typeRadios[RADIO_INDEX_TYPE].getSelection()) {
            return GenericParameter.Type.TYPE_PARAM;
        } else if (typeRadios[RADIO_INDEX_CONSTANT].getSelection()) {
            return GenericParameter.Type.CONSTANT_PARAM;
        }

        throw new RuntimeException("No generic parameter type has been chosen");
    }

    /**
     * @return Name of the parameter chosen by the user.
     */
    private String getParameterName() {
        return nameText.getText();
    }

    /**
     * @return Constant type chosen by the user.
     */
    private String getConstantType() {
        return constantTypeText.getText();
    }

    /**
     * Creates and returns an object that corresponds to the data
     * in the controls, entered by the user, right before closing the
     * window. If the window is not closed or has not been opened,
     * returns null.
     *
     * @return <code>GenericParameter</code> object that represents the data
     *         about the generic parameter that were right before closing the
     *         dialog. Null if the dialog is still opened or has not been
     *         opened yet.
     */
    GenericParameter getData() {
        return finalValues;
    }

    /**
     * Inserts data that was in the controls of this dialog right before
     * closing the window to the given parameter. Does nothing if the window
     * has not been yet opened or is still opened.
     *
     * @param genericParam Generic parameter object to be filled with data.
     */
    void insertData(GenericParameter genericParam) {
        if (finalValues == null) {
            return;
        }

        genericParam.type = finalValues.type;
        genericParam.constantType = finalValues.constantType;
        genericParam.name = finalValues.name;
        genericParam.numberAttribute = finalValues.numberAttribute;
        genericParam.integerAttribute = finalValues.integerAttribute;
    }

    /**
     * Informs the user about the error described by the string in the
     * argument. OK button is properly enabled or disabled depending on
     * the value of the argument. Null means no error.
     *
     * @param errMsg Error message or null is there is no error.
     */
    private void setErrorStatus(String errMsg) {
        getButton(IDialogConstants.OK_ID).setEnabled(errMsg == null);
        validationInfoLabel.setText(errMsg == null ? "OK" : errMsg);
    }

    /**
     * Enables or disables controls properly. Moreover, prints information
     * about the validation result.
     */
    private void validateData() {
        // Enable or disable controls properly
        final GenericParameter.Type selectedType = getParameterType();
        final boolean isConstant = selectedType == GenericParameter.Type.CONSTANT_PARAM,
                      isType = selectedType == GenericParameter.Type.TYPE_PARAM;
        constantTypeGroup.setEnabled(isConstant);
        constantTypeText.setEnabled(isConstant);
        attributeGroup.setEnabled(isType);
        attributeCheckbox.setEnabled(isType);
        for (Button attributeRadio : attributeRadios) {
            attributeRadio.setEnabled(isType && attributeCheckbox.getSelection());
        }

        // Check parameter name
        String errMsg;
        if ((errMsg = validateParameterName()) != null) {
            setErrorStatus(errMsg);
            return;
        }

        // Check constant type
        if ((errMsg = validateConstantType()) != null) {
            setErrorStatus(errMsg);
            return;
        }

        setErrorStatus(null);
    }

    /**
     * @return String describing an error in the parameter name value or
     *         null if it is alright.
     */
    private String validateParameterName() {
        final IdentifierValidator nameValidator = new IdentifierValidator(forbiddenNames);

        switch (nameValidator.validate(getParameterName())) {
        case EMPTY:
            return ERR_MSG_NAME_EMPTY;
        case DUPLICATE:
            return ERR_MSG_NAME_DUPLICATE;
        case FIRST_CHAR_DIGIT:
            return ERR_MSG_NAME_FIRST_CHAR_DIGIT;
        case FORBIDDEN_CHAR:
            return ERR_MSG_NAME_FORBIDDEN_CHAR;
        case SUCCESS:
            return null;
        default:
            throw new RuntimeException("Unsupported identifier validation result.");
        }
    }

    /**
     * @return String describing an error in the constant type value or null
     *         if it is alright.
     */
    private String validateConstantType() {
        final String constantType = getConstantType();

        if (getParameterType() != GenericParameter.Type.CONSTANT_PARAM) {
            return null;
        } else if (TYPE_CHAR_ARRAY.equals(constantType)) {
            return null;
        }

        switch (IdentifierValidator.getSyntaxInstance().validate(constantType)) {
        case EMPTY:
            return ERR_MSG_CONSTANT_TYPE_EMPTY;
        case FIRST_CHAR_DIGIT:
            return ERR_MSG_CONSTANT_TYPE_FIRST_CHAR_DIGIT;
        case FORBIDDEN_CHAR:
            return ERR_MSG_CONSTANT_TYPE_FORBIDDEN_CHAR;
        case SUCCESS:
            return null;
        default:
            throw new RuntimeException("Unsupported identifier validation result.");
        }
    }
}

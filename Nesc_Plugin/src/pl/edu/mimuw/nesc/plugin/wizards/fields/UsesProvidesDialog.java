package pl.edu.mimuw.nesc.plugin.wizards.fields;

import pl.edu.mimuw.nesc.plugin.wizards.fields.UsesProvidesField.UsesProvides;

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
 * Dialog that allows to specify a uses/provides entry.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
final class UsesProvidesDialog extends Dialog {
    /**
     * Regular expression for the interface name.
     */
    private static final String REG_EXP_IDENTIFIER = "[a-zA-Z_]\\w*";
    private static final String REG_EXP_INTERFACE_NAME = "^" + REG_EXP_IDENTIFIER
            + "(\\s*[<]" + REG_EXP_IDENTIFIER + "([,]\\s*" + REG_EXP_IDENTIFIER
            + ")*[>])?$";

    /**
     * Constants related to size of the dialog.
     */
    private static final int MAXIMUM_LABEL_WIDTH = 260;

    /**
     * Constants with indices for arrays.
     */
    private static final int RADIO_INDEX_USES = 0;
    private static final int RADIO_INDEX_PROVIDES = 1;

    /**
     * Various texts used in the dialog.
     */
    private static final String LABEL_GROUP_TYPE = "Type";
    private static final String LABEL_GROUP_INTERFACE_NAME = "Name of the interface";
    private static final String LABEL_GROUP_INSTANCE_SPEC = "Specification of the instance";
    private static final String LABEL_GROUP_VALIDATION = "Correctness information";
    private static final String LABEL_INFO_INSTANCE_SPEC = "You can specify the instance name "
            + "and/or parameters, e.g. \"Iface\", \"Many[uint8_t id]\" or \"[uint8_t id]\".";
    private static final String LABEL_RADIO_USES = "uses";
    private static final String LABEL_RADIO_PROVIDES = "provides";
    private static final String LABEL_STATUS_OK = "OK";
    private static final String ERR_MSG_NO_TYPE = "No type has been chosen.";
    private static final String ERR_MSG_INTERFACE_NAME_EMPTY = "Interface name cannot be empty.";
    private static final String ERR_MSG_INTERFACE_NAME_FIRST_CHAR_DIGIT = "Interface name cannot "
            + "start with a digit.";
    private static final String ERR_MSG_INTERFACE_NAME_OTHER = "Interface name is invalid.";
    private static final String ERR_MSG_INSTANCE_NAME_DUPLICATE = "An interface with given name "
            + "already exists.";
    private static final String ERR_MSG_INSTANCE_SPEC_INVALID = "Instance specification is invalid.";

    /**
     * Controls of the dialog.
     */
    private Group typeGroup;
    private final Button typeRadios[] = new Button[2];
    private Group interfaceNameGroup;
    private Text interfaceNameText;
    private Group instanceSpecGroup;
    private Text instanceSpecText;
    private Group validationInfoGroup;
    private Label validationInfoLabel;

    /**
     * Object that contains the initial values for controls. If null, default
     * values will be used.
     */
    private final UsesProvides initialValues;

    /**
     * Title of the window. Never null.
     */
    private final String windowTitle;

    /**
     * Object that contains the values that user has chosen (it doesn't matter
     * if the user has chosen Cancel or not). Null if the dialog is still opened
     * or has not been opened yet.
     */
    private UsesProvides finalValues;

    /**
     * Instance names that will not be accepted. Never null and all elements of
     * the array are also not null.
     */
    private final String[] forbiddenInstanceNames;

    /**
     * Equivalent to the other constructor with <code>initialValues</code>
     * argument as null.
     */
    UsesProvidesDialog(Shell parent, String title, String[] forbiddenInstanceNames) {
        this(parent, title, forbiddenInstanceNames, null);
    }

    /**
     * Initializes the dialog with given parameters.
     *
     * @param parent Parent of the dialog or null if no parent.
     * @param title Title of the window of the dialog. Must not be null.
     * @param forbiddenInstanceNames Instance names that will not be accepted.
     *                               Must not be null.
     * @param initialValues Values to insert into the controls before opening
     *                      the window. May be null.
     * @throws NullPointerException <code>title</code> or <code>forbiddenInstanceNames</code>
     *                              is null.
     * @throws IllegalArgumentException One of the elements of
     *                                  <code>forbiddenInstanceNames</code> is null.
     *
     */
    UsesProvidesDialog(Shell parent, String title, String[] forbiddenInstanceNames,
            UsesProvides initialValues) {
        super(parent);

        // Validate arguments
        if (title == null || forbiddenInstanceNames == null) {
            throw new NullPointerException("Title or forbidden instance names is null");
        }
        for (String forbiddenInstanceName : forbiddenInstanceNames) {
            if (forbiddenInstanceName == null) {
                throw new IllegalArgumentException("Forbidden instance names array contains null");
            }
        }

        this.windowTitle = title;
        this.forbiddenInstanceNames = forbiddenInstanceNames;
        this.initialValues = initialValues;
    }

    @Override
    protected Composite createDialogArea(Composite parent) {
        // Prepare the dialog area
        final Composite dialogArea = (Composite) super.createDialogArea(parent);
        dialogArea.setLayoutData(new GridData(FILL, FILL, true, true));

        // Create all groups and their controls
        createGroupType(dialogArea);
        createGroupInterfaceName(dialogArea);
        createGroupInstanceSpec(dialogArea);
        createGroupValidationInfo(dialogArea);

        // Create listeners for data changing
        createErrorStatusListeners();

        return dialogArea;
    }

    private void createGroupType(Composite dialogArea) {
        typeGroup = new Group(dialogArea, NONE);
        typeGroup.setLayoutData(new GridData(FILL, CENTER, true, true));
        typeGroup.setLayout(new GridLayout());
        typeGroup.setText(LABEL_GROUP_TYPE);

        typeRadios[RADIO_INDEX_USES] = new Button(typeGroup, RADIO);
        typeRadios[RADIO_INDEX_USES].setText(LABEL_RADIO_USES);
        typeRadios[RADIO_INDEX_USES].setLayoutData(new GridData(FILL, CENTER, true, true));

        typeRadios[RADIO_INDEX_PROVIDES] = new Button(typeGroup, RADIO);
        typeRadios[RADIO_INDEX_PROVIDES].setText(LABEL_RADIO_PROVIDES);
        typeRadios[RADIO_INDEX_PROVIDES].setLayoutData(new GridData(FILL, CENTER, true, true));
    }

    private void createGroupInterfaceName(Composite dialogArea) {
        interfaceNameGroup = new Group(dialogArea, NONE);
        interfaceNameGroup.setLayoutData(new GridData(FILL, CENTER, true, true));
        interfaceNameGroup.setLayout(new GridLayout());
        interfaceNameGroup.setText(LABEL_GROUP_INTERFACE_NAME);

        interfaceNameText = new Text(interfaceNameGroup, SINGLE | BORDER);
        interfaceNameText.setLayoutData(new GridData(FILL, CENTER, true, true));
    }

    private void createGroupInstanceSpec(Composite dialogArea) {
        // Prepare the layout for the group
        final GridLayout groupLayout = new GridLayout();
        groupLayout.verticalSpacing = 15;
        groupLayout.marginBottom = groupLayout.marginTop = 5;

        // Prepare the layout object for the label
        final GridData labelLayoutData = new GridData(CENTER, TOP, true, true);
        labelLayoutData.widthHint = MAXIMUM_LABEL_WIDTH;

        instanceSpecGroup = new Group(dialogArea, NONE);
        instanceSpecGroup.setLayoutData(new GridData(FILL, CENTER, true, true));
        instanceSpecGroup.setLayout(groupLayout);
        instanceSpecGroup.setText(LABEL_GROUP_INSTANCE_SPEC);

        final Label instanceSpecInfoLabel = new Label(instanceSpecGroup, WRAP);
        instanceSpecInfoLabel.setText(LABEL_INFO_INSTANCE_SPEC);
        instanceSpecInfoLabel.setLayoutData(labelLayoutData);

        instanceSpecText = new Text(instanceSpecGroup, SINGLE | BORDER);
        instanceSpecText.setLayoutData(new GridData(FILL, FILL, true, true));
    }

    private void createGroupValidationInfo(Composite dialogArea) {
        final GridData labelLayoutData = new GridData(FILL, FILL, true, true);
        labelLayoutData.widthHint = MAXIMUM_LABEL_WIDTH;

        validationInfoGroup = new Group(dialogArea, NONE);
        validationInfoGroup.setLayoutData(new GridData(FILL, CENTER, true, true));
        validationInfoGroup.setLayout(new GridLayout());
        validationInfoGroup.setText(LABEL_GROUP_VALIDATION);

        validationInfoLabel = new Label(validationInfoGroup, WRAP);
        validationInfoLabel.setLayoutData(labelLayoutData);
    }

    /**
     * Creates listeners for all changes in the input data that can imply
     * changes in the dialog.
     */
    private void createErrorStatusListeners() {
        // Create handlers
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
        final ModifyListener modifyListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateData();
            }
        };

        // Register handlers
        for (Button typeRadio : typeRadios) {
            typeRadio.addSelectionListener(selectionListener);
        }
        interfaceNameText.addModifyListener(modifyListener);
        instanceSpecText.addModifyListener(modifyListener);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        // Set the title of the window
        newShell.setText(windowTitle);
    }

    @Override
    public int open() {
        create();
        insertInitialValues();
        validateData();

        // Center the dialog
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

    @Override
    protected void okPressed() {
        finalValues = getData();
        super.okPressed();
    }

    /**
     * Shows information about error and enables or disables the OK button
     * properly.
     *
     * @param errMsg Error message or null if everything is alright.
     */
    private void setErrorStatus(String errMsg) {
        validationInfoLabel.setText(errMsg == null ? LABEL_STATUS_OK : errMsg);
        getButton(IDialogConstants.OK_ID).setEnabled(errMsg == null);
    }

    /**
     * Enables and disables controls properly and checks validity of the input
     * data. If it is not valid, an error is printed on the dialog.
     */
    private void validateData() {
        // Check type field
        if (!typeRadios[RADIO_INDEX_PROVIDES].getSelection()
                && !typeRadios[RADIO_INDEX_USES].getSelection()) {
            setErrorStatus(ERR_MSG_NO_TYPE);
            return;
        }

        // Check the interface name
        String errMsg;
        if ((errMsg = validateInterfaceName()) != null) {
            setErrorStatus(errMsg);
            return;
        }

        // Check the instance name
        if ((errMsg = validateInstanceSpec()) != null) {
            setErrorStatus(errMsg);
            return;
        }

        setErrorStatus(null);
    }

    /**
     * @return Error message depicting the error in the interface name or
     *         null if there is no error.
     */
    private String validateInterfaceName() {
        final String interfaceName = getInterfaceName();

        if (!interfaceName.matches(REG_EXP_INTERFACE_NAME)) {
            if (interfaceName.isEmpty()) {
                return ERR_MSG_INTERFACE_NAME_EMPTY;
            } else if (Character.isDigit(interfaceName.charAt(0))) {
                return ERR_MSG_INTERFACE_NAME_FIRST_CHAR_DIGIT;
            } else {
                return ERR_MSG_INTERFACE_NAME_OTHER;
            }
        }

        return null;
    }

    /**
     * @return Error message depicting the error in the instance specification
     *         or null if there is no error.
     */
    private String validateInstanceSpec() {
        final UsesProvidesField.UsesProvides.InstanceSpecificationParser parser =
                new UsesProvidesField.UsesProvides.InstanceSpecificationParser(getInstanceSpec());
        if (!parser.correct()) {
            return ERR_MSG_INSTANCE_SPEC_INVALID;
        }

        final String instanceName = parser.getInstanceName();
        if (instanceName.isEmpty()) {
            return null;
        }

        for (String deniedInstanceName : forbiddenInstanceNames) {
            if (deniedInstanceName.equals(instanceName)) {
                return ERR_MSG_INSTANCE_NAME_DUPLICATE;
            }
        }

        return null;
    }

    /**
     * @return Interface name entered by the user.
     */
    private String getInterfaceName() {
        return interfaceNameText.getText();
    }

    /**
     * @return Instance specification entered by the user.
     */
    private String getInstanceSpec() {
        return instanceSpecText.getText();
    }

    /**
     * Inserts initial values into the controls.
     */
    private void insertInitialValues() {
        if (initialValues == null) {
            clean();
        }
        else {
            setData(initialValues);
        }
    }

    /**
     * Inserts default values into the controls.
     */
    private void clean() {
        typeRadios[RADIO_INDEX_USES].setSelection(true);
        typeRadios[RADIO_INDEX_PROVIDES].setSelection(false);
        interfaceNameText.setText("");
        instanceSpecText.setText("");
    }

    /**
     * Inserts values from the parameter to the controls.
     *
     * @param values Values to insert to the controls.
     */
    private void setData(UsesProvides values) {
        // Type of the entry
        values.getType().accept(new UsesProvides.Type.Visitor() {
            @Override
            public void visit(UsesProvides.Uses marker) {
                typeRadios[RADIO_INDEX_USES].setSelection(true);
                typeRadios[RADIO_INDEX_PROVIDES].setSelection(false);
            }
            @Override
            public void visit(UsesProvides.Provides marker) {
                typeRadios[RADIO_INDEX_USES].setSelection(false);
                typeRadios[RADIO_INDEX_PROVIDES].setSelection(true);
            }
        });

        // Other values
        interfaceNameText.setText(values.getInterfaceName());
        instanceSpecText.setText(values.getInstanceSpec());
    }

    /**
     * @return Object that represents the values currently in the controls.
     */
    private UsesProvides getData() {
        // Retrieve the type
        final boolean usesSelected = typeRadios[RADIO_INDEX_USES].getSelection(),
                      providesSelected = typeRadios[RADIO_INDEX_PROVIDES].getSelection();
        UsesProvides.Type type;
        if (usesSelected && providesSelected) {
            throw new RuntimeException("Two uses/provides types chosen.");
        } else if (!usesSelected && !providesSelected) {
            throw new RuntimeException("No uses/provides type chosen.");
        } else if (usesSelected) {
            type = new UsesProvides.Uses();
        } else {
            type = new UsesProvides.Provides();
        }

        return new UsesProvides(type, getInterfaceName(), getInstanceSpec());
    }

    /**
     * @return UsesProvides object with values chosen by the user (if OK button
     *         has been clicked). If the dialog is still opened, has not been
     *         opened yet or button other than OK has been clicked, returns null.
     */
    public UsesProvides getUsesProvides() {
        return finalValues;
    }

    /**
     * Inserts all values chosen by user to given object. If the dialog is still
     * opened, has not been opened yet or the user has not clicked OK button,
     * does nothing.
     *
     * @param usesProvides Object to be filled with values.
     * @throws NullPointerException Given argument is null.
     */
    public void insertValues(UsesProvides usesProvides) {
        if (usesProvides == null) {
            throw new NullPointerException("UsesProvides object is null.");
        } else if (finalValues == null) {
            return;
        }

        usesProvides.setType(finalValues.getType());
        usesProvides.setInterfaceName(finalValues.getInterfaceName());
        usesProvides.setInstanceSpec(finalValues.getInstanceSpec());
    }
}

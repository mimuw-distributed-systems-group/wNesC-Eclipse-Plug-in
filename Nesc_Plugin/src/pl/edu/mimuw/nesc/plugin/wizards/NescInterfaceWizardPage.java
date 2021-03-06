package pl.edu.mimuw.nesc.plugin.wizards;

import pl.edu.mimuw.nesc.plugin.wizards.fields.*;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import static org.eclipse.swt.SWT.*;


/**
 * The only page of the new interface wizard.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public class NescInterfaceWizardPage extends WizardPage {
    /**
     * Path to the icon resource that will be shown in the top right corner of
     * the page.
     */
    private static final String ICON_PATH = "resources/icons/interfaceWizardIcon.png";

    /**
     * Texts shown in various places on this wizard page.
     */
    private static final String PAGE_NAME = "New NesC interface";
    private static final String PAGE_DESCRIPTION = "Create a new NesC interface.";
    private static final String NAME_INTERFACE_NAME_FIELD = "Interface name";
    private static final String LABEL_TYPE_PARAMETERS = "Type parameters:";
    private static final String LABEL_BUTTON_ADD = "Add...";
    private static final String LABEL_BUTTON_EDIT = "Edit...";
    private static final String LABEL_BUTTON_REMOVE = "Remove";
    private static final String LABEL_COMMENTS_CHECKBOX = "Generate comments";
    private static final String TITLE_NEW_TYPE_PARAM = "New type parameter";
    private static final String TITLE_EDIT_TYPE_PARAM = "Edit type parameter";
    private static final String BODY_NEW_TYPE_PARAM = "Enter a new type parameter name:";
    private static final String BODY_EDIT_TYPE_PARAM = "Enter a new name for the selected type parameter:";
    private static final String ERR_MSG_FILE_EXISTS = "Interface with given name already exists in the source folder.";
    private static final String ERR_MSG_TYPE_PARAM_EMPTY = "Type parameter name cannot be empty.";
    private static final String ERR_MSG_TYPE_PARAM_FIRST_CHAR_DIGIT = "First character cannot be a digit.";
    private static final String ERR_MSG_TYPE_PARAM_DENIED_CHAR = "Only letters, digits and underscores are allowed.";
    private static final String ERR_MSG_TYPE_PARAM_DUPLICATE = "Type parameter with given name already exists.";

    /**
     * Container for all controls on this page with grid layout.
     */
    private Composite composite;

    /**
     * Folder to put the new interface definition in.
     */
    private SourceFolderField sourceFolderField;

    /**
     * Field that contains the interface name when user writes it.
     */
    private IdentifierField interfaceNameField;

    /**
     * An array that contains two above fields for quick iteration.
     */
    private WizardField[] fields;

    /**
     * Control that allows specifying type parameters for the interface.
     */
    private List lstTypeParameters;

    /**
     * Buttons that allow manipulations of the type parameters.
     */
    private Button btnAdd;
    private Button btnEdit;
    private Button btnRemove;

    /**
     * Control that allows the user to select the option to automatically
     * add comments with some information to the new file.
     */
    private Button commentsCheckbox;

    /**
     * Object used to check the name of type parameters when adding them.
     */
    private final IInputValidator validator = new TypeParameterNameValidator();

    NescInterfaceWizardPage() {
        super(PAGE_NAME, PAGE_NAME, NescWizardSupport.getImageDescriptorForResource(ICON_PATH));
        setDescription(PAGE_DESCRIPTION);
    }

    @Override
    public void createControl(Composite parent) {
        // Create the composite and set the grid layout
        composite = new Composite(parent, NONE);
        composite.setLayout(new GridLayout());

        // Create controls for all data that will be gathered
        final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        final ModifyListener errorStatusUpdater = new ErrorModifyListener();
        sourceFolderField = SourceFolderField.builder()
                .initialValue(NescWizardSupport.getInitialSourceFolderFullPath())
                .modifyListener(errorStatusUpdater)
                .parentShell(getShell())
                .parentComposite(composite)
                .layoutData(gridData)
                .build();
        interfaceNameField = IdentifierField.builder()
                .fieldName(NAME_INTERFACE_NAME_FIELD)
                .modifyListener(errorStatusUpdater)
                .parentComposite(composite)
                .layoutData(gridData)
                .build();
        createTypeParametersField();

        commentsCheckbox = new Button(composite, CHECK);
        commentsCheckbox.setText(LABEL_COMMENTS_CHECKBOX);
        commentsCheckbox.setSelection(true);

        fields = new WizardField[] { sourceFolderField, interfaceNameField };

        // Final operations
        sourceFolderField.align(new AbstractField[] { interfaceNameField });
        setControl(composite);
        setPageComplete(false);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (!sourceFolderField.isEmpty()) {
            interfaceNameField.setFocus();
        }
    }

    /**
     * @return Path to file for the new interface to be created (in Eclipse
     *         "filesystem").
     */
    public String getNewInterfaceFullPath() {
        return sourceFolderField.getValue() + getNewInterfaceFileName();
    }

    /**
     * @return Only the name of the interface file that will be created (with
     *         extension).
     */
    public String getNewInterfaceFileName() {
        return interfaceNameField.getValue() + NescWizardSupport.NESC_SOURCE_EXTENSION;
    }

    /**
     * @return True if and only if the user has chosen to generate comments for
     *         the new file.
     */
    public boolean getCommentsFlag() {
        return commentsCheckbox.getSelection();
    }

    /**
     * @return A stream that contain initial contents of the new interface
     *         file.
     */
    public NewFileContents getNewInterfaceContents() {
        // Prepare writers
        final StringWriter strWriter = new StringWriter();
        final PrintWriter output = new PrintWriter(strWriter);

        // Add the comments if the user has chosen to do it
        if (getCommentsFlag()) {
            NescWizardSupport.writeComments(getNewInterfaceFullPath(), output);
        }

        // Write the initial contents of the interface file
        output.print("interface ");
        output.print(interfaceNameField.getValue());

        // Write type parameters if the interface is generic
        if (lstTypeParameters.getItemCount() > 0) {
            output.print('<');

            for (int i = 0; i < lstTypeParameters.getItemCount(); ++i) {
                if (i != 0) {
                    output.print(", ");
                }
                output.print(lstTypeParameters.getItem(i));
            }

            output.print('>');
        }

        // Other part of the interface
        output.println();
        output.println('{');
        output.print(NescWizardSupport.getIndentationStep());
        final int cursorOffset = strWriter.getBuffer().length();
        output.println();
        output.println('}');

        return new NewFileContents(new ByteArrayInputStream(strWriter.toString().getBytes()), cursorOffset);
    }

    private void updateErrorStatus() {
        // Iterate over all fields and look for an error
        for (WizardField field : fields) {
            final String errorMsg = field.getErrorStatus();
            if (errorMsg != null) {
                setErrorMessage(errorMsg);
                setPageComplete(false);
                return;
            }
        }

        // Check if the file already exists
        if (sourceFolderField.fileExists(getNewInterfaceFileName())) {
            setErrorMessage(ERR_MSG_FILE_EXISTS);
            setPageComplete(false);
            return;
        }

        // No errors have been found
        setErrorMessage(null);
        setPageComplete(true);
    }

    private void createTypeParametersField() {
        // Composite for the field
        final Composite typeParamsComposite = new Composite(composite, NONE);
        final GridLayout typeParamsLayout = new GridLayout();
        typeParamsLayout.numColumns = 3;
        typeParamsComposite.setLayout(typeParamsLayout);
        typeParamsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Label for this field
        final Label lblTypeParameters = new Label(typeParamsComposite, NONE);
        lblTypeParameters.setText(LABEL_TYPE_PARAMETERS);

        // List with type parameters
        lstTypeParameters = new List(typeParamsComposite, BORDER | SINGLE);
        lstTypeParameters.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Composite for buttons
        final Composite cstButtons = new Composite(typeParamsComposite, NONE);
        cstButtons.setLayout(new FillLayout(VERTICAL));

        // Buttons
        btnAdd = new Button(cstButtons, NONE);
        btnEdit = new Button(cstButtons, NONE);
        btnRemove = new Button(cstButtons, NONE);
        btnAdd.setText(LABEL_BUTTON_ADD);
        btnEdit.setText(LABEL_BUTTON_EDIT);
        btnEdit.setEnabled(false);
        btnRemove.setText(LABEL_BUTTON_REMOVE);
        btnRemove.setEnabled(false);

        // Listeners
        createTypeParamListListener();
        createAddButtonListener();
        createEditButtonListener();
        createRemoveButtonListener();
    }

    private void createTypeParamListListener() {
        lstTypeParameters.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                final boolean enabled = lstTypeParameters.getSelectionIndex() != -1;
                btnEdit.setEnabled(enabled);
                btnRemove.setEnabled(enabled);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
    }

    /**
     * This method creates and registers listener for a new type parameter
     * addition.
     */
    private void createAddButtonListener() {
        btnAdd.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // Create dialog to allow user input a text
                final InputDialog dialog = new InputDialog(getShell(), TITLE_NEW_TYPE_PARAM,
                        BODY_NEW_TYPE_PARAM, "", validator);
                dialog.open();
                if (dialog.getValue() != null && !dialog.getValue().equals("")) {
                    lstTypeParameters.add(dialog.getValue());
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
    }

    private void createEditButtonListener() {
        btnEdit.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // Create a dialog to allow user edit a type parameter
                final int selectedIndex = lstTypeParameters.getSelectionIndex();
                final String nameToEdit = lstTypeParameters.getItem(selectedIndex);
                final InputDialog dialog = new InputDialog(getShell(), TITLE_EDIT_TYPE_PARAM,
                        BODY_EDIT_TYPE_PARAM, nameToEdit, new TypeParameterNameValidator(nameToEdit));

                dialog.open();

                if (dialog.getValue() != null && !dialog.getValue().equals("")) {
                    lstTypeParameters.setItem(selectedIndex, dialog.getValue());
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
    }

    private void createRemoveButtonListener() {
        btnRemove.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                lstTypeParameters.remove(lstTypeParameters.getSelectionIndex());
                btnRemove.setEnabled(false);
                btnEdit.setEnabled(false);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
    }

    /**
     * Listener that will fire updating error status on modification.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    private class ErrorModifyListener implements ModifyListener {
        @Override
        public void modifyText(ModifyEvent e) {
            updateErrorStatus();
        }
    }

    /**
     * Class whose objects check if a type parameter name is valid and can be
     * set.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    private class TypeParameterNameValidator implements IInputValidator {
        /**
         * Name of the type parameter that is being edited. It can be null if
         * a new type parameter is added.
         */
        private final String editedName;

        private TypeParameterNameValidator(String editedName) {
            this.editedName = editedName;
        }

        private TypeParameterNameValidator() {
            this(null);
        }

        @Override
        public String isValid(String text) {
            if (text == null) {
                return ERR_MSG_TYPE_PARAM_EMPTY;
            }

            // Create the list of forbidden identifiers
            final java.util.List<String> immutableIds = Arrays.asList(lstTypeParameters.getItems());
            final java.util.List<String> wrongIdsList = new ArrayList<>(immutableIds);
            if (editedName != null) {
                wrongIdsList.removeAll(Arrays.asList(editedName));
            }
            final String[] wrongIds = wrongIdsList.toArray(new String[wrongIdsList.size()]);

            // Validate the argument
            String result;
            switch (new IdentifierValidator(wrongIds).validate(text)) {
            case EMPTY:
                result = ERR_MSG_TYPE_PARAM_EMPTY;
                break;
            case FIRST_CHAR_DIGIT:
                result = ERR_MSG_TYPE_PARAM_FIRST_CHAR_DIGIT;
                break;
            case FORBIDDEN_CHAR:
                result = ERR_MSG_TYPE_PARAM_DENIED_CHAR;
                break;
            case DUPLICATE:
                result = ERR_MSG_TYPE_PARAM_DUPLICATE;
                break;
            case SUCCESS:
                result = null;
                break;
            default:
                throw new RuntimeException("Unsupported identifier validation result.");
            }

            return result;
        }
    }
}

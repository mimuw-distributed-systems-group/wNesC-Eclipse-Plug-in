package pl.edu.mimuw.nesc.plugin.wizards;

import pl.edu.mimuw.nesc.plugin.wizards.fields.FileNameField;
import pl.edu.mimuw.nesc.plugin.wizards.fields.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import static org.eclipse.swt.SWT.*;


/**
 * The only page of the nesC header wizard.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public class NescHeaderFileWizardPage extends WizardPage {
    /**
     * Extension that is recommended for nesC header files.
     */
    private static final String RECOMMENDED_EXTENSION = ".h";

    /**
     * Path to this plug-in resource with icon for this wizard.
     */
    private static final String ICON_PATH = "icons/headerFileWizardIcon.png";

    /**
     * Various texts that appear in the wizard interface.
     */
    private static final String PAGE_NAME = "New nesC header file";
    private static final String PAGE_DESCRIPTION = "Create a new nesC header file.";
    private static final String LABEL_FILENAME = "Header file name";
    private static final String LABEL_GUARD_CHECKBOX = "Add header guard";
    private static final String ERR_MSG_FILE_EXISTS = "A file with given name already exists in "
            + "the selected source folder.";
    private static final String WARN_MSG_EXTENSION = "Entered header file name is not recommended - "
            + RECOMMENDED_EXTENSION + " extension is missing.";

    /**
     * The source folder field for this wizard.
     */
    private SourceFolderField sourceFolderField;

    /**
     * Field with the file name for the header.
     */
    private FileNameField fileNameField;

    /**
     * The control for choosing if the header guard will be created.
     */
    private Button headerGuardCheckbox;

    /**
     * Array with all fields from this wizard for quick iteration.
     */
    private WizardField[] fields;

    NescHeaderFileWizardPage() {
        super(PAGE_NAME, PAGE_NAME, getPageIcon());
        setDescription(PAGE_DESCRIPTION);
    }

    /**
     * @return Image descriptor with icon for this wizard or null if an error
     *         happens.
     */
    private static ImageDescriptor getPageIcon() {
        try {
            final URL iconURL = NescWizardSupport.getPluginResourceURL(ICON_PATH);
            return ImageDescriptor.createFromURL(iconURL);
        } catch(MalformedURLException e) {
            /* if an error happens, the wizard will not have any icon */
            return null;
        }
    }

    @Override
    public void createControl(Composite parent) {
        // Create the composite for this page
        final Composite pageComposite = new Composite(parent, NONE);
        final GridLayout pageLayout = new GridLayout();
        pageLayout.numColumns = 1;
        pageComposite.setLayout(pageLayout);

        // Create all fields
        final GridData layoutObject = new GridData(GridData.FILL_HORIZONTAL);
        sourceFolderField = new SourceFolderField(pageComposite, layoutObject,
                NescWizardSupport.getInitialSourceFolderFullPath(), getShell());
        fileNameField = new FileNameField(pageComposite, LABEL_FILENAME, layoutObject);
        fields = new WizardField[] { sourceFolderField, fileNameField };

        // Create the guard checkbox
        headerGuardCheckbox = new Button(pageComposite, CHECK);
        headerGuardCheckbox.setText(LABEL_GUARD_CHECKBOX);
        headerGuardCheckbox.setSelection(true);

        // Final actions
        registerCompletionListeners();
        setPageComplete(false);
        setControl(pageComposite);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        // Set focus to the name of the file if the source folder is entered
        if (!sourceFolderField.isEmpty()) {
            fileNameField.setFocus();
        }
    }

    /**
     * @return Full path of the header file to be created in Eclipse
     *         "filesystem".
     */
    public String getNewHeaderFileFullPath() {
        return sourceFolderField.getValue() + fileNameField.getValue();
    }

    /**
     * @return True if and only if the user have chosen to add the guard header.
     */
    public boolean getHeaderGuardFlag() {
        return headerGuardCheckbox.getSelection();
    }

    /**
     * @return Initial contents of the new header file to be created.
     */
    public NewFileContents getNewHeaderFileContents() {
        // Create all needed output streams
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(byteOut);

        // Create initial contents
        final String headerGuardDefine = generateHeaderGuardName();
        if (getHeaderGuardFlag()) {
            out.print("#ifndef ");
            out.println(headerGuardDefine);
            out.print("#define ");
            out.println(headerGuardDefine);
            out.println();
        }

        final int cursorOffset = byteOut.size();

        if (getHeaderGuardFlag()) {
            out.println();
            out.println();
            out.print("#endif /* ");
            out.print(headerGuardDefine);
            out.println(" */");
        }

        return new NewFileContents(new ByteArrayInputStream(byteOut.toByteArray()), cursorOffset);
    }

    /**
     * @return Header guard name for the 'define' preprocessor directive with
     *         the selected file name.
     */
    private String generateHeaderGuardName() {
        final String fileName = fileNameField.getValue().toUpperCase();
        final String afterDotsChanged = fileName.replace('.', '_');
        final String afterCleanup = afterDotsChanged.replaceAll("\\W+", "");

        return     !afterCleanup.isEmpty() && !Character.isDigit(afterCleanup.charAt(0))
                ?  afterCleanup
                :  "_" + afterCleanup;
    }

    /**
     * Updates error and warning status of the page. Allows completion of the
     * wizard if everything is alright.
     */
    private void updateErrorStatus() {
        // Look for errors in the fields
        for (WizardField field : fields) {
            final String errMsg = field.getErrorStatus();
            if (errMsg != null) {
                setErrorMessage(errMsg);
                setPageComplete(false);
                return;
            }
        }

        final String selectedFileName = fileNameField.getValue();

        // Check if the file name is unique
        if (sourceFolderField.fileExists(selectedFileName)) {
            setErrorMessage(ERR_MSG_FILE_EXISTS);
            setPageComplete(false);
            return;
        }

        // No errors have been found
        setErrorMessage(null);
        setPageComplete(true);

        // Look for warnings - check if the file name is recommended
        if (!selectedFileName.endsWith(RECOMMENDED_EXTENSION)) {
            setMessage(WARN_MSG_EXTENSION, WARNING);
            return;
        }

        // No warnings
        setMessage(null, WARNING);
    }

    /**
     * Registers all listeners that are needed to detect completion of the page.
     */
    private void registerCompletionListeners() {
        final ModifyListener listener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                updateErrorStatus();
            }
        };

        sourceFolderField.addModifyListener(listener);
        fileNameField.addModifyListener(listener);
    }
}

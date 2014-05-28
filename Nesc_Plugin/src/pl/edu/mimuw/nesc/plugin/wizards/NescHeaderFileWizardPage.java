package pl.edu.mimuw.nesc.plugin.wizards;

import pl.edu.mimuw.nesc.plugin.wizards.fields.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
    private static final String HEADER_FILE_EXTENSION = ".h";

    /**
     * Path to this plug-in resource with icon for this wizard.
     */
    private static final String ICON_PATH = "resources/icons/headerFileWizardIcon.png";

    /**
     * Various texts that appear in the wizard interface.
     */
    private static final String PAGE_NAME = "New NesC header file";
    private static final String PAGE_DESCRIPTION = "Create a new NesC header file.";
    private static final String LABEL_FILENAME = "Header file name";
    private static final String LABEL_GUARD_CHECKBOX = "Add header guard";
    private static final String LABEL_COMMENTS_CHECKBOX = "Generate comments";
    private static final String INFO_MSG_EXTENSION = "'" + HEADER_FILE_EXTENSION + "' extension will "
            + "be appended to the entered header file name.";

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
     * The control that allows choosing if the comments will be added to the
     * generated file.
     */
    private Button commentsCheckbox;

    /**
     * Array with all fields from this wizard for quick iteration.
     */
    private WizardField[] fields;

    NescHeaderFileWizardPage() {
        super(PAGE_NAME, PAGE_NAME, NescWizardSupport.getImageDescriptorForResource(ICON_PATH));
        setDescription(PAGE_DESCRIPTION);
    }

    @Override
    public void createControl(Composite parent) {
        // Create the composite for this page
        final Composite pageComposite = new Composite(parent, NONE);
        pageComposite.setLayout(new GridLayout());

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

        // Create the comments checkbox
        commentsCheckbox = new Button(pageComposite, CHECK);
        commentsCheckbox.setText(LABEL_COMMENTS_CHECKBOX);
        commentsCheckbox.setSelection(true);

        // Final actions
        sourceFolderField.align(new AbstractField[] { fileNameField });
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
        return sourceFolderField.getValue() + getNewHeaderFileName();
    }

    /**
     * @return Only the name of the header file to be created by this wizard.
     */
    public String getNewHeaderFileName() {
        final String enteredValue = fileNameField.getValue();

        return     enteredValue.endsWith(HEADER_FILE_EXTENSION)
                ?  enteredValue
                :  enteredValue + HEADER_FILE_EXTENSION;
    }

    /**
     * @return True if and only if the user has chosen to add the guard header.
     */
    public boolean getHeaderGuardFlag() {
        return headerGuardCheckbox.getSelection();
    }

    /**
     * @return True if and only if the user has chosen to add the comments.
     */
    public boolean getCommentsFlag() {
       return commentsCheckbox.getSelection();
    }

    /**
     * @return Initial contents of the new header file to be created.
     */
    public NewFileContents getNewHeaderFileContents() {
        // Create all needed output streams
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(byteOut);

        // Add the comments if the user has chosen to do so
        if (getCommentsFlag()) {
            out.println(NescWizardSupport.generateHeadComment());
            out.println();
        }

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
        final String fileName = getNewHeaderFileName().toUpperCase();
        final String afterDotsChanged = fileName.replace('.', '_');
        final String afterCleanup = afterDotsChanged.replaceAll("\\W+", ""),
                     frame = "__";

        return frame + afterCleanup + frame;
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

        final String newHeaderFileName = getNewHeaderFileName();

        // Check if the file name is unique
        if (sourceFolderField.fileExists(newHeaderFileName)) {
            setErrorMessage("'" + newHeaderFileName + "' already exists in "
                    + "the selected source folder.");
            setPageComplete(false);
            return;
        }

        // No errors have been found
        setErrorMessage(null);
        setPageComplete(true);

        // Look for other things - check if the file name is alright
        if (!fileNameField.getValue().endsWith(HEADER_FILE_EXTENSION)) {
            setMessage(INFO_MSG_EXTENSION, INFORMATION);
            return;
        }

        // No warnings
        setMessage(null, INFORMATION);
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

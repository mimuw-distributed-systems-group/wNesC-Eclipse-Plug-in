package pl.edu.mimuw.nesc.plugin.wizards.fields;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import pl.edu.mimuw.nesc.plugin.natures.NescProjectNature;
import static org.eclipse.swt.SWT.*;

/**
 * A wizard field that will allow the user choosing the source folder of a nesC
 * project.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public class SourceFolderField extends AbstractField {
    /**
     * Text that the browse button will contain.
     */
    private static final String LABEL_BROWSE = "Browse...";

    /**
     * Name for this field is always the same.
     */
    private static final String FIELD_NAME = "Source folder";

    /**
     * Texts used in the dialog for the folder selection.
     */
    private static final String TITLE_FOLDER_SELECTION = "Source Folder Selection";
    private static final String BODY_FOLDER_SELECTION = "Choose a source folder:";

    /**
     * Number of columns in the grid layout for this field.
     */
    private static final int FIELD_COLUMNS_COUNT = 3;

    /**
     * Shell that will be used as parent for dialogs.
     */
    private final Shell shell;

    /**
     * Control that allow users enter the value of this field.
     */
    private final Text text;

    /**
     * Button that allows the user to browse for a source folder.
     */
    private final Button browseButton;

    /**
     * Initializes the field with given values.
     *
     * @param parent Composite that this field will be contained in.
     * @param layoutData Layout data object that will be associated with the
     *                   newly created composite for this field. It can be null.
     * @param defaultValue Initial value to set for this field.
     * @param shell Shell that will be used as parent for dialogs.
     * @throws NullPointerException One of the arguments is null
     *                              (except <code>layoutData</code>).
     */
    public SourceFolderField(Composite parent, Object layoutData, String defaultValue, Shell shell) {
        super(parent, FIELD_NAME, FIELD_COLUMNS_COUNT, layoutData);

        if (defaultValue == null || shell == null) {
            throw new NullPointerException("SourceFolderField.<init>: null argument");
        }

        text = createText(getComposite(), defaultValue);
        this.shell = shell;
        browseButton = createButton();
    }

    @Override
    public void setFocus() {
        text.forceFocus();
    }

    @Override
    public String getErrorStatus() {
        final String value = text.getText();

        if (value.isEmpty()) {
            return "Source folder is empty.";
        } else {
            // Check if the folder already exists
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            final IResource container = root.findMember(value);

            if (container == null) {
                return "Folder '" + value + "' does not exist.";
            } else if (container.getType() != IResource.FOLDER
                    && container.getType() != IResource.PROJECT) {
                return "'" + value + "' must be a project or a folder.";
            } else {
                // Check if this is a nesC project
                final IProject project = container.getProject();

                try {
                    if (!project.hasNature(NescProjectNature.NATURE_ID)) {
                        return "Source folder is not a nesC project.";
                    }
                } catch(CoreException e) {
                    return "Cannot identify source folder '" + project.getName() + "'.";
                }
            }
        }

        return null;
    }

    /**
     * The path is always returned with the trailing separator.
     */
    @Override
    public String getValue() {
        return new Path(text.getText()).addTrailingSeparator().toString();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        text.setEnabled(enabled);
        browseButton.setEnabled(enabled);
    }

    /**
     * @return True if and only if this field is empty.
     */
    public boolean isEmpty() {
        return text.getText().isEmpty();
    }

    /**
     * @param fileName Name of the file to check the existence.
     * @return True if and only if file with the given name exists in the
     *         source folder entered by the user in this field.
     * @throws NullPointerException The argument is null.
     */
    public boolean fileExists(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("SourceFolderField.fileExists: null argument");
        }

        // Get the container
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IResource sourceResource = root.findMember(new Path(getValue()), false);
        if (sourceResource == null || sourceResource.getType() != IResource.PROJECT
                && sourceResource.getType() != IResource.FOLDER) {
            return false;
        }
        final IContainer container = (IContainer) sourceResource;

        try {
            /* Iterate over all files in the selected folder (other methods are
               case-sensitive which is wrong on some systems while creating a new
               file). */
            for (IResource resource : container.members()) {
                if (resource.getName().toLowerCase().equals(fileName.toLowerCase())) {
                    return true;
                }
            }

            return false;
        } catch (CoreException e) {
            // If an error happens, try the case-sensitive method
            return root.exists(new Path(getValue() + fileName));
        }
    }

    /**
     * Adds a listener that will be notified when the value of this field
     * changes.
     *
     * @param listener Listener to add.
     */
    public void addModifyListener(ModifyListener listener) {
        text.addModifyListener(listener);
    }

    /**
     * Creates and returns the text control for this field.
     *
     * @param parent Control that the returned one will be contained in.
     * @param defaultValue Initial value that the control will contain.
     * @return The newly created text control.
     */
    private static Text createText(Composite parent, String defaultValue) {
        final Text result = new Text(parent, BORDER | SINGLE);
        result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        result.setText(defaultValue);

        return result;
    }

    /**
     * Creates and configures the button that will allow the user choosing
     * a folder in a nesC project.
     *
     * @return The newly created button control.
     */
    private Button createButton() {
        final Button result = new Button(getComposite(), NONE);
        result.setText(LABEL_BROWSE);

        result.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // Create configure and show the dialog
                final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
                        new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
                dialog.setTitle(TITLE_FOLDER_SELECTION);
                dialog.setMessage(BODY_FOLDER_SELECTION);
                dialog.addFilter(NescProjectsFilter.getInstance());
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
                dialog.open();

                final Object[] result = dialog.getResult();
                if (result != null) {
                    // Update the path to the selected one
                    if (result[0] instanceof IResource) {
                        final IResource selectedResource = (IResource) result[0];
                        text.setText(selectedResource.getFullPath().toString());
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });

        return result;
    }
}

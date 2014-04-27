package pl.edu.mimuw.nesc.plugin.wizards;

import pl.edu.mimuw.nesc.plugin.projects.NescProjectSupport;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


/**
 * Wizard that allows creating and adding to a nesC project a header file.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public class NescHeaderFileWizard extends Wizard implements INewWizard {
    private static final String WIZARD_TITLE = "nesC Header File Wizard";
    private final NescHeaderFileWizardPage page = new NescHeaderFileWizardPage();

    public NescHeaderFileWizard() {
        setWindowTitle(WIZARD_TITLE);
    }

    @Override
    public void addPages() {
        super.addPages();
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        final String newHeaderFileName = page.getNewHeaderFileFullPath();
        final NewFileContents newHeaderFileContents = page.getNewHeaderFileContents();

        // Create the file
        final IFile newHeaderFile = NescProjectSupport.createFile(newHeaderFileName,
                newHeaderFileContents.contents);
        if (newHeaderFile == null) {
            page.setErrorMessage("Cannot create the new header file.");
            return false;
        }

        // Open the editor
        NescWizardSupport.openEditor(newHeaderFile, newHeaderFileContents.cursorOffset,
                getShell(), "The new header file has been successfully created. However, "
                + "the operation of opening it in an editor has failed.");
        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {}
}

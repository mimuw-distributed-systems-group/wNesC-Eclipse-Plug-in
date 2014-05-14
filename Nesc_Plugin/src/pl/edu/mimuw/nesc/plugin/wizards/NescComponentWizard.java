package pl.edu.mimuw.nesc.plugin.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import pl.edu.mimuw.nesc.plugin.projects.NescProjectSupport;

/**
 * Wizard that allows creating new nesC components.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public class NescComponentWizard extends Wizard implements INewWizard {
    private static final String WIZARD_TITLE = "nesC Component Wizard";
    private final NescComponentWizardPage page = new NescComponentWizardPage();

    public NescComponentWizard() {
        setWindowTitle(WIZARD_TITLE);
    }

    @Override
    public void addPages() {
        super.addPages();
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        final String fileName = page.getNewComponentFullPath();
        final NewFileContents contents = page.getNewComponentContents();

        // Create the file
        final IFile newFile = NescProjectSupport.createFile(fileName, contents.contents);
        if (newFile == null) {
            page.setErrorMessage("Cannot create the new component file.");
            return false;
        }

        // Open the editor
        NescWizardSupport.openEditor(newFile, contents.cursorOffset, getShell(),
                "The new component file has been successfully created. However, "
                + "the operation of opening an editor for it has failed.");

        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {}
}

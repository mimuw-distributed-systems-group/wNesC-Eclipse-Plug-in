package pl.edu.mimuw.nesc.plugin.wizards;

import pl.edu.mimuw.nesc.plugin.projects.NescProjectSupport;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard that creates new nesC interfaces.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public class NescInterfaceWizard extends Wizard implements INewWizard {
    /**
     * Identifier of the wizard from 'plugin.xml' file.
     */
    public static final String WIZARD_ID = "pl.edu.mimuw.nesc.wizards.new.NescInterfaceWizard";

    private static final String WIZARD_TITLE = "NesC Interface Wizard";
    private final NescInterfaceWizardPage page = new NescInterfaceWizardPage();

    public NescInterfaceWizard() {
        setWindowTitle(WIZARD_TITLE);
    }

    @Override
    public void addPages() {
        super.addPages();
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        final String newFileFullPath = page.getNewInterfaceFullPath();
        final NewFileContents contents = page.getNewInterfaceContents();

        // Create the file
        final IFile newInterfaceFile = NescProjectSupport.createFile(newFileFullPath,
                contents.contents);
        if (newInterfaceFile == null) {
            page.setErrorMessage("Cannot create the new interface file!");
            return false;
        }

        // Open the editor with this file
        NescWizardSupport.openEditor(newInterfaceFile, contents.cursorOffset, getShell(),
                "Interface '" + newFileFullPath + "' has been successfully created. "
                + "However, the operation of opening an editor for it has failed.");

        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {}
}

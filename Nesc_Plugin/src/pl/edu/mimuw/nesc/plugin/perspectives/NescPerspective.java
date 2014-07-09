package pl.edu.mimuw.nesc.plugin.perspectives;

import pl.edu.mimuw.nesc.plugin.wizards.NescComponentWizard;
import pl.edu.mimuw.nesc.plugin.wizards.NescHeaderFileWizard;
import pl.edu.mimuw.nesc.plugin.wizards.NescInterfaceWizard;
import pl.edu.mimuw.nesc.plugin.wizards.NescNewProjectWizard;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Class that represents the nesC perspective. It is responsible of creating
 * and configuring all elements that a perspective consists of.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public class NescPerspective implements IPerspectiveFactory {
    private static final String FOLDER_ID_BOTTOM = "bottomFolder";

    @Override
    public void createInitialLayout(IPageLayout layout) {
        // Add wizards shortcuts
        layout.addNewWizardShortcut(NescNewProjectWizard.WIZARD_ID);
        layout.addNewWizardShortcut(NescHeaderFileWizard.WIZARD_ID);
        layout.addNewWizardShortcut(NescInterfaceWizard.WIZARD_ID);
        layout.addNewWizardShortcut(NescComponentWizard.WIZARD_ID);

        // Add the views
        layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.2f, layout.getEditorArea());
        final IFolderLayout bottomFolder = layout.createFolder(FOLDER_ID_BOTTOM, IPageLayout.BOTTOM,
                0.75f, layout.getEditorArea());
        bottomFolder.addView(IPageLayout.ID_PROBLEM_VIEW);
        bottomFolder.addView(IPageLayout.ID_TASK_LIST);
    }
}

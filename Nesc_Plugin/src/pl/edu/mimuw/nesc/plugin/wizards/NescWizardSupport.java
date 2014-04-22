package pl.edu.mimuw.nesc.plugin.wizards;

import pl.edu.mimuw.nesc.plugin.editor.NescEditor;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Class that follows the utility design pattern. It contains operations that
 * are common for wizards.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

class NescWizardSupport {
    /**
     * Opens a nesC editor for the file given by argument. The cursor in the
     * editor is set to the given position. If the operation fails and
     * <code>parent</code> and <code>errMsg</code> are not null, a message
     * dialog is shown with this message.
     *
     * @param file File to show in the editor.
     * @param cursorOffset Position of the cursor in the editor to set.
     * @param parent Shell that will be used as the parent for the message
     *               dialog if an error happens.
     * @param errMsg Message that will be shown if the operation fails.
     */
    static void openEditor(IFile file, int cursorOffset, Shell parent, String errMsg) {
        try {
            // Open the editor with the file
            final IEditorInput editorInput = new FileEditorInput(file);
            final IWorkbenchWindow activeWnd = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            final IWorkbenchPage activePage = activeWnd.getActivePage();
            final IEditorPart editorPart = activePage.openEditor(editorInput,
                    NescEditor.EDITOR_ID);

            // Set the cursor properly
            try {
                if (editorPart instanceof ITextEditor) {
                    final ITextEditor textEditor = (ITextEditor) editorPart;
                    textEditor.selectAndReveal(cursorOffset, 0);
                }
            } catch(Exception e) {
                // exceptions in this part are ignored
            }
        } catch(Exception e) {
            if (parent != null && errMsg != null) {
                final MessageDialog dialog = new MessageDialog(parent, "Information", null,
                    errMsg, MessageDialog.INFORMATION, new String [] { "OK" }, 0);
                dialog.open();
            }
        }
    }

    /**
     * @return The initial source folder for a wizard or empty string if it
     *         cannot be obtained.
     */
    static String getInitialSourceFolderFullPath() {
        String result = getSelectedContainerFullPath();
        if ("".equals(result)) {
            result = getActiveEditorContainerFullPath();
        }

        return result;
    }

    /**
     * @return Full path of the currently selected project by the user in the
     *         package explorer. If an error happens, empty string ("") is
     *         returned.
     */
    static String getSelectedContainerFullPath() {
        // Get the active window
        final IWorkbenchWindow activeWnd = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWnd == null) {
            return "";
        }

        // Get the selection
        final ISelection selection = activeWnd.getSelectionService().getSelection();
        if (!(selection instanceof IStructuredSelection)) {
            return "";
        }
        final IStructuredSelection structSelection = (IStructuredSelection) selection;

        // Get the selected element and examine it
        final Object selected = structSelection.getFirstElement();
        if (!(selected instanceof IAdaptable)) {
            return "";
        }
        final IAdaptable selectedAdaptable = (IAdaptable) selected;
        final IContainer curContainer = (IContainer) selectedAdaptable.getAdapter(IContainer.class);
        if (curContainer == null) {
            return "";
        }

        return curContainer.getFullPath().toString();
    }

    /**
     * @return Full path to the container of the file in the currently opened
     *         tab. If the operation fails, empty string is returned.
     */
    static String getActiveEditorContainerFullPath() {
        // Get active window
        final IWorkbenchWindow activeWnd = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWnd == null) {
            return "";
        }

        // Examine currently opened file
        final IWorkbenchPage activePage = activeWnd.getActivePage();
        if (activePage == null) {
            return "";
        }
        final IEditorPart editorPart = activePage.getActiveEditor();
        if (editorPart == null) {
            return "";
        }
        final IEditorInput editorInput = editorPart.getEditorInput();
        if (!(editorInput instanceof IFileEditorInput)) {
            return "";
        }
        final IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
        final IContainer parent = fileEditorInput.getFile().getParent();
        if (parent == null) {
            return "";
        }

        return parent.getFullPath().toString();
    }

    /**
     * @param internalPath Path inside the plug-in to the resource. It should not
     *                     be absolute and start with a slash.
     * @return URL to the plug-in resource indicated by the given path.
     * @throws MalformedURLException The generated URL is invalid.
     */
    static URL getPluginResourceURL(String internalPath) throws MalformedURLException {
        return new URL("platform:/plugin/Nesc_Plugin/" + internalPath);
    }
}

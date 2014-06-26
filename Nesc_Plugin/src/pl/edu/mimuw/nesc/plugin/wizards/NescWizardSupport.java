package pl.edu.mimuw.nesc.plugin.wizards;

import pl.edu.mimuw.nesc.plugin.NescPlugin;
import pl.edu.mimuw.nesc.plugin.editor.NescEditor;
import pl.edu.mimuw.nesc.plugin.preferences.NescPluginPreferences;
import pl.edu.mimuw.nesc.plugin.preferences.NescPreferencesInitializer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
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
     * Number of spaces in content generated by wizards that create a single
     * indentation step.
     */
    private static final int INDENTATION_SIZE = 4;

    /**
     * Constant with the character that creates indentation. It is obviously
     * a space.
     */
    private static final char INDENTATION_CHAR = ' ';

    /**
     * String that contains exactly a single indentation step in the content
     * generated by wizards.
     */
    private static String indentationStep;

    /**
     * Extension of nesC source files (with the dot).
     */
    static final String NESC_SOURCE_EXTENSION = ".nc";

    /**
     * @return String with whitespace that create exactly a single indentation
     *         step.
     */
    static String getIndentationStep() {
        if (indentationStep == null) {
            char[] indentStepCharArray = new char[INDENTATION_SIZE];
            Arrays.fill(indentStepCharArray, INDENTATION_CHAR);
            indentationStep = new String(indentStepCharArray);
        }

        return indentationStep;
    }

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

    /**
     * Creates and returns an image descriptor for the image in the location
     * specified by the given path. It should be a path of a plug-in resource.
     *
     * @param imagePath Path of a plug-in resource with an image.
     * @return Image descriptor object for the given image or null if an error
     *         happens (e.g. the resource is absent).
     */
    static ImageDescriptor getImageDescriptorForResource(String imagePath) {
        try {
            final URL imageURL = getPluginResourceURL(imagePath);
            return ImageDescriptor.createFromURL(imageURL);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * @return String with comment that is to be placed in the top part of
     *         a file that is created by a wizard if the user chooses such
     *         option. It does not contain any newline character after or before
     *         the comment text.
     */
    static String generateHeadComment() {
        return getPreferenceValue(NescPluginPreferences.HEAD_COMMENT,
                NescPreferencesInitializer.getDefaultHeadComment());
    }

    /**
     * @return String with comment that is to be placed exactly before an
     *         interface or component definition. It does not contain any
     *         newline character before or after the comment text.
     */
    static String generateEntityComment() {
        return getPreferenceValue(NescPluginPreferences.ENTITY_COMMENT,
                NescPreferencesInitializer.getDefaultEntityComment());
    }

    /**
     * @param preferenceName Name of the preference to retrieve.
     * @param valueIfEmpty Value that will be returned if the value of the
     *                     preference with given name is empty.
     * @return Value of the preference with given name or
     *         <code>valueIfEmpty</code> if it is empty.
     */
    private static String getPreferenceValue(String preferenceName, String valueIfEmpty) {
        final IPreferenceStore store = NescPlugin.getDefault().getPreferenceStore();
        final String preferredValue = store.getString(preferenceName).trim();

        return   !preferredValue.isEmpty()
               ? preferredValue
               : valueIfEmpty.trim();
    }
}

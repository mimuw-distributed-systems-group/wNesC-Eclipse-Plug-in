package pl.edu.mimuw.nesc.plugin.editor.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Base class for content assist processors.
 * </p>
 * <p>
 * By default no context information is provided.
 * </p>
 * <p>
 * Subclasses must implement proposals computation and define whether proposal
 * auto activation should be enabled.
 * </p>
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public abstract class CompletionProcessorBase extends TemplateCompletionProcessor {

	protected static final List<ICompletionProposal> NO_COMPLETIONS_LIST = new ArrayList<>();
	protected static final ICompletionProposal[] NO_COMPLETIONS = new ICompletionProposal[] {};

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	/**
	 * Gets line of current offset.
	 *
	 * @param viewer
	 *            text viewer
	 * @param offset
	 *            current offset
	 * @return line of offset
	 * @throws BadLocationException
	 */
	protected int getLine(ITextViewer viewer, int offset) throws BadLocationException {
		return viewer.getDocument().getLineOfOffset(offset) + 1;
	}

	/**
	 * Gets column of current offset.
	 *
	 * @param viewer
	 *            text viewer
	 * @param offset
	 *            current offset
	 * @return line of offset
	 * @throws BadLocationException
	 */
	protected int getColumn(ITextViewer viewer, int offset) throws BadLocationException {
		final IDocument document = viewer.getDocument();
		final int line = document.getLineOfOffset(offset);
		return offset - document.getLineOffset(line);
	}

	/**
	 * Gets current project.
	 *
	 * @return current project.
	 */
	protected IProject getProject() {
		final IFile currentFile = getCurrentFile();
		if (currentFile == null) {
			return null;
		}
		return currentFile.getProject();
	}

	/**
	 * Gets current file.
	 *
	 * @return current file
	 */
	protected IFile getCurrentFile() {
		final IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage page = win.getActivePage();
		if (page != null) {
			final IEditorPart editor = page.getActiveEditor();
			if (editor != null) {
				final IEditorInput input = editor.getEditorInput();
				if (input instanceof IFileEditorInput) {
					return ((IFileEditorInput) input).getFile();
				}
			}
		}
		return null;
	}

	/**
	 * Gets full path of current file.
	 *
	 * @return file path
	 */
	protected String getCurrentFilePath() {
		final IFile currentFile = getCurrentFile();
		if (currentFile == null) {
			throw new IllegalStateException();
		}
		return currentFile.getRawLocation().toOSString();
	}
}

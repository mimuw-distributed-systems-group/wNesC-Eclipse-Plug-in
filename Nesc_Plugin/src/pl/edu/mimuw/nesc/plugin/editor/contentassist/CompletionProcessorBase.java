package pl.edu.mimuw.nesc.plugin.editor.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;

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

}

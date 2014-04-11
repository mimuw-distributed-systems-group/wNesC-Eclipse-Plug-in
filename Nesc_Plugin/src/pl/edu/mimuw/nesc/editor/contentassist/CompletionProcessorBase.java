package pl.edu.mimuw.nesc.editor.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;

/**
 * <p>Base class for content assist processors.</p>
 * <p>By default no context information is provided.</p>
 * <p>Subclasses must implement proposals computation and define whether
 * proposal auto activation should be enabled.</p>
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

}

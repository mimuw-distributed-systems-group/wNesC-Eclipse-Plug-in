package pl.edu.mimuw.nesc.editor.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.swt.graphics.Image;

import pl.edu.mimuw.nesc.editor.contentassist.scanner.ContextScanner;
import pl.edu.mimuw.nesc.editor.contentassist.scanner.IdentifierScanner;
import pl.edu.mimuw.nesc.editor.contentassist.scanner.Token;

/**
 * Content assist processor for preprocessor partition.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 */
public class PreprocessorCompletionProcessor extends CompletionProcessorBase {

	private static final PreprocessorProposalsComparator COMPARATOR = new PreprocessorProposalsComparator();
	private static final String[] PREPROCESSOR_KEYWORDS;
	private static final Template USER_INCLUDE;
	private static final Template SYSTEM_INCLUDE;
	private static final Template[] PREPROCESSOR_TEMPLATES;
	private static final String PREPROCESSOR_TEMPLATE_CONTEXT_NAME = "preprocessor_context";
	private static final TemplateContextType PREPROCESSOR_TEMPLATE_CONTEXT_TYPE = new TemplateContextType(
			PREPROCESSOR_TEMPLATE_CONTEXT_NAME);

	static {
		PREPROCESSOR_KEYWORDS = new String[] { "define", "defined", "elif", "else", "endif", "error", "if", "ifdef",
				"ifndef", "include", "line", "pragma", "undef", "warning" };
		USER_INCLUDE = new Template("include \"file\"", "include user header file", PREPROCESSOR_TEMPLATE_CONTEXT_NAME,
				"include \"${}\"\r\n", true);
		SYSTEM_INCLUDE = new Template("include <file>", "include system header file",
				PREPROCESSOR_TEMPLATE_CONTEXT_NAME, "include <${}>\r\n", true);
		PREPROCESSOR_TEMPLATES = new Template[] { USER_INCLUDE, SYSTEM_INCLUDE };
	};

	private final ContextScanner contextScanner;

	public PreprocessorCompletionProcessor() {
		this.contextScanner = new IdentifierScanner();
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		final List<ICompletionProposal> proposals = computeProposals(viewer, offset);
		final List<ICompletionProposal> templates = computeTemplates(viewer, offset);
		proposals.addAll(templates);
		Collections.sort(proposals, COMPARATOR);
		return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	protected Template[] getTemplates(String contextTypeId) {
		if (PREPROCESSOR_TEMPLATE_CONTEXT_NAME.equals(contextTypeId)) {
			return PREPROCESSOR_TEMPLATES;
		}
		return null;
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		return PREPROCESSOR_TEMPLATE_CONTEXT_TYPE;
	}

	@Override
	protected Image getImage(Template template) {
		return null;
	}

	private List<ICompletionProposal> computeProposals(ITextViewer viewer, int offset) {
		final List<Token> tokens = this.contextScanner.getTokens(viewer.getDocument(), offset);
		if (tokens.isEmpty()) {
			return NO_COMPLETIONS_LIST;
		}
		final Token identifier = tokens.get(0);
		final String prefix = identifier.getValue();
		final List<ICompletionProposal> proposals = new ArrayList<>();

		for (String id : PREPROCESSOR_KEYWORDS) {
			if (id.startsWith(prefix)) {
				final ICompletionProposal proposal = new CompletionProposal(id, identifier.getOffset(),
						identifier.getLength(), id.length());
				proposals.add(proposal);
			}
		}
		return proposals;
	}

	private List<ICompletionProposal> computeTemplates(ITextViewer viewer, int offset) {
		final List<ICompletionProposal> templates = new ArrayList<>();
		final ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
		if (selection.getOffset() == offset) {
			offset = selection.getOffset() + selection.getLength();
		}
		final String prefix = extractPrefix(viewer, offset);
		final IRegion region = new Region(offset - prefix.length(), prefix.length());
		final TemplateContext context = createContext(viewer, region);
		if (context == null) {
			return NO_COMPLETIONS_LIST;
		}

		for (Template template : getTemplates(PREPROCESSOR_TEMPLATE_CONTEXT_NAME)) {
			try {
				context.getContextType().validate(template.getPattern());
			} catch (TemplateException e) {
				e.printStackTrace();
				// TODO log
				continue;
			}
			if (template.getName().startsWith(prefix)) {
				/*
				 * Relevance is not important, since there are only a few
				 * proposals.
				 */
				final ICompletionProposal proposal = createProposal(template, context, region, 1);
				templates.add(proposal);
			}
		}
		return templates;
	}

	/**
	 * Sorts proposals lexicographically.
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 */
	private static final class PreprocessorProposalsComparator implements Comparator<ICompletionProposal> {

		@Override
		public int compare(ICompletionProposal lhs, ICompletionProposal rhs) {
			return lhs.getDisplayString().compareToIgnoreCase(rhs.getDisplayString());
		}
	}

}

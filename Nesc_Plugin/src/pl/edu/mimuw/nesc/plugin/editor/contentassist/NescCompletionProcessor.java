package pl.edu.mimuw.nesc.plugin.editor.contentassist;

import static pl.edu.mimuw.nesc.plugin.editor.contentassist.EnvironmentUtils.flattenEnvironment;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.EnvironmentUtils.getEnvironment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
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

import pl.edu.mimuw.nesc.FileData;
import pl.edu.mimuw.nesc.ast.Location;
import pl.edu.mimuw.nesc.environment.Environment;
import pl.edu.mimuw.nesc.plugin.editor.ImageManager;
import pl.edu.mimuw.nesc.plugin.editor.NescEditor;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.CommandEventPattern;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.IdentifierChainPattern;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.Pattern;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.TaskPattern;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.VariablePattern;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.ContextScanner;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.DefaultPartitionScanner;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token;
import pl.edu.mimuw.nesc.plugin.projects.util.ProjectManager;

/**
 * Completion proposals processor for nesc code.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescCompletionProcessor extends CompletionProcessorBase {

	private static final TemplateContextType NESC_CONTEXT_TYPE = new NescContextType();
	private static final char[] AUTO_ACTIVATION_CHARACTERS = new char[] { '.', '>' };

	// TODO: for each scope type provide different set of patterns
	private static final Pattern[] PATTERNS = new Pattern[] { new VariablePattern(), new CommandEventPattern(),
			new TaskPattern(), new IdentifierChainPattern() };

	private static final NescProposalsComparator COMPARATOR = new NescProposalsComparator();

	private final NescEditor nescEditor;
	private final ContextScanner scanner;

	public NescCompletionProcessor(NescEditor nescEditor) {
		this.nescEditor = nescEditor;
		this.scanner = new DefaultPartitionScanner();
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		final FileData fileData = ProjectManager.getFileData(getProject(), getCurrentFilePath());
		/* Hardly possible? situation when FileData is unavailable. */
		if (fileData == null) {
			System.out.println("FileData is unavailable");
			return null;
		}
		final List<Token> tokens = this.scanner.getTokens(viewer.getDocument(), offset);
		List<ICompletionProposal> proposals;
		try {
			proposals = computeProposals(viewer, offset, tokens, fileData);
		} catch (BadLocationException e) {
			proposals = Collections.emptyList();
		}
		Collections.sort(proposals, COMPARATOR);
		return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return AUTO_ACTIVATION_CHARACTERS;
	}

	@Override
	protected Template[] getTemplates(String contextTypeId) {
		return null;
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		return NESC_CONTEXT_TYPE;
	}

	@Override
	protected Image getImage(Template template) {
		// hack but this is the simplest way
		if (template instanceof NescTemplate) {
			return ((NescTemplate) template).getImage();
		}
		return ImageManager.COMPLETION_TEMPLATE;
	}

	/**
	 * Computes proposals.
	 *
	 * @param viewer
	 *            text viewer
	 * @param offset
	 *            current offset
	 * @param tokens
	 *            recognized tokens
	 * @param fileData
	 *            current file data
	 * @return list of proposals
	 * @throws BadLocationException
	 */
	private List<ICompletionProposal> computeProposals(ITextViewer viewer, int offset, List<Token> tokens,
			FileData fileData) throws BadLocationException {
		final List<ICompletionProposal> result = new ArrayList<>();

		/* Prepare flattened environment. */
		final Location location = new Location(nescEditor.getFileLocation(), getLine(viewer, offset), getColumn(viewer,
				offset));
		final Environment environment = getEnvironment(fileData.getEnvironment(), location);
		final Environment flattenedEnvironment = flattenEnvironment(environment);

		/* Find matching patterns. */
		final List<Pattern> matchingPatterns = getMatchingPatterns(tokens, offset);

		/* Collect proposals. */
		final ObjectProposalBuilder objectProposalBuilder = new ObjectProposalBuilder(offset, flattenedEnvironment,
				environment.getScopeType(), matchingPatterns);
		objectProposalBuilder.buildProposals();
		addProposals(viewer, result, objectProposalBuilder.getProposals(), objectProposalBuilder.getTemplates());
		return result;
	}

	/**
	 * Adds given proposals and templates into the result list.
	 *
	 * @param viewer
	 *            current text viewer
	 * @param result
	 *            result list
	 * @param proposals
	 *            simple completion proposals
	 * @param templates
	 *            template completion proposals
	 */
	private void addProposals(ITextViewer viewer, List<ICompletionProposal> result,
			List<NescCompletionProposal> proposals, List<NescTemplateProposal> templates) {
		for (NescCompletionProposal p : proposals) {
			final ICompletionProposal proposal = buildProposal(p.getReplacementString(), p.getOffset(), p.getLength(),
					p.getImage());
			result.add(proposal);
		}

		for (NescTemplateProposal t : templates) {
			final ICompletionProposal proposal = buildTemplate(viewer, t.getTemplate(), t.getOffset(), t.getLength());
			result.add(proposal);
		}
	}

	/**
	 * Gets patterns that match the given token list.
	 *
	 * @param tokens
	 *            tokens
	 * @param offset
	 *            current offset
	 * @return matching patterns
	 */
	private List<Pattern> getMatchingPatterns(List<Token> tokens, int offset) {
		final List<Pattern> result = new ArrayList<>();
		for (Pattern pattern : PATTERNS) {
			if (pattern.match(tokens, offset)) {
				result.add(pattern);
			}
		}
		return result;
	}

	/**
	 * Builds a plain completion proposal.
	 *
	 * @param replacementString
	 *            replacement string
	 * @param offset
	 *            offset of the prefix of the proposal
	 * @param length
	 *            length of text to be inserted
	 * @param image
	 *            image
	 * @return completion proposal
	 */
	private ICompletionProposal buildProposal(String replacementString, int offset, int length, Image image) {
		final ICompletionProposal proposal = new CompletionProposal(replacementString, offset, length,
				replacementString.length(), image, null, null, null);
		return proposal;
	}

	/**
	 * Builds a completion proposal based on the template.
	 *
	 * @param viewer
	 *            current text viewer
	 * @param template
	 *            template
	 * @param offset
	 *            current offset in text
	 * @param length
	 *            length of the prefix of the proposal
	 * @return completion proposal or <code>null</code> if proposal cannot be
	 *         built (invalid template body or template context)
	 */
	private ICompletionProposal buildTemplate(ITextViewer viewer, Template template, int offset, int length) {
		final ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
		if (selection.getOffset() == offset) {
			offset = selection.getOffset() + selection.getLength();
		}
		final IRegion region = new Region(offset - length, length);
		final TemplateContext context = createContext(viewer, region);
		if (context == null) {
			return null;
		}

		try {
			context.getContextType().validate(template.getPattern());
		} catch (TemplateException e) {
			e.printStackTrace();
			// TODO log
			return null;
		}
		return createProposal(template, context, region, 1);
	}

	/**
	 * Sorts proposals lexicographically.
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 *
	 */
	private static class NescProposalsComparator implements Comparator<ICompletionProposal> {

		@Override
		public int compare(ICompletionProposal lhs, ICompletionProposal rhs) {
			return lhs.getDisplayString().compareToIgnoreCase(rhs.getDisplayString());
		}
	}

}

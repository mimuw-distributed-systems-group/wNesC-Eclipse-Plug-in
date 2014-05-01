package pl.edu.mimuw.nesc.plugin.editor.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.graphics.Image;

import pl.edu.mimuw.nesc.environment.ScopeType;
import pl.edu.mimuw.nesc.plugin.editor.ImageManager;

/**
 * Base class for completion proposal builders.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public abstract class ProposalBuilder {

	protected final List<NescCompletionProposal> proposals;
	protected final List<NescTemplateProposal> templates;

	protected ProposalBuilder() {
		this.proposals = new ArrayList<>();
		this.templates = new ArrayList<>();
	}

	public abstract void buildProposals();

	public List<NescCompletionProposal> getProposals() {
		return proposals;
	}

	public List<NescTemplateProposal> getTemplates() {
		return templates;
	}

	protected NescCompletionProposal buildProposal(String replacementString, int offset, int length, Image image) {
		final NescCompletionProposal proposal = new NescCompletionProposal(replacementString, offset, length, image);
		this.proposals.add(proposal);
		return proposal;
	}

	protected Template buildTemplate(String name, String description, String contextTypeId, String pattern,
			Image image) {
		final Template template = new Template(name, description, contextTypeId, pattern, true);
		final NescTemplate nescTemplate = new NescTemplate(template, image);
		return nescTemplate;
	}

	protected void buildNescTemplateProposal(Template template, int offset, int length) {
		final NescTemplateProposal proposal = new NescTemplateProposal(template, offset, length);
		this.templates.add(proposal);
	}

	protected boolean isAllowedScope(ScopeType currentScope, ScopeType... allowed) {
		for (ScopeType type : allowed) {
			if (currentScope == type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets visibility corresponding to given scope type.
	 *
	 * @param scopeType
	 *            scope type
	 * @return visibility
	 */
	protected Visibility scopeToVisibility(ScopeType scopeType) {
		switch (scopeType) {
		case GLOBAL:
			return Visibility.GLOBAL;
		case COMPOUND:
		case FUNCTION_PARAMETER:
			return Visibility.LOCAL;
		case COMPONENT_PARAMETER:
		case SPECIFICATION:
		case MODULE_IMPLEMENTATION:
		case CONFIGURATION_IMPLEMENTATION:
		case INTERFACE_PARAMETER:
		case INTERFACE:
			return Visibility.COMPONENT;
		default:
			throw new IllegalArgumentException("Unknown scope type " + scopeType);
		}
	}

	/**
	 * Gets completion proposal image for given visibility.
	 *
	 * @param visibility
	 *            visibility of declaration to be proposed
	 * @return image
	 */
	protected Image getVisibilityImage(Visibility visibility) {
		switch (visibility) {
		case LOCAL:
			return ImageManager.COMPLETION_LOCAL;
		case COMPONENT:
			return ImageManager.COMPLETION_COMPONENT;
		case GLOBAL:
			return ImageManager.COMPLETION_GLOBAL;
		case UNDEFINED:
			return ImageManager.COMPLETION_TEMPLATE;
		default:
			throw new IllegalArgumentException("Unknown visibility " + visibility);
		}
	}

	/**
	 * Gets completion proposal image for given scope.
	 *
	 * @param scopeType
	 *            scope type
	 * @return image
	 */
	protected Image imageForScope(ScopeType scopeType) {
		return getVisibilityImage(scopeToVisibility(scopeType));
	}

}

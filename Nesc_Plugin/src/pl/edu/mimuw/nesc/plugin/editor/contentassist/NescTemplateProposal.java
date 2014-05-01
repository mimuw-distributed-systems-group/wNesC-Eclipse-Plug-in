package pl.edu.mimuw.nesc.plugin.editor.contentassist;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;

/**
 * <p>
 * Template completion proposal.
 * </p>
 * <p>
 * Note that this class does not implements {@link ICompletionProposal}.
 * </p>
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescTemplateProposal {

	private final Template template;
	private final int offset;
	private final int length;

	public NescTemplateProposal(Template template, int offset, int length) {
		this.template = template;
		this.offset = offset;
		this.length = length;
	}

	public Template getTemplate() {
		return template;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

}

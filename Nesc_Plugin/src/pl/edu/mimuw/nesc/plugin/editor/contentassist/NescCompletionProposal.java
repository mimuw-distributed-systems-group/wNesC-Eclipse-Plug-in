package pl.edu.mimuw.nesc.plugin.editor.contentassist;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

/**
 * <p>
 * Simple completion proposal. Object of this class contains only data to be
 * used to build final proposal, but it is not a proposal itself.
 * </p>
 * <p>
 * Note that this class does not implements {@link ICompletionProposal}.
 * </p>
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescCompletionProposal {

	private final String replacementString;
	private final int offset;
	private final int length;
	private final Image image;

	public NescCompletionProposal(String replacementString, int offset, int length, Image image) {
		this.replacementString = replacementString;
		this.offset = offset;
		this.length = length;
		this.image = image;
	}

	public String getReplacementString() {
		return replacementString;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public Image getImage() {
		return image;
	}

}

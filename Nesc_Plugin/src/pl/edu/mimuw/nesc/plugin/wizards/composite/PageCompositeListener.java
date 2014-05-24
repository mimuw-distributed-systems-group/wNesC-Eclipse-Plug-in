package pl.edu.mimuw.nesc.plugin.wizards.composite;

/**
 * Collects events from page composite.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public interface PageCompositeListener {

	/**
	 *
	 * @param message
	 *            the message, or null to clear the error message
	 */
	void setErrorMessage(String message);

	/**
	 *
	 * @param isComplete
	 *            <code>true</code> if this page is complete, and and
	 *            <code>false</code> otherwise
	 */
	void setPageComplete(boolean isComplete);
}

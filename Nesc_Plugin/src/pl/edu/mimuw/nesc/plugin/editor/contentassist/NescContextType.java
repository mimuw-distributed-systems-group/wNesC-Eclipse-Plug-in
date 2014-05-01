package pl.edu.mimuw.nesc.plugin.editor.contentassist;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * Context type for nesc template proposals.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescContextType extends TemplateContextType {

	/**
	 * Context type's name.
	 */
	public static final String NESC_CONTEXT_TYPE = "pl.edu.mimuw.nesc.plugin.nesc_content_type"; //$NON-NLS-1$

	public NescContextType() {
		super(NESC_CONTEXT_TYPE);
		addGlobalResolvers();
	}

	private void addGlobalResolvers() {
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());
	}

}

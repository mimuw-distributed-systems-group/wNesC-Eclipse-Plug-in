package pl.edu.mimuw.nesc.plugin.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Empty preference sub page (just to show how to add categorized preference
 * pages).
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescPluginEmptyChildPreferencesPage extends NescPreferencePage {

	@Override
	protected Control createContents(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);
		container.setLayoutData(parentData);

		final Label label = new Label(parent, SWT.NONE);
		label.setText("Empty child page"); //$NON-NLS-1$
		return new Composite(parent, SWT.NULL);
	}

	@Override
	protected void initializeDefaults() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void initializeValues() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void storeValues() {
		// TODO Auto-generated method stub
	}
}
